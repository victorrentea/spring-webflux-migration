package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.SendOptions;
import reactor.rabbitmq.Sender;
import victor.training.spring.sql.Comment;
import victor.training.spring.sql.CommentRepo;
import victor.training.spring.sql.Post;
import victor.training.spring.sql.PostRepo;

import java.security.Principal;

import static java.time.LocalDateTime.now;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC4_CreatePost {
  private final PostRepo postRepo;
  private final CommentRepo commentRepo;

  public record CreatePostRequest(String title, String body, Long authorId) {
    Post toPost() {
      return new Post().title(title).body(body).authorId(authorId);
    }
  }

  @PostMapping("posts")
  @PreAuthorize("isAuthenticated()")
  @Transactional
  public Mono<Long> createPost(@RequestBody CreatePostRequest request) {
//    postRepo.save(request.toPost()); // daca nimeni nu e chaineuit
    // la un ❄️COLD Mono/Flux nimic nu se intampla
    // nu se face SAVE!!!!

//    postRepo.save(request.toPost()).block(); blocant
    return f(request)
        .map(Post::getId)
//        .contextWrite(c->c.put("userId", at.get))
        ;
//    return postRepo.save(request.toPost())
//        .delayUntil(post -> Mono.zip( // in parallel
//            sendPostCreatedEvent("Post created: " + post.id())
//                .doOnError(e -> log.error("VALEU:", e))
//                .onErrorResume(e -> Mono.empty()) // ignora eroarea ❌ => |
//                .thenReturn(42), // not empty please
//            createInitialComment(post.id(), request.title())
//                .flatMap(commentRepo::save))
//            .then())
//        .map(Post::getId)
//        ;
  }

  private Mono<Post> f(CreatePostRequest request) {
    return postRepo.save(request.toPost())
        .delayUntil(e->Mono.deferContextual(context -> System.out.println("User " + context.get("userId"))));

  }

  // ce alte entry pointuri mai pot executa cod in app voastra in afara de REST?
  // - @GetMapping, gRPC <<<  Spring subscrie
  // - @KafkaLIstener/Rabbit/activeMQ... > e bine sa faci .block
  // - @PostConstruct, @EventListener, implements CommandLineRunner .block
  // - @Scheduler .block ca sa nu bata din nou
  // - main() pot #sieu

  private static Mono<Comment> createInitialComment(long postId, String postTitle) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Principal::getName)
        .map(loggedInUser -> new Comment(postId, "Posted on " + now() + ": " + postTitle, loggedInUser));
  }

  private final RabbitTemplate rabbitTemplate;

  private Mono<Void> sendPostCreatedEvent(String message) {
    log.info("Sending message: " + message);
    OutboundMessage outboundMessage = new OutboundMessage("", "post-created-event", message.getBytes());
    return radioactiveRabbit.sendWithPublishConfirms(Mono.just(outboundMessage), new SendOptions())
        .then();
  }

  private final Sender radioactiveRabbit;
}

package victor.training.spring.api;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.spring.mongo.Author;
import victor.training.spring.mongo.AuthorRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC1_GetAllAuthors {
  private final AuthorRepo authorRepo; // MongoDB
  private final ContactApi contactApi;

  @PostConstruct
  public void insertInitialDataInMongo() {
    log.info("Insert in Mongo");
    // legal at startup, blocking MQ listener, @Scheduled
    authorRepo.save(new Author(1000L, "John DOE", "Long description")).block();
  }

  public record GetAuthorsResponse(long id, String name, String email, String bio) {
    GetAuthorsResponse(Author author, String email) {
      this(author.id(), author.name(),email, author.bio());
    }
  }
  @GetMapping("authors")
  public Flux<GetAuthorsResponse> getAllAuthors() {
     return authorRepo.findAll()
        .flatMap(this::toDto);
  }

  private Mono<GetAuthorsResponse> toDto(Author author) {
    return contactApi.fetchEmail(author.id())
        .map(email -> new GetAuthorsResponse(author, email));
  }
  // TODO how many calls in parallel?

  @Component
  @RequiredArgsConstructor
  public static class ContactApi {
    private final WebClient webClient;
    @Cacheable("contact-email")
    public Mono<String> fetchEmail(long authorId) {
      String uri = "http://localhost:9999/contact/" + authorId + "/email";
      return webClient.get().uri(uri).retrieve().bodyToMono(String.class)
          .doOnSubscribe(s -> log.info("Retrieving email for author {}", authorId))
          .doOnNext(e -> log.info("Got response " + e))
          .cache() // i'm gonna cache a cached mono!
          ;
    }
  }
}

package victor.training.spring.api;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.spring.mongo.Author;
import victor.training.spring.mongo.AuthorRepo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC1_GetAllAuthors {
  private final AuthorRepo authorRepo; // MongoDB reactive
  private final ContactApi contactApi;

  @PostConstruct
  public void insertInitialDataInMongo() {
    log.info("Insert in Mongo");
    authorRepo.save(new Author(1000L, "John DOE", "Long description"))
//        .subscribe(); // ❌⚠️non-blocking, async; when this method retruns the work is not yet done + may crash💥
        .block(); // ✅ here, in @KafkaListener, or @Scheduler; not in WEB processing, or in any method returning Mono
  }

  public record GetAuthorsResponse(long id, String name, String email, String bio) {
    GetAuthorsResponse(Author author, String email) {
      this(author.id(), author.name(),email, author.bio());
    }
  }
  @GetMapping("authors")
  public Flux<GetAuthorsResponse> getAllAuthors() {
    // start with blocking code
//    blockingMethod(author); // ❌blocks netty thread here

//    Mono.fromSupplier(() -> blockingMethod(author)) ✅
//        .log()
//        .subscribeOn(Schedulers.boundedElastic())
//        .log()
//        .flatMap(...)

    return authorRepo.findAll()
//        .flatMap(author -> contactApi.fetchEmail(author.id())
//            .map(email->new GetAuthorsResponse(author, email)));
        .publishOn(Schedulers.boundedElastic())
        .log("Before blocking call")
        .map(author -> blockingMethod(author))
        .log("After blocking call")
        .subscribeOn(Schedulers.parallel())
        .log("Spring's thread");
  }

  private GetAuthorsResponse blockingMethod(Author author) {
    log.info("On what thread am I running? {}", Thread.currentThread().getName());
    return new GetAuthorsResponse(author, contactApi.fetchEmailBlocking(author.id()));
  }

  @Component
  @RequiredArgsConstructor
  public static class ContactApi {
    private final WebClient webClient;
    private final ConcurrentMapCacheManager cacheManager;

    //    @Cacheable("contact-email") // ❌❌❌ AVODI @Cacheable #1returns the same value if method is called again with same params
    public Mono<String> fetchEmail(long authorId) {
      log.info("Retrieving email for author {}", authorId);
      //          .cache(); //#1 re-emit the same value to later subscribers TODO ut vr ☢️dangerous; ttl-confusing

      // ⭐️ remote cache eg Redis
//      Mono<String> emailMono = cacheService.getCachedEmailByAuthorId(authorId);
//      emailMono.switchIfEmpty(actualCall.delayUntil(cacheService::put));

      // ⭐️ local in mem cache (caffeine) using the Spring CacheManager abstraction
      // auto-subscribes to mono; ⚠context/cancel is lost
      return cacheManager.getCache("contact-email")
//          .retrieve(authorId, ()-> actualCall.toFuture()); // auto-subscribes to mono; ⚠context/cancel is lost
          // CompletableFuture is auto-started
          // Mono needs you to subscribe
          .get(authorId, () -> webClient.get()
              .uri("http://localhost:9999/contact/" + authorId + "/email")
              .retrieve()
              .bodyToMono(String.class)
              .cache());
    }

    // implement the same flow But using a blocking REST template call and no caching.
    // imagine ... that this is a method you cannot change because it comes from a library
    // (🤔think: HIBERNATE)
    public String fetchEmailBlocking(long authorId) {
      log.info("Retrieving email for author {}", authorId);
      return new RestTemplate().getForObject(
          "http://localhost:9999/contact/" + authorId + "/email",
          String.class); // BLOCKING 😱😱😱😱
    }
  }
}

// backpressure on a Flux, ochestration, combining, windowing
// cancellation (structured concurrency)


// TODO victorrentea 2026-01-15: @Entity (hibernate) for a post-related metadata (eg)
// in-mem cache for contactApi.email
// redis cache for author-approves-comments
package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.spring.mongo.Author;
import victor.training.spring.mongo.AuthorRepo;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetAllAuthors { // #1
  private final AuthorRepo authorRepo; // MongoDB
  private final WebClient webClient;

  @PostConstruct
  public void initialDataInMongo() {
    // whenever a method returns you a Mono/Flux, nothing happens until someone subscribes to that mono/flux
    // usually 99% of time you pass along that Mono/Flux weaving it with other operator .map.flatMap.zio.... untile
    // in the end you return it to Spring

    // TODO why does this work
    authorRepo.save(new Author().setId(1000L).setName("John DOE").setBio("Long description"))
//            .subscribe();
            .block();
  }

  public record GetAuthorsResponse(Long id, String name, String email, String bio) { // JSON
    GetAuthorsResponse(Author author, String email) {
      this(author.getId(), author.getName(),email, author.getBio());
    }
  }
  @GetMapping("authors")
  // if this
  public Flux<GetAuthorsResponse> getAllAuthors() {
     return authorRepo.findAll()
            // .map and .filter are only used for CPU-only transformation (no networking)
//            .flatMap(author -> new GetAuthorsResponse(author, fetchEmail(author.getId())))
            .flatMap(author -> fetchEmail(author.getId())
                    .map(email->new GetAuthorsResponse(author, email)) );
  }

  // If a method in a reactive app does NETWORK anywhere inside it, it always returns a Mono/Flux
  private Mono<String> fetchEmail(Long authorId) {
//    new RestTemplate()
//    WebClient.create();
    return webClient.get()
            .uri("http://localhost:9999/contact/" + authorId + "/email")
            .retrieve()
            .bodyToMono(String.class);
  }
}

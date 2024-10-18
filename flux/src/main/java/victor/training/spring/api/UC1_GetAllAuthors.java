package victor.training.spring.api;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import victor.training.spring.mongo.Author;
import victor.training.spring.mongo.AuthorRepo;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC1_GetAllAuthors {
  private final AuthorRepo authorRepo; // MongoDB
  private final ContactApi contactApi;

  @PostConstruct
  public void insertInitialDataInMongo() {
    log.info("Insert in Mongo");
    authorRepo.save(new Author(1000L, "John DOE", "Long description"))
        .block(); // waits for insert to complete + throws any errors happening
//        .subscribe(); // fire and forget.
  }

  public record GetAuthorsResponse(long id, String name, String email, String bio) {
    GetAuthorsResponse(Author author, String email) {
      this(author.id(), author.name(),email, author.bio());
    }
  }
  @GetMapping("authors")
  public List<GetAuthorsResponse> getAllAuthors() {
    return authorRepo.findAll().toStream()
        .map(author -> new GetAuthorsResponse(author, contactApi.fetchEmail(author.id())))
        .collect(Collectors.toList());
  }
  // TODO how many calls in parallel?

  @Component
  @RequiredArgsConstructor
  public static class ContactApi {
    private final WebClient webClient;
    @Cacheable("contact-email")
    public String fetchEmail(long authorId) {
      log.info("Retrieving email for author {}", authorId);
      String uri = "http://localhost:9999/contact/" + authorId + "/email";
//      return restTemplate.getForObject(uri, String.class);
      return webClient.get()
          .uri(uri)
//          .uri("http://localhost:9999/contact/{}/email",authorId) // better
          .retrieve().bodyToMono(String.class).block();
    }
  }
}

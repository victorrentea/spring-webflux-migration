package victor.training.spring.api;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.mongo.Author;
import victor.training.spring.mongo.AuthorRepo;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UC1_GetAllAuthors {
  private final AuthorRepo authorRepo; // MongoDB
  private final ContactApi contactApi;

  @PostConstruct
  public void insertInitialDataInMongo() {
    authorRepo.save(new Author(1000L, "John DOE", "Long description"));
  }

  public record GetAuthorsResponse(Long id, String name, String email, String bio) {
    GetAuthorsResponse(Author author, String email) {
      this(author.id(), author.name(),email, author.bio());
    }
  }
  @GetMapping("authors")
  public List<GetAuthorsResponse> getAllAuthors() {
    List<GetAuthorsResponse> list = new ArrayList<>();
    for (Author author : authorRepo.findAll()) {
      String email = contactApi.fetchEmail(author.id());
      list.add(new GetAuthorsResponse(author, email));
    }
    return list;
  }

  @Component
  @RequiredArgsConstructor
  public static class ContactApi {
    private final RestTemplate restTemplate;
    @Cacheable("contact-email")
    public String fetchEmail(Long authorId) {
      return restTemplate.getForObject("http://localhost:9999/contact/" + authorId + "/email", String.class);
    }
  }
}

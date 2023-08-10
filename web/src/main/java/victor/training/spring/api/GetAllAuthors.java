package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.mongo.Author;
import victor.training.spring.mongo.AuthorRepo;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetAllAuthors { // #1
  private final AuthorRepo authorRepo; // MongoDB
  private final RestTemplate restTemplate;

  @PostConstruct
  public void initialDataInMongo() {
    authorRepo.save(new Author().setId(1000L).setName("John DOE").setBio("Long description"));
  }

  public record GetAuthorsResponse(Long id, String name, String email, String bio) { // JSON
    GetAuthorsResponse(Author author, String email) {
      this(author.getId(), author.getName(),email, author.getBio());
    }
  }
  @GetMapping("authors")
  public List<GetAuthorsResponse> getAllAuthors() {
    return authorRepo.findAll()
            .stream()
            .map(author -> new GetAuthorsResponse(author, fetchEmail(author.getId())))
            .collect(Collectors.toList());
  }

  private String fetchEmail(Long authorId) {
    return restTemplate.getForObject("http://localhost:9999/contact/" + authorId + "/email", String.class);
  }
}

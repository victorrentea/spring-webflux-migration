package victor.training.spring;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.api.CreateComment.CreateCommentRequest;
import victor.training.spring.api.GetAllAuthors.GetAuthorsResponse;
import victor.training.spring.api.GetAllPosts.GetPostsResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static victor.training.spring.api.CreatePost.CreatePostRequest;
import static victor.training.spring.api.GetPostById.GetPostByIdResponse;

@SuppressWarnings("DataFlowIssue")
@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class UserJourneyTest {
  public static final String BASE_URL = "http://localhost:8080/";
  public static final String NEW_COMMENT = "new comment";
  private final RestTemplate rest = new RestTemplate();
  private int initialPostsCounts;
  private final String createdPostTitle = "Title" + UUID.randomUUID();


  @Test
  @Order(1)
  void get_authors() {
    assertThat(rest.getForObject(BASE_URL + "authors", GetAuthorsResponse[].class))
        .contains(new GetAuthorsResponse(1000L, "John DOE", "jdoe@example.com", "Long description"));
  }


  @Test
  @Order(10)
  void get_posts() {
    GetPostsResponse[] posts = rest.getForObject(BASE_URL + "posts", GetPostsResponse[].class);
    initialPostsCounts = posts.length;
    assertThat(posts)
        .contains(new GetPostsResponse(1L, "Hello world!"))
        .contains(new GetPostsResponse(2L, "Locked Post"));
  }

  @Test
  @Order(11)
  void create_post() {
    rest.postForObject(BASE_URL + "posts", new CreatePostRequest(createdPostTitle,"Some Body", 15L), Void.class);
  }

  @Test
  @Order(12)
  void get_posts_showsNewlyCreatedOne() {
    GetPostsResponse[] posts = rest.getForObject(BASE_URL + "posts", GetPostsResponse[].class);
    assertThat(posts)
        .hasSize(initialPostsCounts + 1)
        .extracting(GetPostsResponse::title)
        .contains(createdPostTitle);

  }
  @Test
  @Order(20)
  void get_post_by_id() {
    assertThat(rest.getForObject(BASE_URL + "posts/1", GetPostByIdResponse.class))
        .extracting(GetPostByIdResponse::id, GetPostByIdResponse::title, GetPostByIdResponse::body)
        .containsExactly(1L, "Hello world!", "European Software Crafters");
  }
  @Test
  @Order(21)
  void create_comment() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth("user","user");
    HttpEntity<CreateCommentRequest> requestEntity = new HttpEntity<>(new CreateCommentRequest(NEW_COMMENT), headers);
    rest.exchange(BASE_URL + "posts/1/comments", HttpMethod.POST, requestEntity,Void.class);
  }
  @Test
  @Order(22)
  void get_post_by_id_shows_new_comment() {
    GetPostByIdResponse response = rest.getForObject(BASE_URL + "posts/1", GetPostByIdResponse.class);
    assertThat(response.comments())
        .contains(new GetPostByIdResponse.CommentResponse(NEW_COMMENT, "user"));
  }
  @Test
  @Order(23)
  void create_comment_fails_for_locked_post() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth("user","user");
    HttpEntity<CreateCommentRequest> requestEntity = new HttpEntity<>(new CreateCommentRequest(NEW_COMMENT), headers);

    assertThatThrownBy(() -> rest.exchange(BASE_URL + "posts/2/comments", HttpMethod.POST, requestEntity, Void.class))
        .hasMessageContaining("Comment Rejected");
  }
}

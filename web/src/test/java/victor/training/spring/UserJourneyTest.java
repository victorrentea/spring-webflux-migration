package victor.training.spring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.api.CreateComment;
import victor.training.spring.api.GetAllAuthors.GetAuthorsResponse;
import victor.training.spring.api.GetAllPosts.GetPostsResponse;

import java.io.IOException;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static victor.training.spring.api.CreatePost.CreatePostRequest;
import static victor.training.spring.api.GetPostById.GetPostByIdResponse;
import static victor.training.spring.rabbit.RabbitSender.QUEUE_NAME;

@SuppressWarnings("DataFlowIssue")
@TestInstance(PER_CLASS)
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class UserJourneyTest {
  public static final String BASE_URL = "http://localhost:8080/";
  public static final String NEW_COMMENT = "new comment";
  // https://stackoverflow.com/questions/7952154/spring-resttemplate-how-to-enable-full-debugging-logging-of-requests-responses
  private final RestTemplate rest = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
  private final String createdPostTitle = "Title" + UUID.randomUUID();
  @Autowired
  RabbitTemplate rabbitTemplate;
  @Autowired
  RabbitAdmin admin;
  private int initialPostsCounts;

  @BeforeEach
  final void before() {
    rest.setErrorHandler(
        new DefaultResponseErrorHandler() {
          @Override
          public void handleError(ClientHttpResponse response) throws IOException {
            response.getBody().transferTo(System.err); // dump the server response text to serr to debug easier
            super.handleError(response);
          }
        }
    );
  }

  @Test
  @Order(1)
  void get_all_authors() {
    assertThat(rest.getForObject(BASE_URL + "authors", GetAuthorsResponse[].class))
        .contains(new GetAuthorsResponse(1000L, "John DOE", "jdoe@example.com", "Long description"));
  }

  @Test
  @Order(2)
  @Timeout(value = 99, unit = MILLISECONDS)
  void get_all_authors_again_is_faster_due_to_caching() {
    assertThat(rest.getForObject(BASE_URL + "authors", GetAuthorsResponse[].class))
        .contains(new GetAuthorsResponse(1000L, "John DOE", "jdoe@example.com", "Long description"));
  }

  @Test
  @Order(10)
  void get_all_posts() {
    GetPostsResponse[] posts = rest.getForObject(BASE_URL + "posts", GetPostsResponse[].class);
    initialPostsCounts = posts.length;
    assertThat(posts)
        .contains(new GetPostsResponse(1L, "Hello world!"))
        .contains(new GetPostsResponse(2L, "Locked Post"));
  }

  @Test
  @Order(11)
  void create_post() {
    admin.purgeQueue(QUEUE_NAME); // drain the queue
    rest.postForObject(BASE_URL + "posts", new CreatePostRequest(createdPostTitle, "Some Body", 15L), Void.class);
    Message receive = rabbitTemplate.receive(QUEUE_NAME, 500);
    assertThat(receive).describedAs("No message send to Rabbit").isNotNull();
    assertThat(new String(receive.getBody())).startsWith("Post created");
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
  void get_new_post_by_id() {
    assertThat(rest.getForObject(BASE_URL + "posts/1", GetPostByIdResponse.class))
        .extracting(GetPostByIdResponse::id, GetPostByIdResponse::title, GetPostByIdResponse::body)
        .containsExactly(1L, "Hello world!", "European Software Crafters");
  }

  @Test
  @Order(21)
  void create_comment() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth("user", "user");
    HttpEntity<CreateComment.CreateCommentRequest> requestEntity = new HttpEntity<>(new CreateComment.CreateCommentRequest(NEW_COMMENT), headers);
    rest.exchange(BASE_URL + "posts/1/comments", HttpMethod.POST, requestEntity, Void.class);
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
    headers.setBasicAuth("user", "user");
    HttpEntity<CreateComment.CreateCommentRequest> requestEntity = new HttpEntity<>(new CreateComment.CreateCommentRequest(NEW_COMMENT), headers);

    assertThatThrownBy(() -> rest.exchange(BASE_URL + "posts/2/comments", HttpMethod.POST, requestEntity, Void.class))
        .hasMessageContaining("Comment Rejected");
  }

  @AfterAll
  public void END() {
    System.err.println("⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️");
    System.err.println("** Please check the output of the deployed application **");
    System.err.println("⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️");
  }
}

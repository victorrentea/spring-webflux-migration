package victor.training.spring;

import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.api.UC1_GetAllAuthors.GetAuthorsResponse;
import victor.training.spring.api.UC2_GetAllPosts.GetPostsResponse;
import victor.training.spring.api.UC5_CreateComment.CreateCommentRequest;
import victor.training.spring.api.UC6_GetPostLikes;
import victor.training.spring.util.WaitForApp;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static victor.training.spring.api.UC3_GetPostDetails.GetPostDetailsResponse;
import static victor.training.spring.api.UC4_CreatePost.CreatePostRequest;

@SuppressWarnings("DataFlowIssue")
@TestInstance(PER_CLASS)
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public abstract class UserJourney {

  protected abstract String baseUrl();

  public static class Web extends UserJourney {
    protected String baseUrl() {
      return "http://localhost:8080/";
    }
  }

  public static class Flux extends UserJourney {
    protected String baseUrl() {
      return "http://localhost:8081/";
    }
  }

  public static final String NEW_COMMENT = "new text";
  // https://stackoverflow.com/questions/7952154/spring-resttemplate-how-to-enable-full-debugging-logging-of-requests-responses
  private final RestTemplate rest = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
  private final String createdPostTitle = "Title" + UUID.randomUUID();
  @Autowired
  RabbitTemplate rabbitTemplate;
  @Autowired
  RabbitAdmin admin;
  private int initialPostsCounts;
  private String currentTestName;

  @BeforeAll
  public void waitForAppToStart() {
    WaitForApp.waitForApp(baseUrl());
    rest.getInterceptors().add((request, body, execution) -> {
      request.getHeaders().add("test-name", currentTestName);
      return execution.execute(request, body);
    });
  }
  @BeforeEach
  final void before(TestInfo testInfo) {
    currentTestName = testInfo.getDisplayName();
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
  @NotNull
  private static HttpHeaders basicAuth() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth("user", "user");
    return headers;
  }

  @Test
  @Order(1)
  void uc1_get_all_authors() {
    assertThat(rest.getForObject(baseUrl() + "authors", GetAuthorsResponse[].class))
        .contains(new GetAuthorsResponse(1000L, "John DOE", "jdoe@example.com", "Long description"));
  }

  @Test
  @Order(100)
  @Timeout(value = 99, unit = MILLISECONDS)
  void uc2_get_all_authors_again_is_faster_due_to_caching() {
    assertThat(rest.getForObject(baseUrl() + "authors", GetAuthorsResponse[].class))
        .contains(new GetAuthorsResponse(1000L, "John DOE", "jdoe@example.com", "Long description"));
  }

  @Test
  @Order(10)
  void uc2_get_all_posts() {
    GetPostsResponse[] posts = rest.getForObject(baseUrl() + "posts", GetPostsResponse[].class);
    initialPostsCounts = posts.length;
    assertThat(posts)
        .contains(new GetPostsResponse(1L, "Hello world!"))
        .contains(new GetPostsResponse(2L, "Locked Post"));
  }

  @Test
  @Order(11)
  void uc4_create_post() {
    admin.purgeQueue("post-created-event"); // drain the queue

    HttpEntity<CreatePostRequest> requestEntity = new HttpEntity<>(
        new CreatePostRequest(createdPostTitle, "Some Body", 15L), basicAuth());
    rest.exchange(baseUrl() + "posts", HttpMethod.POST, requestEntity, Void.class);

    Message receive = rabbitTemplate.receive("post-created-event", 300);
    assertThat(receive).describedAs("No message send to Rabbit").isNotNull();
    assertThat(new String(receive.getBody())).contains("Post created");
  }

  @Test
  @Order(13)
  void uc3_get_post_details_fails_for_bad_id() {
    assertThatThrownBy(()-> rest.getForObject(baseUrl() + "posts/99999",
        GetPostDetailsResponse.class));
  }
  @Test
  @Order(14)
  void uc3_get_post_details() {
    GetPostDetailsResponse response = rest.getForObject(baseUrl() + "posts/1", GetPostDetailsResponse.class);
    assertThat(response)
        .extracting(GetPostDetailsResponse::id, GetPostDetailsResponse::title, GetPostDetailsResponse::body)
        .containsExactly(1L, "Hello world!", "European Software Crafters");
  }



  private String newPostId;
  @Test
  @Order(15)
  void uc4_get_posts_showsNewlyCreatedOne() {
    GetPostsResponse[] posts = rest.getForObject(baseUrl() + "posts", GetPostsResponse[].class);
    assertThat(posts).describedAs("The posts after creating a new one").hasSize(initialPostsCounts + 1);
    newPostId = Arrays.stream(posts).filter(p -> p.title().equals(createdPostTitle)).findAny()
        .map(GetPostsResponse::id)
        .orElseThrow().toString();
  }


  @Test
  @Order(16)
  void uc4_get_new_post_details_shows_initial_comment() {
    GetPostDetailsResponse response = rest.getForObject(baseUrl() + "posts/" + newPostId, GetPostDetailsResponse.class);
    assertThat(response.comments()).describedAs("Comments are missing at " + baseUrl() + "posts/" + newPostId).hasSize(1);
  }

  @Test
  @Order(17)
  void uc4_create_post_tx_failed() {
    HttpEntity<CreatePostRequest> requestEntity = new HttpEntity<>(
        new CreatePostRequest("x".repeat(254), "Some Body", 15L), basicAuth());
    assertThatThrownBy(()->rest.exchange(baseUrl() + "posts", HttpMethod.POST, requestEntity, Void.class));
    GetPostsResponse[] posts = rest.getForObject(baseUrl() + "posts", GetPostsResponse[].class);
    assertThat(posts).describedAs("No new post should have been created").hasSize(initialPostsCounts + 1);
  }

  @Test
  @Order(18)
  void uc4_get_new_post_details_showsLoggedInUser_in_comment() {
    GetPostDetailsResponse response = rest.getForObject(baseUrl() + "posts/" + newPostId, GetPostDetailsResponse.class);
    assertThat(response.comments().get(0).name()).isEqualTo("user");
  }

  @Test
  @Order(20)
  void uc5_create_comment_ok() {
    rest.postForObject(baseUrl() + "posts/1/comments", new CreateCommentRequest(NEW_COMMENT, "troll"), Void.class);
  }

  @Test
  @Order(22)
  void uc5_get_post_by_id_shows_new_comment() {
    GetPostDetailsResponse response = rest.getForObject(baseUrl() + "posts/1", GetPostDetailsResponse.class);
    assertThat(response.comments())
        .contains(new GetPostDetailsResponse.CommentResponse(NEW_COMMENT, "troll"));
  }

  @Test
  @Order(23)
  void uc5_create_comment_fails_for_locked_post() {
    assertThatThrownBy(() -> rest.postForObject(baseUrl() + "posts/2/comments", new CreateCommentRequest(NEW_COMMENT, "u"), Void.class))
        .hasMessageContaining("Comment Rejected");
  }

  @Test
  @Order(23)
  void uc5_create_comment_fails_for_nonexisting_postId() {
    assertThatThrownBy(() -> rest.postForObject(baseUrl() + "posts/119142/comments",new CreateCommentRequest(NEW_COMMENT, "troll"), Void.class))
        .isInstanceOf(HttpServerErrorException.InternalServerError.class);
  }


  @Test
  @Order(300)
  void uc6_getPostLikes() {
    UC6_GetPostLikes.LikeEvent event = new UC6_GetPostLikes.LikeEvent(1L, 1);
    rabbitTemplate.convertAndSend("likes", "likes.web", event);
    rabbitTemplate.convertAndSend("likes", "likes.flux", event);
    Awaitility.await().pollDelay(ofMillis(10)).pollDelay(ofMillis(10)).timeout(ofMillis(500))
            .untilAsserted(() -> assertThat(rest.getForObject(baseUrl() + "posts/1/likes", Integer.class)).isEqualTo(1));
  }

  @AfterAll
  public void END() {
    System.err.println("⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️");
    System.err.println("** Please check the output of the deployed application **");
    System.err.println("⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️");
  }
}

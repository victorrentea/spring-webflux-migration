package victor.training.spring;

import org.awaitility.Awaitility;
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
import victor.training.spring.api.UC1_GetAllAuthors.GetAuthorsResponse;
import victor.training.spring.api.UC2_GetAllPosts.GetPostsResponse;
import victor.training.spring.api.UC5_CreateComment.CreateCommentRequest;
import victor.training.spring.api.UC6_GetPostLikes;

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
import static victor.training.spring.rabbit.RabbitSender.QUEUE_NAME;

@SuppressWarnings("DataFlowIssue")
@TestInstance(PER_CLASS)
@DisplayNameGeneration(HumanReadableTestNames.class)
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class UserJourneyTest {
  public static final String BASE_URL = "http://localhost:8080/";
  public static final String NEW_COMMENT = "new text";
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

    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth("user", "user");
    HttpEntity<CreatePostRequest> requestEntity = new HttpEntity<>(new CreatePostRequest(createdPostTitle, "Some Body", 15L), headers);
    rest.exchange(BASE_URL + "posts", HttpMethod.POST, requestEntity, Void.class, headers);

    Message receive = rabbitTemplate.receive(QUEUE_NAME, 300);
    assertThat(receive).describedAs("No message send to Rabbit").isNotNull();
    assertThat(new String(receive.getBody())).contains("Post created");
  }

  @Test
  @Order(12)
  void get_post_details() {
    GetPostDetailsResponse response = rest.getForObject(BASE_URL + "posts/1", GetPostDetailsResponse.class);
    assertThat(response)
        .extracting(GetPostDetailsResponse::id, GetPostDetailsResponse::title, GetPostDetailsResponse::body)
        .containsExactly(1L, "Hello world!", "European Software Crafters");
  }

  private String newPostId;
  @Test
  @Order(15)
  void get_posts_showsNewlyCreatedOne() {
    GetPostsResponse[] posts = rest.getForObject(BASE_URL + "posts", GetPostsResponse[].class);
    assertThat(posts).hasSize(initialPostsCounts + 1);
    newPostId = Arrays.stream(posts).filter(p -> p.title().equals(createdPostTitle)).findAny()
        .map(GetPostsResponse::id)
        .orElseThrow().toString();
  }
  @Test
  @Order(17)
  void get_new_post_details_comments() {
    GetPostDetailsResponse response = rest.getForObject(BASE_URL + "posts/" + newPostId, GetPostDetailsResponse.class);
    assertThat(response.comments()).hasSize(1);
    assertThat(response.comments().get(0).name()).isEqualTo("user");
  }

  @Test
  @Order(21)
  void create_comment() {
    rest.postForObject(BASE_URL + "posts/1/comments", new CreateCommentRequest(NEW_COMMENT, "troll"), Void.class);
  }

  @Test
  @Order(22)
  void get_post_by_id_shows_new_comment() {
    GetPostDetailsResponse response = rest.getForObject(BASE_URL + "posts/1", GetPostDetailsResponse.class);
    assertThat(response.comments())
        .contains(new GetPostDetailsResponse.CommentResponse(NEW_COMMENT, "troll"));
  }

  @Test
  @Order(23)
  void create_comment_fails_for_locked_post() {
    assertThatThrownBy(() -> rest.postForObject(BASE_URL + "posts/2/comments", new CreateCommentRequest(NEW_COMMENT, "u"), Void.class))
        .hasMessageContaining("Comment Rejected");
  }



  @Test
  @Order(30)
  void getPostLikes() {
    UC6_GetPostLikes.LikeEvent event = new UC6_GetPostLikes.LikeEvent(1L, 1);
    rabbitTemplate.convertAndSend("likes", "likes.app", event);
    Awaitility.await().pollDelay(ofMillis(100)).pollDelay(ofMillis(50)).timeout(ofMillis(500))
            .until(() -> rest.getForObject(BASE_URL + "posts/1/likes", Integer.class) == 1);
  }

  @AfterAll
  public void END() {
    System.err.println("⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️");
    System.err.println("** Please check the output of the deployed application **");
    System.err.println("⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️");
  }
}

package victor.training.spring.lib;

import org.springframework.web.client.RestClient;

// Pretend you can't edit this class to make it non-blocking
// In practice, Hibernate is king of blocking libs => 👍don't use it in webflux apps
public class BlockingLib {
  public static boolean isSafe(String body, String comment) {
    record Request(String body, String comment) {
    }
    String response = RestClient.create().post()
        .uri("http://localhost:9999/safety-check")
        .body(new Request(body, comment))
        .retrieve()
        .body(String.class);
    return "OK".equals(response);
  }
}

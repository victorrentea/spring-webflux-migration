package victor.training.spring.util;

import org.awaitility.Awaitility;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

public class WaitForApp {
  public static void waitForApp(String baseUrl) {
    System.out.print("Wait for app to become available ");
    Awaitility.await()
        .pollDelay(ofMillis(100))
        .timeout(ofSeconds(2))
        .pollInterval(ofMillis(50))
        .untilAsserted(()->springBootActuatorUP(baseUrl));
    System.out.println(" UP🎉");
  }

  private static void springBootActuatorUP(String baseUrl) {
    try {
      System.out.printf(".");
      RestTemplate restTemplate = new RestTemplate();
      Map<String, Object> responseMap = restTemplate.getForObject(baseUrl + "/actuator/health", Map.class);
      if (!responseMap.get("status").equals("UP")) {
        throw new AssertionError("Not started yet: " + responseMap);
      }
    } catch (RestClientException e) {
      throw new AssertionError(e);
    }
  }
}

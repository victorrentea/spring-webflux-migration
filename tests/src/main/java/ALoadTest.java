import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static java.time.Duration.ofSeconds;

public class ALoadTest extends Simulation {
  public static void main(String[] args) {
    GatlingEngine.startClass(ALoadTest.class);
  }

  {
    setUp(scenario(getClass().getSimpleName()).exec(http("").get("/"))
            .injectClosed(constantConcurrentUsers(180).during(ofSeconds(5))))
            .protocols(http.baseUrl("http://localhost:8081"));
  }
}

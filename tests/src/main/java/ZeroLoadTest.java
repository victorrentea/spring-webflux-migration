import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static java.time.Duration.ofSeconds;

public class ZeroLoadTest extends Simulation {
  public static void main(String[] args) {
    GatlingEngine.startClass(ZeroLoadTest.class);
  }

  {
    setUp(scenario(getClass().getSimpleName()).exec(http("").get("/0"))
            .injectClosed(constantConcurrentUsers(200).during(ofSeconds(5))))
            .protocols(http.baseUrl("http://localhost:8080"));
  }
}

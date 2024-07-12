package mockapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
@RestController
public class MockApi {
  public static void main(String[] args) {
    SpringApplication.run(MockApi.class, "--server.port=9999");
  }

  @GetMapping("/10ms/**")
  public CompletableFuture<String> ten() {
    return supplyAsync(()->"OK", delayedExecutor(10, MILLISECONDS));
  }

  @GetMapping("/100ms/**")
  public CompletableFuture<String> hundred() {
    return supplyAsync(()->"OK", delayedExecutor(100, MILLISECONDS));
  }

}

package completable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class D {
  private final WebClient webClient;

  @GetMapping("/d")
  public CompletableFuture<String> d() {
    return null; // TODO
  }

  private CompletableFuture<String> d1() {
    return webClient.get().uri("http://localhost:9999/10ms")
        .retrieve()
        .bodyToMono(String.class)
        .toFuture();
  }
  private CompletableFuture<String> d2(String d1) {
    return webClient.get().uri("http://localhost:9999/10ms/"+d1)
        .retrieve()
        .bodyToMono(String.class)
        .toFuture();
  }
  private CompletableFuture<String> d3(String d1) {
    return webClient.get().uri("http://localhost:9999/10ms/"+d1)
        .retrieve()
        .bodyToMono(String.class)
        .toFuture();
  }
}

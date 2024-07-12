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
public class C {
  private final WebClient webClient;

  @GetMapping("/c")
  public CompletableFuture<String> c() {
    return c1().thenCombine(c2(), (c1, c2) -> c1 + c2);
  }

  private CompletableFuture<String> c1() {
    return webClient.get().uri("http://localhost:9999/10ms")
        .retrieve()
        .bodyToMono(String.class)
        .toFuture();
  }
  private CompletableFuture<String> c2() {
    return webClient.get().uri("http://localhost:9999/10ms/")
        .retrieve()
        .bodyToMono(String.class)
        .toFuture();
  }
}

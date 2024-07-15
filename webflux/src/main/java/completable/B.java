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
public class B {
  private final WebClient webClient;

  @GetMapping("/b")
  public CompletableFuture<String> b() {
    return b1().thenCompose(b1-> b2(b1));
  }

  private CompletableFuture<String> b1() {
    return webClient.get().uri("http://localhost:9999/10ms")
        .retrieve()
        .bodyToMono(String.class)
        .toFuture();
  }
  private CompletableFuture<String> b2(String b1) {
    return webClient.get().uri("http://localhost:9999/10ms/"+b1)
        .retrieve()
        .bodyToMono(String.class)
        .toFuture();
  }
}

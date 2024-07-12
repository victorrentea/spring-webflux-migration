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
public class A {
  private final WebClient webClient;

  @GetMapping("/a")
  public CompletableFuture<String> a() {
    return webClient.get().uri("http://localhost:9999/100ms")
        .retrieve()
        .bodyToMono(String.class)
        .toFuture();
  }

}

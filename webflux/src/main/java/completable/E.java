package completable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class E {
  private Map<String, CompletableFuture<String>> pendingRequests = new HashMap<>();

  @GetMapping("/e")
  public CompletableFuture<String> e() {
    String requestId = UUID.randomUUID().toString();
    CompletableFuture<String> cf = new CompletableFuture<>();
    pendingRequests.put(requestId, cf);
    return cf;
  }

  @Scheduled
  public void imagineCallbackForAll() {
    pendingRequests.forEach((requestId, cf) -> {
      cf.complete("Hello, " + requestId);
    });
  }


}

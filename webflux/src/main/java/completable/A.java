package completable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class A {
  private final WebClient webClient;

  @GetMapping("/0")
  public String blocant() {
//    return webClient.get().uri("http://localhost:9999/100ms")
//        .retrieve()
//        .bodyToMono(String.class)
//        .block();
    return new RestTemplate().getForObject("http://localhost:9999/100ms", String.class);
    // tine blocat .5M pe durata requestului
  }

  @GetMapping("/a")
  public CompletableFuture<String> a() {
    String requestId = UUID.randomUUID().toString();
    log.info("HTTP server: netty " + requestId);
    long t0 = System.currentTimeMillis();
    var promise = webClient.get()
        .uri("http://localhost:9999/100ms")
        .retrieve()
        .bodyToMono(String.class)
        .toFuture(); // agatam un callback pentru cand vine raspunsul pe TCP
    long t1 = System.currentTimeMillis();
    log.info("am tinut blocat threadul lui Tomcat pt {} ms", t1 - t0);
    promise.thenAccept(date -> {
      log.info("au venit datele " + date + " pt " + requestId);
    });
    return promise;
  } // ii dau lui spring un promise de care poate sa se agate sa
  // scrie raspunsul inapoi catre client


  // explica cum functioneaza codul de mai sus.
  // scrie-mi o metoda echivalenta fara CompletableFuture



}

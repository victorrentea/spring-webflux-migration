package completable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
@RestController
public class CompletableApp {
  public static void main(String[] args) {
    SpringApplication.run(CompletableApp.class, args);
  }
  @Bean
  public WebClient webClient() {
    return WebClient.create();
  }

}

package victor.training.spring.flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class FluxApp {
  public static void main(String[] args) {
    SpringApplication.run(FluxApp.class, args);
  }

  @Bean
  public WebClient webClient() {
    return WebClient.create();
  }

  @RestControllerAdvice
  static class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle(Exception e) {
      e.printStackTrace();
      return e.getMessage();
    }
  }
}

package victor.training.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

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

  @Slf4j
  @RestControllerAdvice
  static class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle(Exception e) {
      log.error("Error: " + e, e);
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      return sw.toString(); // security breach! Don't do this in your project. It's just for easier debugging
    }
  }

  static {  // ⭐️⭐️⭐️ Detection of blocking code in a reactive application:
    // ⚠️ add to startup VM options: -XX:+AllowRedefinitionToAddDeleteMethods
//    BlockHound.builder()
//        .allowBlockingCallsInside("io.r2dbc.postgresql.authentication.SASLAuthenticationHandler", "handleAuthenticationSASL")
//        .install();
  }

  @RestController
  public static class RedirectRootToSwaggerUI {
    @GetMapping
    public ResponseEntity<Void> redirectRootToSwagger() {
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(URI.create("/swagger-ui.html"));
      return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
  }
}

package victor.training.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

@EnableCaching
@SpringBootApplication
public class WebApp {
  public static void main(String[] args) {
    SpringApplication.run(WebApp.class, args);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

//  @Bean
//  public WebClient webClient() {
//    return WebClient.create();
//  }

  @Slf4j
  @RestControllerAdvice
  static class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle(HttpRequest request, Exception e) {
      log.error("Error at " + request.getMethod() + " " + request.getURI() + ": " + e, e);
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      return sw.toString(); // security breach! Don't do this in your project. It's just for easier debugging
    }
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

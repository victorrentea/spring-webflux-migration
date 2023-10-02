package victor.training.spring;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

@EnableCaching // TODO REMOVE
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

  @RestController
  public static class RedirectRootToSwaggerUI {
    @GetMapping
    public ResponseEntity<Void> redirectRootToSwagger() {
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(URI.create("/swagger-ui.html"));
      return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
  }

  @Slf4j
  @Component
  public static class RequestLogger implements WebFilter {
    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
      log.info("▶️" + serverWebExchange.getRequest().getMethod() + " " + serverWebExchange.getRequest().getURI().getPath());

      return webFilterChain.filter(serverWebExchange);
    }
  }
}

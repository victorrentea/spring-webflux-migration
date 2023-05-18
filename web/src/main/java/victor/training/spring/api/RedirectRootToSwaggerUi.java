package victor.training.spring.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectRootToSwaggerUi {
  @GetMapping
  public ResponseEntity<Void> redirectRootToSwagger() {
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create("/swagger-ui.html"));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }
}

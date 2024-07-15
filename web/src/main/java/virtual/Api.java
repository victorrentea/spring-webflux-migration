package virtual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class Api {

  @GetMapping
  public String method() throws InterruptedException {
    Thread.sleep(500);
    return "Here in " + Thread.currentThread().isVirtual();
  }
}

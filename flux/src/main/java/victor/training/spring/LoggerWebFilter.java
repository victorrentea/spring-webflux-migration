package victor.training.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggerWebFilter implements WebFilter {
  @Override
  public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
    log.info("▶️" + serverWebExchange.getRequest().getMethod() + " " + serverWebExchange.getRequest().getURI().getPath() + " 👉 " + serverWebExchange.getRequest().getHeaders().get("test-name"));
    return webFilterChain.filter(serverWebExchange);
  }
}
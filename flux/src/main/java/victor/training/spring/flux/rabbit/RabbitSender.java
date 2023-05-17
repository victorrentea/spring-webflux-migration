package victor.training.spring.flux.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RabbitSender {
  private final Sender sender;

  public Mono<Void> sendMessage(String payload) {
    log.info("Sending message: " + payload);
    OutboundMessage outboundMessage = new OutboundMessage("", "rabbitqueue", payload.getBytes());
    return sender.sendWithPublishConfirms(Flux.just(outboundMessage)).then()
        .doOnNext(e -> log.info("Sent"));
  }


}

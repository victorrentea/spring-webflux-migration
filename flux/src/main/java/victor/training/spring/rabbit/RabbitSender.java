package victor.training.spring.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RabbitSender {
  private final Sender sender;

  public Mono<Void> sendPostCreatedEvent(String payload) {
    OutboundMessage outboundMessage = new OutboundMessage("", "post-created-event", payload.getBytes());
    return sender.sendWithPublishConfirms(Mono.just(outboundMessage))
        .doOnComplete(()->log.info("Sent"))
        .then()
        .doOnSubscribe(e-> log.info("Sending message: " + payload))
        ;
  }

  @Bean
  public Queue rabbitqueue() {
    return new Queue("post-created-event", false);
  }
}

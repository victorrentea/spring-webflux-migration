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
  public static final String POST_CREATED_EVENT = "post-created-event";
  private final Sender sender;

  public Mono<Void> sendPostCreatedEvent(String message) {
    log.info("Sending message: " + message);
    OutboundMessage outboundMessage = new OutboundMessage("", POST_CREATED_EVENT, message.getBytes());
    return sender.sendWithPublishConfirms(Mono.just(outboundMessage))
        .doOnSubscribe(s -> log.info("sending " + message) )
        .doOnComplete(() -> log.info("completed " + message))
        .doOnNext(m -> log.info("sent " + m)).then();
  }

  @Bean
  public Queue myQueue() { // create queue at startup
    return new Queue(POST_CREATED_EVENT, false);
  }
}

package victor.training.spring.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RabbitSender {
  public static final String POST_CREATED_EVENT = "post-created-event";
  private final RabbitTemplate rabbitTemplate;

  public void sendPostCreatedEvent(String message) {
    log.info("Sending message: " + message);
    rabbitTemplate.convertAndSend(POST_CREATED_EVENT, message);
  }

  @Bean // create queue at startup
  public Queue myQueue() { // create queue at startup
    return new Queue(POST_CREATED_EVENT, false);
  }
}

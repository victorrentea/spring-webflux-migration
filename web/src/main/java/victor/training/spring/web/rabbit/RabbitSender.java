package victor.training.spring.web.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RabbitSender {
  private final RabbitTemplate rabbitTemplate;
  public void sendMessage(String message) {
    log.info("Sending message: " + message);
    rabbitTemplate.convertAndSend("rabbitqueue", message);
    log.info("Sent");
  }

  @Bean
  public Queue myQueue() {
    return new Queue("rabbitqueue", false);
  }
}

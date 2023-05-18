package victor.training.spring.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static java.lang.Thread.sleep;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RabbitSender {
  private final RabbitTemplate rabbitTemplate;
  @SneakyThrows
  public void sendMessage(String message) {
    log.info("Sending message: " + message);
    sleep(10);
    rabbitTemplate.convertAndSend("rabbitqueue", message);
    sleep(10);
    log.info("Sent");
  }

  @Bean
  public Queue myQueue() {
    return new Queue("rabbitqueue", false);
  }
}

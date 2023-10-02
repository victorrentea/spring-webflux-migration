package victor.training.spring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static reactor.core.publisher.Mono.just;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class LikesAutoSender {
  private final Sender sender;
  private static final AtomicInteger counter = new AtomicInteger(1);
  @Scheduled(fixedRate = 300)
  public void sendPostCreatedEvent() {
    long postId = new Random().nextInt(5) + 2;
    String json = """
        {"postId":%d, "likes":%d}""".formatted(postId, counter.addAndGet(new Random().nextInt(100)));
    OutboundMessage message = new OutboundMessage("", "likes.flux", json.getBytes());
    sender.sendWithPublishConfirms(just(message)).blockLast(); // OK to block in scheduler
  }

}

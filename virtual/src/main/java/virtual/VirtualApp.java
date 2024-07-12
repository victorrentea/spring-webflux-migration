package virtual;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@RestController
public class VirtualApp {
  public static void main(String[] args) {
    SpringApplication.run(VirtualApp.class, "--server.port=8081");
  }
  @GetMapping("/a-vechi")
  public void aVechi(HttpServletRequest request) {
    System.out.println("Here");
     long t0 = System.currentTimeMillis();
     AsyncContext asyncContext = request.startAsync();
     CompletableFuture.runAsync(() -> {
       try {
         Thread.sleep(1000);
         asyncContext.getResponse().getWriter().write("Hello, world dupa 1 sec!");
         asyncContext.complete(); // inchid req cu clientu
       } catch (Exception e) {
         e.printStackTrace();
       }
     });
     long t1 = System.currentTimeMillis();
     System.out.println("a-vechi: " + (t1 - t0));
   }
}

package victor.training.spring.webtoflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClassicDependencies {

  public String mongoRepoSave(String document) {
    log.info("Persist the document: {}", document);
    return document;
  }

  public void rabbitSend(String message) {
//    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    String currentUsername = "jdoe"; // from current thread in Spring Web
    log.info("Pretend send message on 🐇: {} by {}", message, currentUsername);
  }

  public ReaderProfile fetchUserProfile(Long readerId) {
    log.info("Rest call PROFILE: {}", readerId);
    return new ReaderProfile();
  }

  public boolean checkAddress(ReaderProfile readerProfile) {
    log.info("Rest call ADDRESS: {}", readerProfile);
    return true;
  }

  public boolean checkPhone(ReaderProfile readerProfile) {
    log.info("Rest call PHONE: {}", readerProfile);
    return true;
  }

  public String wsdlCall(String data) {
    return LegacyLib.blockingCall(data);
  }
}
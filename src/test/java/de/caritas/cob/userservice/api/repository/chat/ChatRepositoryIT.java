package de.caritas.cob.userservice.api.repository.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import java.time.LocalDateTime;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ChatRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  @Autowired
  private ChatRepository underTest;

  @Autowired
  private ConsultantRepository consultantRepository;

  private Consultant consultant;

  private Chat chat;

  @AfterEach
  public void restore() {
    underTest.deleteById(chat.getId());
    chat = null;
    consultant = null;
  }

  @Test
  public void saveShouldSaveChat() {
    givenAConsultant();
    givenAValidChat();

    var persistedChat = underTest.save(chat);

    var foundOptionalChat = underTest.findById(persistedChat.getId());
    assertTrue(foundOptionalChat.isPresent());
    var foundChat = foundOptionalChat.get();
    assertEquals(chat.isRepetitive(), foundChat.isRepetitive());
    assertEquals(chat.isActive(), foundChat.isActive());
  }

  private void givenAValidChat() {
    chat = new Chat();
    chat.setTopic(RandomStringUtils.randomAlphanumeric(1, 255));
    chat.setConsultingTypeId(1);
    chat.setInitialStartDate(LocalDateTime.now());
    chat.setStartDate(easyRandom.nextObject(LocalDateTime.class));
    chat.setDuration(easyRandom.nextInt());
    chat.setChatOwner(consultant);
  }

  private void givenAConsultant() {
    consultant = consultantRepository.findAll().iterator().next();
  }
}

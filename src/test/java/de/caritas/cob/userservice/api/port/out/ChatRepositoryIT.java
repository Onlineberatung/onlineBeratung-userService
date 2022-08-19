package de.caritas.cob.userservice.api.port.out;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
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
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ChatRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  @Autowired private ChatRepository underTest;

  @Autowired private ConsultantRepository consultantRepository;

  private Consultant consultant;

  private Chat chat;

  @AfterEach
  public void restore() {
    if (nonNull(chat)) {
      underTest.deleteById(chat.getId());
      chat = null;
      consultant = null;
    }
  }

  @Test
  @Sql(value = "/database/chatAndRelationData.sql")
  void findAssignedByUserId_Should_FindAllDirectAssignedChats() {
    String userId = "015d013d-95e7-4e91-85b5-12cdb3d317f3";

    var assignedChats = underTest.findAssignedByUserId(userId);

    assertEquals(2, assignedChats.size());
    assertEquals(0, assignedChats.get(0).getId());
    assertEquals(1, assignedChats.get(1).getId());
  }

  @Test
  @Sql(value = "/database/chatAndRelationData.sql")
  void findByUserId_Should_FindAllChatWithChatAgencyRelation() {
    String userId = "017cac2a-2086-47eb-9f8e-40547dfa2fd5";

    var chats = underTest.findByUserId(userId);

    assertEquals(1, chats.size());
    assertEquals(2, chats.get(0).getId());
  }

  @Test
  void saveShouldSaveChat() {
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

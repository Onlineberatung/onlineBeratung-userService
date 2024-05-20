package de.caritas.cob.userservice.api.port.out;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.caritas.cob.userservice.api.helper.CustomLocalDateTime;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserChat;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = "spring.profiles.active=testing")
@ActiveProfiles("testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class UserChatRepositoryTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @Autowired UserChatRepository chatUserRepository;

  @Autowired ChatRepository chatRepository;

  @Autowired UserRepository userRepository;

  @Autowired ConsultantRepository consultantRepository;

  @Test
  void save_Should_saveUserChatRelation() {
    // given
    var chat = givenChat();
    var user = givenUser();

    // when
    var chatUser = chatUserRepository.save(UserChat.builder().user(user).chat(chat).build());

    // then
    assertThat(chat).isNotNull();
    assertThat(chat.getId()).isNotNull();
    assertThat(chatUser.getUser()).isEqualTo(user);
    assertThat(chatUser.getChat()).isEqualTo(chat);
  }

  @Test
  void findByChatAndUser_Should_findChatUserCombination() {
    // given
    var chat = givenChat();
    var user = givenUser();
    var chatUser = chatUserRepository.save(UserChat.builder().user(user).chat(chat).build());

    // when
    var findByChatAndUser = chatUserRepository.findByChatAndUser(chat, user).orElseThrow();

    // then
    assertThat(findByChatAndUser.getId()).isEqualTo(chatUser.getId());
    assertThat(findByChatAndUser.getUser()).isEqualTo(chatUser.getUser());
    assertThat(findByChatAndUser.getChat()).isEqualTo(chatUser.getChat());
  }

  private Chat givenChat() {
    Chat chat = easyRandom.nextObject(Chat.class);
    chat.setId(null);
    chat.setActive(true);
    chat.setRepetitive(true);
    chat.setChatOwner(givenConsultant());
    chat.setConsultingTypeId(easyRandom.nextInt(128));
    chat.setDuration(easyRandom.nextInt(32768));
    chat.setMaxParticipants(easyRandom.nextInt(128));
    chat.setUpdateDate(CustomLocalDateTime.nowInUtc());
    return chatRepository.save(chat);
  }

  private User givenUser() {
    return userRepository.findById("015d013d-95e7-4e91-85b5-12cdb3d317f3").orElseThrow();
  }

  private Consultant givenConsultant() {
    return consultantRepository.findById("0b3b1cc6-be98-4787-aa56-212259d811b9").orElseThrow();
  }
}

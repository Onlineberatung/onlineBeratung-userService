package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserChat;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserChatRepository extends CrudRepository<UserChat, Long> {

  Optional<UserChat> findByChatAndUser(Chat chat, User user);
}

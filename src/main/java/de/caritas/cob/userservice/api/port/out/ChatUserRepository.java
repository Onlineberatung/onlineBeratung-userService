package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatUser;
import de.caritas.cob.userservice.api.model.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ChatUserRepository extends CrudRepository<ChatUser, Long> {

  Optional<ChatUser> findByChatAndUser(Chat chat, User user);

}

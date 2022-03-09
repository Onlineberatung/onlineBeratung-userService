package de.caritas.cob.userservice.api.port.in;

import de.caritas.cob.userservice.api.model.Chat;
import java.util.Map;
import java.util.Optional;

public interface Messaging {

  boolean banUserFromChat(String consultantId, String adviceSeekerId, long chatId);

  boolean existsChat(long id);

  Optional<Chat> findChat(long id);

  Optional<Map<String, Object>> findChatMetaInfo(long chatId, String userId);
}

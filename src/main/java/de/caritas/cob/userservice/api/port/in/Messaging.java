package de.caritas.cob.userservice.api.port.in;

import de.caritas.cob.userservice.api.model.Chat;
import java.util.Map;
import java.util.Optional;

public interface Messaging {

  boolean banUserFromChat(String adviceSeekerId, long chatId);

  boolean existsChat(long id);

  Optional<Chat> findChat(long id);

  Optional<Map<String, Object>> findChatMetaInfo(long chatId, String userId);

  void unbanUsersInChat(Long chatId, String id);

  Boolean updateE2eKeys(String chatUserId, String publicKey);

  boolean removeUserFromSession(String chatUserId, String chatId);

  Optional<Map<String, String>> findSession(Long sessionId);

  boolean isInChat(String chatId, String chatUserId);

  boolean markAsDirectConsultant(Long sessionId);
}

package de.caritas.cob.userservice.api.port.out;

import java.util.Map;
import java.util.Optional;

public interface MessageClient {

  boolean muteUserInChat(String consultantId, String username, String chatId);

  Optional<Map<String, Object>> getChatInfo(String chatId, String userId);

  boolean unmuteUserInChat(String consultantId, String chatUserId, String chatId);
}

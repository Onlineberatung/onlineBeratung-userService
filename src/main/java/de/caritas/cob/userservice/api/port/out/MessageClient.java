package de.caritas.cob.userservice.api.port.out;

import java.util.Map;
import java.util.Optional;

public interface MessageClient {

  boolean muteUserInChat(String username, String chatId);

  Optional<Map<String, Object>> getChatInfo(String chatId);

  boolean unmuteUserInChat(String chatUserId, String chatId);

  boolean updateUser(String chatUserId, String displayName);
}

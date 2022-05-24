package de.caritas.cob.userservice.api.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MessageClient {

  boolean muteUserInChat(String username, String chatId);

  Optional<Map<String, Object>> getChatInfo(String chatId);

  boolean unmuteUserInChat(String chatUserId, String chatId);

  boolean updateUser(String chatUserId, String displayName);

  Optional<Map<String, Object>> findUser(String chatUserId);

  Optional<List<Map<String, String>>> findAllChats(String chatUserId);

  boolean updateChatE2eKey(String chatUserId, String roomId, String key);

  boolean removeUserFromSession(String chatUserId, String... chatIds);
}

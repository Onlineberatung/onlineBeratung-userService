package de.caritas.cob.userservice.api.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface MessageClient {

  boolean muteUserInChat(String username, String chatId);

  Optional<Map<String, Object>> getChatInfo(String chatId);

  @SuppressWarnings("UnusedReturnValue")
  boolean unmuteUserInChat(String chatUserId, String chatId);

  @SuppressWarnings("UnusedReturnValue")
  boolean updateUser(String chatUserId, String displayName);

  Optional<Boolean> isLoggedIn(String chatUserId);

  Set<String> findAllAvailableUserIds();

  Optional<Boolean> isAvailable(String chatUserId);

  boolean setUserPresence(String username, String status);

  Optional<Map<String, Object>> findUser(String chatUserId);

  Optional<Map<String, Object>> findUserAndAddToCache(String chatUserId);

  Optional<List<Map<String, String>>> findAllChats(String chatUserId);

  boolean updateChatE2eKey(String chatUserId, String roomId, String key);

  boolean removeUserFromSession(String chatUserId, String chatId);

  Optional<List<Map<String, String>>> findMembers(String chatId);
}

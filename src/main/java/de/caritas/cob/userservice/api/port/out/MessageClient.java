package de.caritas.cob.userservice.api.port.out;

import java.util.Map;
import java.util.Optional;

public interface MessageClient {

  boolean muteUserInRoom(String consultantId, String username, String roomId);

  Optional<Map<String, Object>> getChatInfo(String groupId, String userId);
}

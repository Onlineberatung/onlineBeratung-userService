package de.caritas.cob.userservice.api.port.out;

public interface MessageClient {

  boolean muteUserInRoom(String consultantId, String username, String roomId);
}

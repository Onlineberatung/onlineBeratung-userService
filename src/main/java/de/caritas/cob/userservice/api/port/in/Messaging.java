package de.caritas.cob.userservice.api.port.in;

public interface Messaging {

  boolean banUserFromChat(String consultantId, String adviceSeekerId, long chatId);
}

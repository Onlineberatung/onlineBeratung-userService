package de.caritas.cob.userservice.api.adapters.rocketchat.model;

import java.time.Instant;
import java.util.Date;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rocketchat_sessions")
@Data
public class RocketchatSession {
  private Instant closedAt;

  private String sessionId;

  private String userId;

  public RocketchatSession(org.bson.Document document) {
    this.sessionId = document.getString("sessionId");
    this.userId = document.getString("userId");
    Date closedAtNullable = document.getDate("closedAt");
    this.closedAt = closedAtNullable != null ? closedAtNullable.toInstant() : null;
  }
}

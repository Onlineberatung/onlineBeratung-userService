package de.caritas.cob.userservice.api.model;

import java.time.Instant;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rocketchat_sessions")
@Data
public class RocketchatSession {
  private Instant closedAt;

  private String sessionId;

  private String userId;
}

package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.RocketchatSession;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

@Profile("!test")
public interface MongoRocketchatSessionRepository
    extends RocketchatSessionRepository, MongoRepository<RocketchatSession, String> {

  @Query(value = "{'lastActivityAt': { $lt: ?0 }, 'closedAt': null }")
  List<RocketchatSession> findInactiveSessions();
}

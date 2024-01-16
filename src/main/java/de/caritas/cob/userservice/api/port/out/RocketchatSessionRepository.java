package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.RocketchatSession;
import java.util.List;

public interface RocketchatSessionRepository {

  List<RocketchatSession> findInactiveSessions();

  RocketchatSession save(RocketchatSession rocketchatSession);
}

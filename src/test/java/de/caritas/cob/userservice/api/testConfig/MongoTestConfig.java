package de.caritas.cob.userservice.api.testConfig;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.model.RocketchatSession;
import de.caritas.cob.userservice.api.port.out.RocketchatSessionRepository;
import java.util.ArrayList;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class MongoTestConfig {

  public RocketchatSessionRepository getRocketchatSessionRepository() {
    return new RocketchatSessionRepository() {
      @Override
      public ArrayList findInactiveSessions() {
        return Lists.newArrayList();
      }

      @Override
      public RocketchatSession save(RocketchatSession rocketchatSession) {
        return rocketchatSession;
      }
    };
  }
}

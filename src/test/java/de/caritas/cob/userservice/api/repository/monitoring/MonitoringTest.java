package de.caritas.cob.userservice.api.repository.monitoring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.Test;

public class MonitoringTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    Monitoring monitoring = new EasyRandom().nextObject(Monitoring.class);

    assertThat(monitoring, is(monitoring));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoMonitoringInstance() {
    Monitoring monitoring = new EasyRandom().nextObject(Monitoring.class);

    boolean equals = monitoring.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_monitoringSessionIdsAreDifferent() {
    Monitoring monitoring = new EasyRandom().nextObject(Monitoring.class);
    monitoring.setSessionId(1L);
    Monitoring otherMonitoring = new EasyRandom().nextObject(Monitoring.class);
    otherMonitoring.setSessionId(2L);

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_monitoringTypesAreDifferent() {
    Monitoring monitoring = new EasyRandom().nextObject(Monitoring.class);
    monitoring.setMonitoringType(MonitoringType.ADDICTIVE_DRUGS);
    Monitoring otherMonitoring = new EasyRandom().nextObject(Monitoring.class);
    otherMonitoring.setMonitoringType(MonitoringType.INTERVENTION);

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_monitoringKeysAreDifferent() {
    Monitoring monitoring = new EasyRandom().nextObject(Monitoring.class);
    monitoring.setKey("Key");
    Monitoring otherMonitoring = new EasyRandom().nextObject(Monitoring.class);
    otherMonitoring.setKey("Other Key");

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_sessionIdAndMonitoringTypeAndKeyIsEqual() {
    Monitoring monitoring = new EasyRandom().nextObject(Monitoring.class);
    Monitoring otherMonitoring = new EasyRandom().nextObject(Monitoring.class);
    otherMonitoring.setSessionId(monitoring.getSessionId());
    otherMonitoring.setMonitoringType(monitoring.getMonitoringType());
    otherMonitoring.setKey(monitoring.getKey());

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(true));
  }

}

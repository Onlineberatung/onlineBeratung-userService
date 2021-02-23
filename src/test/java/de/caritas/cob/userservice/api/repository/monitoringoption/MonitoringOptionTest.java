package de.caritas.cob.userservice.api.repository.monitoringoption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import org.jeasy.random.EasyRandom;
import org.junit.Test;

public class MonitoringOptionTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    MonitoringOption monitoring = new EasyRandom().nextObject(MonitoringOption.class);

    assertThat(monitoring, is(monitoring));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoMonitoringInstance() {
    MonitoringOption monitoring = new EasyRandom().nextObject(MonitoringOption.class);

    boolean equals = monitoring.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_monitoringSessionIdsAreDifferent() {
    MonitoringOption monitoring = new EasyRandom().nextObject(MonitoringOption.class);
    monitoring.setSessionId(1L);
    MonitoringOption otherMonitoring = new EasyRandom().nextObject(MonitoringOption.class);
    otherMonitoring.setSessionId(2L);

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_monitoringTypesAreDifferent() {
    MonitoringOption monitoring = new EasyRandom().nextObject(MonitoringOption.class);
    monitoring.setMonitoringType(MonitoringType.ADDICTIVE_DRUGS);
    MonitoringOption otherMonitoring = new EasyRandom().nextObject(MonitoringOption.class);
    otherMonitoring.setMonitoringType(MonitoringType.INTERVENTION);

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_monitoringKeysAreDifferent() {
    MonitoringOption monitoring = new EasyRandom().nextObject(MonitoringOption.class);
    monitoring.setKey("Key");
    MonitoringOption otherMonitoring = new EasyRandom().nextObject(MonitoringOption.class);
    otherMonitoring.setKey("Other Key");

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_monitoringKeysNamesAreDifferent() {
    MonitoringOption monitoring = new EasyRandom().nextObject(MonitoringOption.class);
    monitoring.setMonitoringKey("monitoringKey");
    MonitoringOption otherMonitoring = new EasyRandom().nextObject(MonitoringOption.class);
    otherMonitoring.setMonitoringKey("Other monitoringKey");

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_sessionIdAndMonitoringTypeAndKeyAndMonitoringKeyIsEqual() {
    MonitoringOption monitoring = new EasyRandom().nextObject(MonitoringOption.class);
    MonitoringOption otherMonitoring = new EasyRandom().nextObject(MonitoringOption.class);
    otherMonitoring.setSessionId(monitoring.getSessionId());
    otherMonitoring.setMonitoringType(monitoring.getMonitoringType());
    otherMonitoring.setKey(monitoring.getKey());
    otherMonitoring.setMonitoringKey(monitoring.getMonitoringKey());

    boolean equals = monitoring.equals(otherMonitoring);

    assertThat(equals, is(true));
  }

}

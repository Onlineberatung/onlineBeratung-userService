package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

public class SessionDataTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    SessionData sessionData = new EasyRandom().nextObject(SessionData.class);

    assertThat(sessionData, is(sessionData));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoSessionDataInstance() {
    SessionData sessionData = new EasyRandom().nextObject(SessionData.class);

    boolean equals = sessionData.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_sessionIdsAreDifferent() {
    SessionData sessionData = new EasyRandom().nextObject(SessionData.class);
    sessionData.setId(1L);
    SessionData otherSessionData = new EasyRandom().nextObject(SessionData.class);
    otherSessionData.setId(2L);

    boolean equals = sessionData.equals(otherSessionData);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_sessionIdsAreEqual() {
    SessionData sessionData = new EasyRandom().nextObject(SessionData.class);
    sessionData.setId(1L);
    SessionData otherSessionData = new EasyRandom().nextObject(SessionData.class);
    otherSessionData.setId(1L);

    boolean equals = sessionData.equals(otherSessionData);

    assertThat(equals, is(true));
  }
}

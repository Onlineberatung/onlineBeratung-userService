package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

public class SessionTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    Session session = new EasyRandom().nextObject(Session.class);

    assertThat(session, is(session));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoSessionInstance() {
    Session session = new EasyRandom().nextObject(Session.class);

    boolean equals = session.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_sessionIdsAreDifferent() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setId(1L);
    Session otherSession = new EasyRandom().nextObject(Session.class);
    otherSession.setId(2L);

    boolean equals = session.equals(otherSession);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_sessionIdsAreEqual() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setId(1L);
    Session otherSession = new EasyRandom().nextObject(Session.class);
    otherSession.setId(1L);

    boolean equals = session.equals(otherSession);

    assertThat(equals, is(true));
  }
}

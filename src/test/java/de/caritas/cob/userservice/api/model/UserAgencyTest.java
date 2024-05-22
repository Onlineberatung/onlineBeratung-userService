package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

public class UserAgencyTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    UserAgency userAgency = new EasyRandom().nextObject(UserAgency.class);

    assertThat(userAgency, is(userAgency));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoUserAgencyInstance() {
    UserAgency userAgency = new EasyRandom().nextObject(UserAgency.class);

    boolean equals = userAgency.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_userIdsAreDifferent() {
    UserAgency userAgency = new EasyRandom().nextObject(UserAgency.class);
    userAgency.setId(1L);
    UserAgency otherUserAgency = new EasyRandom().nextObject(UserAgency.class);
    otherUserAgency.setId(2L);

    boolean equals = userAgency.equals(otherUserAgency);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_userIdsAreEqual() {
    UserAgency userAgency = new EasyRandom().nextObject(UserAgency.class);
    userAgency.setId(1L);
    UserAgency otherUserAgency = new EasyRandom().nextObject(UserAgency.class);
    otherUserAgency.setId(1L);

    boolean equals = userAgency.equals(otherUserAgency);

    assertThat(equals, is(true));
  }
}

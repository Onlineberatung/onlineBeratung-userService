package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    User user = new EasyRandom().nextObject(User.class);

    assertThat(user, is(user));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoUserInstance() {
    User user = new EasyRandom().nextObject(User.class);

    boolean equals = user.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_userIdsAreDifferent() {
    User user = new EasyRandom().nextObject(User.class);
    user.setUserId("1");
    User otherUser = new EasyRandom().nextObject(User.class);
    otherUser.setUserId("2");

    boolean equals = user.equals(otherUser);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_userIdsAreEqual() {
    User user = new EasyRandom().nextObject(User.class);
    user.setUserId("1");
    User otherUser = new EasyRandom().nextObject(User.class);
    otherUser.setUserId("1");

    boolean equals = user.equals(otherUser);

    assertThat(equals, is(true));
  }
}

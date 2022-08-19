package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

class UserMobileTokenTest {

  @Test
  void equals_Should_returnTrue_When_objectIsSameReference() {
    var userMobileToken = new EasyRandom().nextObject(UserMobileToken.class);

    assertThat(userMobileToken, is(userMobileToken));
  }

  @Test
  void equals_Should_returnFalse_When_objectIsNoUserMobileTokenInstance() {
    var userMobileToken = new EasyRandom().nextObject(UserMobileToken.class);

    var equals = userMobileToken.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  void equals_Should_returnFalse_When_userMobileTokenValuesAreDifferent() {
    var userMobileToken = new EasyRandom().nextObject(UserMobileToken.class);
    userMobileToken.setMobileAppToken("token");
    var otherUserMobileToken = new EasyRandom().nextObject(UserMobileToken.class);
    otherUserMobileToken.setMobileAppToken("token2");

    var equals = userMobileToken.equals(otherUserMobileToken);

    assertThat(equals, is(false));
  }

  @Test
  void equals_Should_returnTrue_When_userMobileTokensAreEqual() {
    var userMobileToken = new EasyRandom().nextObject(UserMobileToken.class);
    userMobileToken.setMobileAppToken("token");
    var otherUserMobileToken = new EasyRandom().nextObject(UserMobileToken.class);
    otherUserMobileToken.setMobileAppToken("token");

    var equals = userMobileToken.equals(otherUserMobileToken);

    assertThat(equals, is(true));
  }
}

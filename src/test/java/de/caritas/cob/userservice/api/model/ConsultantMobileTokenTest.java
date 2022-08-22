package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

class ConsultantMobileTokenTest {

  @Test
  void equals_Should_returnTrue_When_objectIsSameReference() {
    var consultantMobileToken = new EasyRandom().nextObject(ConsultantMobileToken.class);

    assertThat(consultantMobileToken, is(consultantMobileToken));
  }

  @Test
  void equals_Should_returnFalse_When_objectIsNoConsultantMobileTokenInstance() {
    var consultantMobileToken = new EasyRandom().nextObject(ConsultantMobileToken.class);

    var equals = consultantMobileToken.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  void equals_Should_returnFalse_When_consultantMobileTokenValuesAreDifferent() {
    var consultantMobileToken = new EasyRandom().nextObject(ConsultantMobileToken.class);
    consultantMobileToken.setMobileAppToken("token");
    var otherConsultantMobileToken = new EasyRandom().nextObject(ConsultantMobileToken.class);
    otherConsultantMobileToken.setMobileAppToken("token2");

    var equals = consultantMobileToken.equals(otherConsultantMobileToken);

    assertThat(equals, is(false));
  }

  @Test
  void equals_Should_returnTrue_When_consultantMobileTokensAreEqual() {
    var consultantMobileToken = new EasyRandom().nextObject(ConsultantMobileToken.class);
    consultantMobileToken.setMobileAppToken("token");
    var otherConsultantMobileToken = new EasyRandom().nextObject(ConsultantMobileToken.class);
    otherConsultantMobileToken.setMobileAppToken("token");

    var equals = consultantMobileToken.equals(otherConsultantMobileToken);

    assertThat(equals, is(true));
  }
}

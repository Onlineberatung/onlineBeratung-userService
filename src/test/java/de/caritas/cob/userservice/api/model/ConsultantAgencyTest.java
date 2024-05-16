package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

public class ConsultantAgencyTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);

    assertThat(consultantAgency, is(consultantAgency));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoConsultantAgencyInstance() {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);

    boolean equals = consultantAgency.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_consultantAgencyIdsAreDifferent() {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setId(1L);
    ConsultantAgency otherConsultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    otherConsultantAgency.setId(2L);

    boolean equals = consultantAgency.equals(otherConsultantAgency);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_consultantAgencyIdsAreEqual() {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setId(1L);
    ConsultantAgency otherConsultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    otherConsultantAgency.setId(1L);

    boolean equals = consultantAgency.equals(otherConsultantAgency);

    assertThat(equals, is(true));
  }
}

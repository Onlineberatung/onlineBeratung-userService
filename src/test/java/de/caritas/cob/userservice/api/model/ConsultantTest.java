package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import com.neovisionaries.i18n.LanguageCode;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantTest {

  private static final String FIRSTNAME = "firstname";
  private static final String LASTNAME = "lastname";
  private static final Consultant CONSULTANT =
      new Consultant(
          "yyy",
          "XXX",
          "consultant",
          FIRSTNAME,
          LASTNAME,
          "consultant@domain.de",
          false,
          false,
          null,
          false,
          null,
          1L,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);

  @Test
  public void getFullName_Should_Return_FirstnameAndLastname() {
    String result = CONSULTANT.getFullName();
    String expectedFullName = FIRSTNAME + " " + LASTNAME;

    assertEquals(expectedFullName, result);
  }

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);

    assertThat(consultant, is(consultant));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoConsultantInstance() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);

    boolean equals = consultant.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_consultantIdsAreDifferent() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setId("1");
    Consultant otherConsultant = new EasyRandom().nextObject(Consultant.class);
    otherConsultant.setId("2");

    boolean equals = consultant.equals(otherConsultant);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_consultantIdsAreEqual() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setId("1");
    Consultant otherConsultant = new EasyRandom().nextObject(Consultant.class);
    otherConsultant.setId("1");

    boolean equals = consultant.equals(otherConsultant);

    assertThat(equals, is(true));
  }
}

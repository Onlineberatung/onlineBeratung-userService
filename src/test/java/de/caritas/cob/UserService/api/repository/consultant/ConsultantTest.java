package de.caritas.cob.UserService.api.repository.consultant;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantTest {

  private final String CONSULTANT_ID = "yyy";

  private final String FIRSTNAME = "firstname";
  private final String LASTNAME = "lastname";
  private final String FULL_NAME = FIRSTNAME + " " + LASTNAME;
  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, "XXX", "consultant",
      FIRSTNAME, LASTNAME, "consultant@domain.de", false, false, null, false, 1L, null, null);

  @Test
  public void getFullName_Should_Return_FirstnameAndLastname() {

    String result = CONSULTANT.getFullName();
    assertEquals(FULL_NAME, result);

  }

}

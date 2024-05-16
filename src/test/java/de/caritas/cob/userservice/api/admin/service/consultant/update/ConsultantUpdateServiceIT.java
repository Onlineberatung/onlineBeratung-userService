package de.caritas.cob.userservice.api.admin.service.consultant.update;

import de.caritas.cob.userservice.api.UserServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantUpdateServiceIT extends ConsultantUpdateServiceBase {

  protected String VALID_CONSULTANT_ID = "5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe";

  @Test
  public void updateConsultant_Should_returnUpdatedPersistedConsultant_When_inputDataIsValid() {
    super.updateConsultant_Should_returnUpdatedPersistedConsultant_When_inputDataIsValid();
  }

  @Test
  public void updateConsultant_Should_throwCustomResponseException_When_absenceIsInvalid() {
    super.updateConsultant_Should_throwCustomResponseException_When_absenceIsInvalid();
  }

  @Test
  public void updateConsultant_Should_throwCustomResponseException_When_newEmailIsInvalid() {
    super.updateConsultant_Should_throwCustomResponseException_When_newEmailIsInvalid();
  }

  protected String getValidConsultantId() {
    return VALID_CONSULTANT_ID;
  }
}

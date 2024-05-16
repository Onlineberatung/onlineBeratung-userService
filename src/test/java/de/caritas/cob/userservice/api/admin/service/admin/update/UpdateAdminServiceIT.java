package de.caritas.cob.userservice.api.admin.service.admin.update;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_VALID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class UpdateAdminServiceIT {

  private final String VALID_ADMIN_ID = "164be67d-4d1b-4d80-bb6b-0ee057a1c59e";

  @Autowired private UpdateAdminService updateAdminService;
  @MockBean private IdentityClient identityClient;
  @Autowired private RetrieveAdminService retrieveAdminService;

  @Test
  public void updateAgencyAdmin_Should_returnUpdatedPersistedAdmin_When_inputDataIsValid() {
    // given
    String newFirstname = "new firstname";
    String newLastname = "new lastname";
    String newEmail = "newemail@email.com";
    UpdateAgencyAdminDTO updateAgencyAdminDTO = new UpdateAgencyAdminDTO();
    updateAgencyAdminDTO.setFirstname(newFirstname);
    updateAgencyAdminDTO.setLastname(newLastname);
    updateAgencyAdminDTO.setEmail(newEmail);

    // when
    Admin updatedAdmin = updateAdminService.updateAgencyAdmin(VALID_ADMIN_ID, updateAgencyAdminDTO);
    Admin admin = retrieveAdminService.findAdmin(VALID_ADMIN_ID, Admin.AdminType.AGENCY);

    // then
    assertThat(updatedAdmin, notNullValue());
    assertThat(updatedAdmin.getFirstName(), is(newFirstname));
    assertThat(updatedAdmin.getLastName(), is(newLastname));
    assertThat(updatedAdmin.getEmail(), is(newEmail));

    assertThat(admin, notNullValue());
    assertThat(admin.getFirstName(), is(newFirstname));
    assertThat(admin.getLastName(), is(newLastname));
    assertThat(admin.getEmail(), is(newEmail));
  }

  @Test
  public void updateAgencyAdmin_Should_throwCustomResponseException_When_newEmailIsInvalid() {
    // given
    UpdateAgencyAdminDTO updateAgencyAdminDTO = new UpdateAgencyAdminDTO();
    updateAgencyAdminDTO.setEmail("invalid");

    // when
    try {
      this.updateAdminService.updateAgencyAdmin(VALID_ADMIN_ID, updateAgencyAdminDTO);
      fail("Exception should be thrown");

      // then
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders().get("X-Reason").get(0), is(EMAIL_NOT_VALID.name()));
    }
  }
}

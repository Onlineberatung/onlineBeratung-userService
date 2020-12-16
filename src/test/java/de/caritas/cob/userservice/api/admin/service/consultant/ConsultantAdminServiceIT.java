package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.model.GetConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.HalLink.MethodEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantAdminServiceIT {

  private static final String EXISTING_CONSULTANT = "0b3b1cc6-be98-4787-aa56-212259d811b9";

  @Autowired
  private ConsultantAdminService consultantAdminService;

  @Test
  public void findConsultantById_Should_returnExpectedConsultant_When_consultantIdExists() {
    GetConsultantResponseDTO consultantById = this.consultantAdminService
        .findConsultantById(EXISTING_CONSULTANT);

    assertThat(consultantById.getEmbedded(), notNullValue());
    assertThat(consultantById.getEmbedded().getEmail(), notNullValue());
    assertThat(consultantById.getEmbedded().getFirstname(), notNullValue());
    assertThat(consultantById.getEmbedded().getLastname(), notNullValue());
    assertThat(consultantById.getEmbedded().getUsername(), notNullValue());
    assertThat(consultantById.getEmbedded().getAbsent(), notNullValue());
    assertThat(consultantById.getEmbedded().getTeamConsultant(), notNullValue());
    assertThat(consultantById.getEmbedded().getFormalLanguage(), notNullValue());
    assertThat(consultantById.getEmbedded().getId(), is(EXISTING_CONSULTANT));
    assertThat(consultantById.getEmbedded().getUpdateDate(), notNullValue());
    assertThat(consultantById.getEmbedded().getCreateDate(), notNullValue());
  }

  @Test
  public void findConsultantById_Should_returnExpectedConsultantLinks_When_consultantIdExists() {
    GetConsultantResponseDTO consultantById = this.consultantAdminService
        .findConsultantById(EXISTING_CONSULTANT);

    assertThat(consultantById.getLinks(), notNullValue());
    assertThat(consultantById.getLinks().getSelf(), notNullValue());
    assertThat(consultantById.getLinks().getSelf().getHref(),
        endsWith("/useradmin/consultant/" + EXISTING_CONSULTANT));
    assertThat(consultantById.getLinks().getSelf().getMethod(), is(MethodEnum.GET));
    assertThat(consultantById.getLinks().getUpdate(), notNullValue());
    assertThat(consultantById.getLinks().getUpdate().getHref(),
        endsWith("/useradmin/consultant/" + EXISTING_CONSULTANT));
    assertThat(consultantById.getLinks().getUpdate().getMethod(), is(MethodEnum.PUT));
    assertThat(consultantById.getLinks().getDelete(), notNullValue());
    assertThat(consultantById.getLinks().getDelete().getHref(),
        endsWith("/useradmin/consultant/" + EXISTING_CONSULTANT));
    assertThat(consultantById.getLinks().getDelete().getMethod(), is(MethodEnum.DELETE));
  }

  @Test(expected = NoContentException.class)
  public void findConsultantById_Should_throwNoContentException_When_consultantIdDoesNotExist() {
    this.consultantAdminService.findConsultantById("Invalid");
  }

}

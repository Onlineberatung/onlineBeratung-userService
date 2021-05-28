package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.admin.service.consultant.create.ConsultantCreatorService;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.model.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

  @MockBean
  private ConsultantCreatorService consultantCreatorService;

  @MockBean
  private ConsultantUpdateService consultantUpdateService;

  @Test
  public void findConsultantById_Should_returnExpectedConsultant_When_consultantIdExists() {
    ConsultantAdminResponseDTO consultantById = this.consultantAdminService
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
    ConsultantAdminResponseDTO consultantById = this.consultantAdminService
        .findConsultantById(EXISTING_CONSULTANT);

    assertThat(consultantById.getLinks(), notNullValue());
    assertThat(consultantById.getLinks().getSelf(), notNullValue());
    assertThat(consultantById.getLinks().getSelf().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT));
    assertThat(consultantById.getLinks().getSelf().getMethod(), is(MethodEnum.GET));
    assertThat(consultantById.getLinks().getUpdate(), notNullValue());
    assertThat(consultantById.getLinks().getUpdate().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT));
    assertThat(consultantById.getLinks().getUpdate().getMethod(), is(MethodEnum.PUT));
    assertThat(consultantById.getLinks().getDelete(), notNullValue());
    assertThat(consultantById.getLinks().getDelete().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT));
    assertThat(consultantById.getLinks().getDelete().getMethod(), is(MethodEnum.DELETE));
    assertThat(consultantById.getLinks().getAgencies(), notNullValue());
    assertThat(consultantById.getLinks().getAgencies().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT + "/agencies"));
    assertThat(consultantById.getLinks().getAgencies().getMethod(), is(MethodEnum.GET));
    assertThat(consultantById.getLinks().getAddAgency(), notNullValue());
    assertThat(consultantById.getLinks().getAddAgency().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT + "/agencies"));
    assertThat(consultantById.getLinks().getAddAgency().getMethod(), is(MethodEnum.POST));
  }

  @Test(expected = NoContentException.class)
  public void findConsultantById_Should_throwNoContentException_When_consultantIdDoesNotExist() {
    this.consultantAdminService.findConsultantById("Invalid");
  }

  @Test
  public void createNewConsultant_Should_useCreatorServiceAndBuildConsultantAdminResponseDTO() {
    CreateConsultantDTO createConsultantDTO =
        new EasyRandom().nextObject(CreateConsultantDTO.class);
    when(this.consultantCreatorService.createNewConsultant(any()))
        .thenReturn(new EasyRandom().nextObject(Consultant.class));

    ConsultantAdminResponseDTO result =
        this.consultantAdminService.createNewConsultant(createConsultantDTO);

    verify(this.consultantCreatorService, times(1)).createNewConsultant(eq(createConsultantDTO));
    assertThat(result.getLinks(), notNullValue());
    assertThat(result.getEmbedded(), notNullValue());
  }

  @Test
  public void updateConsultant_Should_useUpdateServiceAndBuildConsultantAdminResponseDTO() {
    UpdateAdminConsultantDTO updateConsultantDTO =
        new EasyRandom().nextObject(UpdateAdminConsultantDTO.class);
    when(this.consultantUpdateService.updateConsultant(any(), any()))
        .thenReturn(new EasyRandom().nextObject(Consultant.class));

    ConsultantAdminResponseDTO result =
        this.consultantAdminService.updateConsultant("id", updateConsultantDTO);

    verify(this.consultantUpdateService, times(1)).updateConsultant(any(), any());
    assertThat(result.getLinks(), notNullValue());
    assertThat(result.getEmbedded(), notNullValue());
  }

}

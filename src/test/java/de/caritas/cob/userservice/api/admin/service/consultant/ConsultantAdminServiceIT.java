package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import de.caritas.cob.userservice.api.AccountManager;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.create.CreateConsultantSaga;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
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
public class ConsultantAdminServiceIT {

  private static final String EXISTING_CONSULTANT = "0b3b1cc6-be98-4787-aa56-212259d811b9";
  private static final Boolean FORCE_DELETE_SESSIONS = false;

  @Autowired private ConsultantAdminService consultantAdminService;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private ConsultantAgencyRepository consultantAgencyRepository;

  @MockBean private CreateConsultantSaga createConsultantSaga;

  @MockBean private ConsultantUpdateService consultantUpdateService;

  @MockBean private AppointmentService appointmentService;

  @MockBean private AccountManager accountManager;

  @Test
  public void findConsultantById_Should_returnExpectedConsultant_When_consultantIdExists() {
    ConsultantAdminResponseDTO consultantById =
        this.consultantAdminService.findConsultantById(EXISTING_CONSULTANT);

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
    ConsultantAdminResponseDTO consultantById =
        this.consultantAdminService.findConsultantById(EXISTING_CONSULTANT);

    assertThat(consultantById.getLinks(), notNullValue());
    assertThat(consultantById.getLinks().getSelf(), notNullValue());
    assertThat(
        consultantById.getLinks().getSelf().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT));
    assertThat(consultantById.getLinks().getSelf().getMethod(), is(MethodEnum.GET));
    assertThat(consultantById.getLinks().getUpdate(), notNullValue());
    assertThat(
        consultantById.getLinks().getUpdate().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT));
    assertThat(consultantById.getLinks().getUpdate().getMethod(), is(MethodEnum.PUT));
    assertThat(consultantById.getLinks().getDelete(), notNullValue());
    assertThat(
        consultantById.getLinks().getDelete().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT + "?forceDeleteSessions=false"));
    assertThat(consultantById.getLinks().getDelete().getMethod(), is(MethodEnum.DELETE));
    assertThat(consultantById.getLinks().getAgencies(), notNullValue());
    assertThat(
        consultantById.getLinks().getAgencies().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT + "/agencies"));
    assertThat(consultantById.getLinks().getAgencies().getMethod(), is(MethodEnum.GET));
    assertThat(consultantById.getLinks().getAddAgency(), notNullValue());
    assertThat(
        consultantById.getLinks().getAddAgency().getHref(),
        endsWith("/useradmin/consultants/" + EXISTING_CONSULTANT + "/agencies"));
    assertThat(consultantById.getLinks().getAddAgency().getMethod(), is(MethodEnum.POST));
  }

  @Test
  public void findConsultantById_Should_throwNoContentException_When_consultantIdDoesNotExist() {
    assertThrows(
        NoContentException.class,
        () -> {
          this.consultantAdminService.findConsultantById("Invalid");
        });
  }

  @Test
  public void createNewConsultant_Should_useCreatorServiceAndBuildConsultantAdminResponseDTO() {
    CreateConsultantDTO createConsultantDTO =
        new EasyRandom().nextObject(CreateConsultantDTO.class);
    when(this.createConsultantSaga.createNewConsultant(any()))
        .thenReturn(new EasyRandom().nextObject(ConsultantAdminResponseDTO.class));

    ConsultantAdminResponseDTO result =
        this.consultantAdminService.createNewConsultant(createConsultantDTO);

    verify(this.createConsultantSaga, times(1)).createNewConsultant(createConsultantDTO);
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

  @Test
  public void markConsultantForDeletion_Should_setDeleteDateForConsultantAndConsultantAgencies() {
    var consultant = givenAPersistedConsultantWithMultipleAgencies();

    this.consultantAdminService.markConsultantForDeletion(consultant.getId(), false);

    var deletedConsultant = consultantRepository.findById(consultant.getId());
    assertThat(deletedConsultant.get().getDeleteDate(), notNullValue());
    assertThat(deletedConsultant.get().getStatus(), is(ConsultantStatus.IN_DELETION));
    deletedConsultant
        .get()
        .getConsultantAgencies()
        .forEach(
            ca -> {
              assertThat(ca.getDeleteDate(), notNullValue());
            });
  }

  private Consultant givenAPersistedConsultantWithMultipleAgencies() {
    var parameters =
        new EasyRandomParameters()
            .stringLengthRange(1, 17)
            .excludeField(FieldPredicates.named("consultantAgencies"))
            .excludeField(FieldPredicates.named("languages"))
            .excludeField(FieldPredicates.named("consultantMobileTokens"))
            .excludeField(FieldPredicates.named("deleteDate"))
            .excludeField(FieldPredicates.named("appointments"))
            .excludeField(FieldPredicates.named("sessions"));
    var consultant = new EasyRandom(parameters).nextObject(Consultant.class);
    consultantRepository.save(consultant);
    var consultantAgencies =
        new EasyRandom()
            .objects(ConsultantAgency.class, 10)
            .peek(
                agencyRelation -> {
                  agencyRelation.setAgencyId(1L);
                  agencyRelation.setConsultant(consultant);
                  agencyRelation.setDeleteDate(null);
                })
            .collect(Collectors.toList());
    consultantAgencyRepository.saveAll(consultantAgencies);
    return consultant;
  }
}

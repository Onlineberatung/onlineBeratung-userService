package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static de.caritas.cob.userservice.api.testHelper.AsyncVerification.verifyAsync;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hibernate.search.util.impl.CollectionHelper.asSet;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.util.Lists;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ConsultantAgencyRelationCreatorServiceIT {

  private final EasyRandom easyRandom = new EasyRandom();

  @Autowired private ConsultantAgencyRelationCreatorService consultantAgencyRelationCreatorService;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private UserAgencyRepository userAgencyRepository;

  @Autowired private ConsultantAgencyRepository consultantAgencyRepository;

  @Autowired private SessionRepository sessionRepository;

  @MockBean private AgencyService agencyService;

  @MockBean private KeycloakService keycloakService;

  @MockBean private RocketChatFacade rocketChatFacade;

  @MockBean private ConsultingTypeManager consultingTypeManager;

  @Autowired private RocketChatAsyncHelper rocketChatAsyncHelper;

  @Test
  public void
      createNewConsultantAgency_Should_addConsultantToEnquiriesRocketChatGroups_When_ParamsAreValid() {

    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRoleSetKey("valid-role-set");

    when(keycloakService.userHasRole(eq(consultant.getId()), any())).thenReturn(true);

    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(false);
    agencyDTO.setConsultingType(0);
    when(agencyService.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    Session enquirySessionWithoutConsultant =
        createSessionWithoutConsultant(agencyDTO.getId(), SessionStatus.NEW);

    final var consultingTypeResponse =
        easyRandom.nextObject(ExtendedConsultingTypeResponseDTO.class);
    when(consultingTypeManager.getConsultingTypeSettings(0)).thenReturn(consultingTypeResponse);

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultant.getId(), createConsultantAgencyDTO);

    verifyAsync(
        (a) ->
            verify(rocketChatFacade, times(1))
                .addUserToRocketChatGroup(
                    consultant.getRocketChatId(), enquirySessionWithoutConsultant.getGroupId()));

    verifyAsync(
        (a) ->
            verify(rocketChatFacade, times(1))
                .addUserToRocketChatGroup(
                    consultant.getRocketChatId(),
                    enquirySessionWithoutConsultant.getFeedbackGroupId()));

    List<ConsultantAgency> result =
        this.consultantAgencyRepository.findByConsultantIdAndDeleteDateIsNull(consultant.getId());

    assertThat(result, notNullValue());
    assertThat(result, hasSize(1));
  }

  @Test
  public void
      createNewConsultantAgency_Should_addConsultantToTeamSessionRocketChatGroups_When_ParamsAreValid() {

    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRoleSetKey("valid-role-set");

    when(keycloakService.userHasRole(eq(consultant.getId()), any())).thenReturn(true);
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setExcludeNonMainConsultantsFromTeamSessions(true);
    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(true);
    agencyDTO.setConsultingType(0);
    when(agencyService.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);
    when(consultingTypeManager.getConsultingTypeSettings(0))
        .thenReturn(extendedConsultingTypeResponseDTO);

    Session enquirySessionWithoutConsultant =
        createSessionWithoutConsultant(agencyDTO.getId(), SessionStatus.IN_PROGRESS);

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultant.getId(), createConsultantAgencyDTO);

    verify(rocketChatFacade, timeout(10000))
        .addUserToRocketChatGroup(
            consultant.getRocketChatId(), enquirySessionWithoutConsultant.getGroupId());

    verify(rocketChatFacade, timeout(10000))
        .addUserToRocketChatGroup(
            consultant.getRocketChatId(), enquirySessionWithoutConsultant.getFeedbackGroupId());

    List<ConsultantAgency> result =
        this.consultantAgencyRepository.findByConsultantIdAndDeleteDateIsNull(consultant.getId());

    assertThat(result, notNullValue());
    assertThat(result, hasSize(1));
    assertThat(
        this.consultantRepository
            .findByIdAndDeleteDateIsNull(consultant.getId())
            .get()
            .isTeamConsultant(),
        is(true));
  }

  @Test
  public void createNewConsultantAgency_Should_updateKeycloakRoles_When_ParamsAreValid() {
    var roleSetName = "peer";
    var createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRoleSetKey(roleSetName);

    int consultingType = 0;
    var agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(false);
    agencyDTO.setConsultingType(consultingType);
    when(agencyService.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    var consultant = createConsultantWithoutAgencyAndSession();
    when(keycloakService.userHasRole(eq(consultant.getId()), any())).thenReturn(true);
    var roles = givenRoleSets(consultingType, roleSetName);

    consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultant.getId(), createConsultantAgencyDTO);

    roles.forEach(role -> verify(keycloakService).ensureRole(consultant.getId(), role));
    var result =
        consultantAgencyRepository.findByConsultantIdAndDeleteDateIsNull(consultant.getId());

    assertThat(result, notNullValue());
    assertThat(result, hasSize(1));
  }

  @SuppressWarnings("SameParameterValue")
  private List<String> givenRoleSets(int consultingTypeId, String roleSetName) {
    var roleSets = new LinkedHashMap<String, List<String>>();
    var roles = List.of("consultant", "u25-consultant");
    roleSets.put(roleSetName, roles);

    var roleConsultant =
        new de.caritas.cob.userservice.api.manager.consultingtype.roles.Consultant();
    roleConsultant.setRoleSets(roleSets);

    var rolesDTO =
        new de.caritas.cob.userservice.consultingtypeservice.generated.web.model.RolesDTO();
    rolesDTO.setConsultant(roleConsultant);

    final var consultingTypeResponse =
        easyRandom.nextObject(ExtendedConsultingTypeResponseDTO.class);
    consultingTypeResponse.setRoles(rolesDTO);
    when(consultingTypeManager.getConsultingTypeSettings(consultingTypeId))
        .thenReturn(consultingTypeResponse);

    return roles;
  }

  private Consultant createConsultantWithoutAgencyAndSession() {
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    consultant.setConsultantAgencies(null);
    consultant.setSessions(null);
    consultant.setConsultantMobileTokens(null);
    consultant.setRocketChatId("RocketChatId");
    consultant.setDeleteDate(null);
    consultant.setLanguages(null);
    consultant.setAppointments(null);
    return this.consultantRepository.save(consultant);
  }

  private Session createSessionWithoutConsultant(Long agencyId, SessionStatus sessionStatus) {

    User user = easyRandom.nextObject(User.class);
    user.setSessions(null);
    user.setUserMobileTokens(null);
    user.setUserAgencies(null);
    this.userRepository.save(user);

    UserAgency userAgency = new UserAgency();
    userAgency.setAgencyId(agencyId);
    userAgency.setUser(user);
    this.userAgencyRepository.save(userAgency);

    Session session = new Session();
    session.setStatus(sessionStatus);
    session.setPostcode("12345");
    session.setId(1L);
    session.setConsultant(null);
    session.setUser(user);
    session.setAgencyId(agencyId);
    session.setLanguageCode(LanguageCode.de);
    session.setTeamSession(true);
    session.setSessionTopics(Lists.newArrayList());
    session.setIsConsultantDirectlySet(false);

    return this.sessionRepository.save(session);
  }

  @Test(expected = BadRequestException.class)
  public void
      createConsultantAgencyRelations_Should_throwBadRequestException_When_consultantDoesNotExist() {
    this.consultantAgencyRelationCreatorService.createConsultantAgencyRelations(
        "invalid", asSet(1L), asSet("role"), null);
  }

  @Test(expected = BadRequestException.class)
  public void
      createNewConsultantAgency_Should_throwBadRequestException_When_consultantHasNotExpectedRole() {
    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    when(keycloakService.userHasRole(any(), any())).thenReturn(false);

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultant.getId(), createConsultantAgencyDTO);
  }

  @Test(expected = BadRequestException.class)
  public void
      createNewConsultantAgency_Should_throwBadRequestException_When_agencyServiceReturnesNullForAgency() {
    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO =
        new CreateConsultantAgencyDTO().roleSetKey("valid role set");
    when(keycloakService.userHasRole(any(), any())).thenReturn(true);
    when(this.agencyService.getAgencyWithoutCaching(any())).thenReturn(null);

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultant.getId(), createConsultantAgencyDTO);
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createNewConsultantAgency_Should_throwInternalServerErrorException_When_agencyServiceThrowsAgencyServiceHelperException() {
    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO =
        new CreateConsultantAgencyDTO().roleSetKey("valid role set");
    when(keycloakService.userHasRole(any(), any())).thenReturn(true);
    when(agencyService.getAgencyWithoutCaching(any()))
        .thenThrow(new InternalServerErrorException(""));

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultant.getId(), createConsultantAgencyDTO);
  }

  @Test(expected = BadRequestException.class)
  public void
      createNewConsultantAgency_Should_throwBadRequestException_When_agencyTypeIsU25AndConsultantHasAnotherConsultingTypeAssigned() {

    AgencyDTO emigrationAgency = new AgencyDTO().consultingType(17);

    AgencyDTO agencyDTO = new AgencyDTO().consultingType(1).id(2L);

    when(agencyService.getAgencyWithoutCaching(1731L)).thenReturn(emigrationAgency);
    when(agencyService.getAgencyWithoutCaching(2L)).thenReturn(agencyDTO);
    when(keycloakService.userHasRole(any(), any())).thenReturn(true);
    when(consultingTypeManager.isConsultantBoundedToAgency(1)).thenReturn(true);

    CreateConsultantAgencyDTO createConsultantAgencyDTO =
        new CreateConsultantAgencyDTO().roleSetKey("valid role set").agencyId(2L);

    String consultantIdWIthEmigrationAgency = "0b3b1cc6-be98-4787-aa56-212259d811b9";
    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultantIdWIthEmigrationAgency, createConsultantAgencyDTO);
  }

  @Test(expected = BadRequestException.class)
  public void
      createNewConsultantAgency_Should_throwBadRequestException_When_agencyTypeIsKreuzbundAndConsultantHasAnotherConsultingTypeAssigned() {

    AgencyDTO emigrationAgency = new AgencyDTO().consultingType(17);

    AgencyDTO agencyDTO = new AgencyDTO().consultingType(15).id(2L);

    when(agencyService.getAgencyWithoutCaching(1731L)).thenReturn(emigrationAgency);
    when(agencyService.getAgencyWithoutCaching(2L)).thenReturn(agencyDTO);
    when(keycloakService.userHasRole(any(), any())).thenReturn(true);
    when(consultingTypeManager.isConsultantBoundedToAgency(15)).thenReturn(true);

    CreateConsultantAgencyDTO createConsultantAgencyDTO =
        new CreateConsultantAgencyDTO().roleSetKey("valid role set").agencyId(2L);

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
        "0b3b1cc6-be98-4787-aa56-212259d811b9", createConsultantAgencyDTO);
  }

  @Test
  public void
      createConsultantAgencyRelations_Should_throwBadRequestException_When_ConsultantHasNotRequestedRole() {
    final var consultant = createConsultantWithoutAgencyAndSession();
    try {
      consultantAgencyRelationCreatorService.createConsultantAgencyRelations(
          consultant.getId(), Set.of(), Set.of(), null);
      fail("There was no BadRequestException");
    } catch (Exception e) {
      assertThat(e, instanceOf(BadRequestException.class));
      assertThat(
          e.getMessage(),
          is("Consultant with id " + consultant.getId() + " does not have the role set []"));
    }
  }
}

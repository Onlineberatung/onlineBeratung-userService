package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hibernate.search.util.impl.CollectionHelper.asSet;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgency;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgencyRepository;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.List;
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

  @Autowired
  private ConsultantAgencyRelationCreatorService consultantAgencyRelationCreatorService;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserAgencyRepository userAgencyRepository;

  @Autowired
  private ConsultantAgencyRepository consultantAgencyRepository;

  @Autowired
  private SessionRepository sessionRepository;

  @MockBean
  private AgencyServiceHelper agencyServiceHelper;

  @MockBean
  private KeycloakAdminClientService keycloakAdminClientService;

  @MockBean
  private RocketChatService rocketChatService;

  @Test
  public void createNewConsultantAgency_Should_addConsultantToEnquiriesRocketChatGroups_When_ParamsAreValid()
      throws AgencyServiceHelperException, RocketChatUserNotInitializedException, RocketChatAddUserToGroupException, RocketChatRemoveUserFromGroupException {

    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("valid-role");

    when(keycloakAdminClientService.userHasRole(eq(consultant.getId()), any())).thenReturn(true);

    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(false);
    agencyDTO.setConsultingType(ConsultingType.SUCHT);
    when(agencyServiceHelper.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    Session enquirySessionWithoutConsultant = createSessionWithoutConsultant(agencyDTO.getId(),
        SessionStatus.NEW);

    this.consultantAgencyRelationCreatorService
        .createNewConsultantAgency(consultant.getId(), createConsultantAgencyDTO);

    verify(rocketChatService, times(1))
        .addTechnicalUserToGroup(eq(enquirySessionWithoutConsultant.getGroupId()));
    verify(rocketChatService, times(1))
        .addUserToGroup(eq(consultant.getRocketChatId()),
            eq(enquirySessionWithoutConsultant.getGroupId()));
    verify(rocketChatService, times(1))
        .addUserToGroup(eq(consultant.getRocketChatId()),
            eq(enquirySessionWithoutConsultant.getFeedbackGroupId()));
    verify(rocketChatService, times(1))
        .removeTechnicalUserFromGroup(eq(enquirySessionWithoutConsultant.getGroupId()));
    List<ConsultantAgency> result = this.consultantAgencyRepository
        .findByConsultantId(consultant.getId());

    assertThat(result, notNullValue());
    assertThat(result, hasSize(1));
  }

  @Test
  public void createNewConsultantAgency_Should_addConsultantToTeamSessionRocketChatGroups_When_ParamsAreValid()
      throws AgencyServiceHelperException, RocketChatUserNotInitializedException, RocketChatAddUserToGroupException, RocketChatRemoveUserFromGroupException {

    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("valid-role");

    when(keycloakAdminClientService.userHasRole(eq(consultant.getId()), any())).thenReturn(true);

    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(true);
    agencyDTO.setConsultingType(ConsultingType.SUCHT);
    when(agencyServiceHelper.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    Session enquirySessionWithoutConsultant = createSessionWithoutConsultant(agencyDTO.getId(),
        SessionStatus.IN_PROGRESS);

    this.consultantAgencyRelationCreatorService
        .createNewConsultantAgency(consultant.getId(), createConsultantAgencyDTO);

    verify(rocketChatService, times(1))
        .addTechnicalUserToGroup(eq(enquirySessionWithoutConsultant.getGroupId()));
    verify(rocketChatService, times(1))
        .addUserToGroup(eq(consultant.getRocketChatId()),
            eq(enquirySessionWithoutConsultant.getGroupId()));
    verify(rocketChatService, times(1))
        .addUserToGroup(eq(consultant.getRocketChatId()),
            eq(enquirySessionWithoutConsultant.getFeedbackGroupId()));
    verify(rocketChatService, times(1))
        .removeTechnicalUserFromGroup(eq(enquirySessionWithoutConsultant.getGroupId()));
    List<ConsultantAgency> result = this.consultantAgencyRepository
        .findByConsultantId(consultant.getId());

    assertThat(result, notNullValue());
    assertThat(result, hasSize(1));
    assertThat(this.consultantRepository.findById(consultant.getId()).get().isTeamConsultant(),
        is(true));
  }

  private Consultant createConsultantWithoutAgencyAndSession() {
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    consultant.setConsultantAgencies(null);
    consultant.setSessions(null);
    consultant.setRocketChatId("RocketChatId");
    return this.consultantRepository.save(consultant);
  }

  private Session createSessionWithoutConsultant(Long agencyId, SessionStatus sessionStatus) {

    User user = easyRandom.nextObject(User.class);
    user.setSessions(null);
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
    session.setTeamSession(true);
    return this.sessionRepository.save(session);
  }

  @Test(expected = BadRequestException.class)
  public void createConsultantAgencyRelations_Should_throwBadRequestException_When_consultantDoesNotExist() {
    this.consultantAgencyRelationCreatorService.createConsultantAgencyRelations("invalid",
        asSet(1L), asSet("role"), null);
  }

  @Test(expected = BadRequestException.class)
  public void createNewConsultantAgency_Should_throwBadRequestException_When_consultantHasNotExpectedRole() {
    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    when(keycloakAdminClientService.userHasRole(any(), any())).thenReturn(false);

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(consultant.getId(),
        createConsultantAgencyDTO);
  }

  @Test(expected = BadRequestException.class)
  public void createNewConsultantAgency_Should_throwBadRequestException_When_agencyServiceReturnesNullForAgency()
      throws AgencyServiceHelperException {
    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO()
        .role("valid role");
    when(keycloakAdminClientService.userHasRole(any(), any())).thenReturn(true);
    when(this.agencyServiceHelper.getAgencyWithoutCaching(any())).thenReturn(null);

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(consultant.getId(),
        createConsultantAgencyDTO);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createNewConsultantAgency_Should_throwInternalServerErrorException_When_agencyServiceThrowsAgencyServiceHelperException()
      throws AgencyServiceHelperException {
    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO()
        .role("valid role");
    when(keycloakAdminClientService.userHasRole(any(), any())).thenReturn(true);
    when(agencyServiceHelper.getAgencyWithoutCaching(any()))
        .thenThrow(new AgencyServiceHelperException(new Exception()));

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(consultant.getId(),
        createConsultantAgencyDTO);
  }

  @Test(expected = BadRequestException.class)
  public void createNewConsultantAgency_Should_throwBadRequestException_When_agencyTypeIsU25AndConsultantHasAnotherConsultingTypeAssigned()
      throws AgencyServiceHelperException {

    AgencyDTO emigrationAgency = new AgencyDTO()
        .consultingType(ConsultingType.EMIGRATION);

    AgencyDTO agencyDTO = new AgencyDTO()
        .consultingType(ConsultingType.U25)
        .id(2L);

    when(agencyServiceHelper.getAgencyWithoutCaching(eq(1731L))).thenReturn(emigrationAgency);
    when(agencyServiceHelper.getAgencyWithoutCaching(eq(2L))).thenReturn(agencyDTO);
    when(keycloakAdminClientService.userHasRole(any(), any())).thenReturn(true);

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO()
        .role("valid role")
        .agencyId(2L);

    String consultantIdWIthEmigrationAgency = "0b3b1cc6-be98-4787-aa56-212259d811b9";
    this.consultantAgencyRelationCreatorService
        .createNewConsultantAgency(consultantIdWIthEmigrationAgency, createConsultantAgencyDTO);
  }

  @Test(expected = BadRequestException.class)
  public void createNewConsultantAgency_Should_throwBadRequestException_When_agencyTypeIsKreuzbundAndConsultantHasAnotherConsultingTypeAssigned()
      throws AgencyServiceHelperException {

    AgencyDTO emigrationAgency = new AgencyDTO()
        .consultingType(ConsultingType.EMIGRATION);

    AgencyDTO agencyDTO = new AgencyDTO()
        .consultingType(ConsultingType.KREUZBUND)
        .id(2L);

    when(agencyServiceHelper.getAgencyWithoutCaching(eq(1731L))).thenReturn(emigrationAgency);
    when(agencyServiceHelper.getAgencyWithoutCaching(eq(2L))).thenReturn(agencyDTO);
    when(keycloakAdminClientService.userHasRole(any(), any())).thenReturn(true);

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO()
        .role("valid role")
        .agencyId(2L);


    this.consultantAgencyRelationCreatorService
        .createNewConsultantAgency("0b3b1cc6-be98-4787-aa56-212259d811b9",
            createConsultantAgencyDTO);
  }

  @Test
  public void createNewConsultantAgency_Should_throwBadRequestException_When_ConsultantHasNotRequestedRole() {

    final Consultant consultant = createConsultantWithoutAgencyAndSession();
    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();

    try {
      consultantAgencyRelationCreatorService
          .createNewConsultantAgency(consultant.getId(), createConsultantAgencyDTO);
      fail("There was no BadRequestException");
    } catch (Exception e) {
      assertThat(e, instanceOf(BadRequestException.class));
      assertThat(e.getMessage(),
          is("Consultant with id " + consultant.getId() + " does not have the role "
              + "[null]"));
    }
  }

}

package de.caritas.cob.userservice.api.admin.service;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.admin.service.consultant.create.ConsultantAgencyCreatorService;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
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
public class ConsultantAgencyCreatorServiceIT {

  @Autowired
  private ConsultantAgencyCreatorService consultantAgencyCreatorService;

  private final EasyRandom easyRandom = new EasyRandom();

  @MockBean
  private AgencyServiceHelper agencyServiceHelper;

  @MockBean
  private ConsultantRepository consultantRepository;

  @MockBean
  private KeycloakAdminClientHelper keycloakAdminClientHelper;

  @MockBean
  private ConsultantAgencyService consultantAgencyService;

  @MockBean
  private SessionRepository sessionRepository;

  @MockBean
  private RocketChatService rocketChatService;

  @Test
  public void createNewConsultantAgency_Should_addConsultantToEnquiriesRocketChatGroups_When_ParamsAreValid()
      throws AgencyServiceHelperException, RocketChatUserNotInitializedException, RocketChatAddUserToGroupException, RocketChatRemoveUserFromGroupException {

    String consultantId = "12345678-1234-1234-1234-1234567890ab";

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("a-valid-role");

    Consultant consultant = this.easyRandom.nextObject(Consultant.class);
    consultant.setId(consultantId);
    Optional<Consultant> consultantOptional = Optional.of(consultant);
    when(consultantRepository.findById(consultantId)).thenReturn(consultantOptional);

    when(keycloakAdminClientHelper.userHasRole(consultantId, "a-valid-role")).thenReturn(true);

    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(false);
    when(agencyServiceHelper.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    Session session = new Session();
    session.setAgencyId(agencyDTO.getId());
    session.setGroupId("this-is-the-group-id-without-feedbackgroup");

    Session sessionWithFeedbackGroup = new Session();
    sessionWithFeedbackGroup.setAgencyId(agencyDTO.getId());
    sessionWithFeedbackGroup.setGroupId("this-is-the-group-id-with-feedbackgroup");
    sessionWithFeedbackGroup.setFeedbackGroupId("this-is-the-feedback-group-id");

    when(sessionRepository.findByAgencyId(15L))
        .thenReturn(Arrays.asList(session, sessionWithFeedbackGroup));

    LocalDateTime localDateTime = LocalDateTime.now();

    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setConsultant(consultant);
    consultantAgency.setAgencyId(agencyDTO.getId());
    consultantAgency.setCreateDate(localDateTime);
    consultantAgency.setUpdateDate(localDateTime);

    when(consultantAgencyService.saveConsultantAgency(any())).thenReturn(consultantAgency);

    ConsultantAgencyAdminResultDTO newConsultantAgency = consultantAgencyCreatorService
        .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);

    verify(rocketChatService, times(1)).addTechnicalUserToGroup(eq(session.getGroupId()));
    verify(rocketChatService, times(1))
        .addUserToGroup(eq(consultant.getRocketChatId()), eq(session.getGroupId()));
    verify(rocketChatService, times(1)).removeTechnicalUserFromGroup(eq(session.getGroupId()));

    verify(rocketChatService, times(1))
        .addTechnicalUserToGroup(eq(sessionWithFeedbackGroup.getGroupId()));
    verify(rocketChatService, times(1)).addUserToGroup(eq(consultant.getRocketChatId()),
        eq(sessionWithFeedbackGroup.getGroupId()));
    verify(rocketChatService, times(1)).addUserToGroup(eq(consultant.getRocketChatId()),
        eq(sessionWithFeedbackGroup.getFeedbackGroupId()));
    verify(rocketChatService, times(1))
        .removeTechnicalUserFromGroup(eq(sessionWithFeedbackGroup.getGroupId()));
  }

  @Test
  public void createNewConsultantAgency_Should_throwInternalServerErrorException_When_RocketChatCantBeReachedForGroupId()
      throws AgencyServiceHelperException, RocketChatUserNotInitializedException, RocketChatAddUserToGroupException {

    String consultantId = "12345678-1234-1234-1234-1234567890ab";

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("a-valid-role");

    Consultant consultant = this.easyRandom.nextObject(Consultant.class);
    consultant.setId(consultantId);
    Optional<Consultant> consultantOptional = Optional.of(consultant);
    when(consultantRepository.findById(consultantId)).thenReturn(consultantOptional);

    when(keycloakAdminClientHelper.userHasRole(consultantId, "a-valid-role")).thenReturn(true);

    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(false);
    when(agencyServiceHelper.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    Session session = new Session();
    session.setAgencyId(agencyDTO.getId());
    session.setGroupId("this-is-the-group-id-without-feedbackgroup");

    when(sessionRepository.findByAgencyId(15L)).thenReturn(Collections.singletonList(session));

    LocalDateTime localDateTime = LocalDateTime.now();

    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setConsultant(consultant);
    consultantAgency.setAgencyId(agencyDTO.getId());
    consultantAgency.setCreateDate(localDateTime);
    consultantAgency.setUpdateDate(localDateTime);

    when(consultantAgencyService.saveConsultantAgency(any())).thenReturn(consultantAgency);

    doThrow(new RocketChatAddUserToGroupException("Rocket.Chat test exception"))
        .when(rocketChatService).addTechnicalUserToGroup(session.getGroupId());

    try {
      ConsultantAgencyAdminResultDTO newConsultantAgency = consultantAgencyCreatorService
          .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
      fail("There was no BadRequestException");
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
      assertThat(e.getMessage(),
          is("RocketChatService error while setting up a Rocket.Chat room during consultantAgency-creation for groupId (this-is-the-group-id-without-feedbackgroup) and consultantId (12345678-1234-1234-1234-1234567890ab)"));
    }
  }

  @Test
  public void createNewConsultantAgency_Should_throwInternalServerErrorException_When_RocketChatCantBeReachedForFeedbackGroupId()
      throws AgencyServiceHelperException, RocketChatUserNotInitializedException, RocketChatAddUserToGroupException {

    String consultantId = "12345678-1234-1234-1234-1234567890ab";

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("a-valid-role");

    Consultant consultant = this.easyRandom.nextObject(Consultant.class);
    consultant.setId(consultantId);
    Optional<Consultant> consultantOptional = Optional.of(consultant);
    when(consultantRepository.findById(consultantId)).thenReturn(consultantOptional);

    when(keycloakAdminClientHelper.userHasRole(consultantId, "a-valid-role")).thenReturn(true);

    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(false);
    when(agencyServiceHelper.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    Session session = new Session();
    session.setAgencyId(agencyDTO.getId());
    session.setGroupId("this-is-the-group-id-with-feedbackgroup");
    session.setFeedbackGroupId("this-is-the-feedbackGroupId");

    when(sessionRepository.findByAgencyId(15L)).thenReturn(Collections.singletonList(session));

    LocalDateTime localDateTime = LocalDateTime.now();

    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setConsultant(consultant);
    consultantAgency.setAgencyId(agencyDTO.getId());
    consultantAgency.setCreateDate(localDateTime);
    consultantAgency.setUpdateDate(localDateTime);

    when(consultantAgencyService.saveConsultantAgency(any())).thenReturn(consultantAgency);

    doThrow(new RocketChatAddUserToGroupException("Rocket.Chat test exception"))
        .when(rocketChatService).addTechnicalUserToGroup(session.getGroupId());

    try {
      ConsultantAgencyAdminResultDTO newConsultantAgency = consultantAgencyCreatorService
          .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
      fail("There was no BadRequestException");
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
      assertThat(e.getMessage(),
          is("RocketChatService error while setting up a Rocket.Chat room during consultantAgency-creation for groupId (this-is-the-group-id-with-feedbackgroup) or feedbackGroupId (this-is-the-feedbackGroupId) and consultantId (12345678-1234-1234-1234-1234567890ab)"));
    }
  }

  @Test
  public void createNewConsultantAgency_Should_returnExpectedCreatedConsultantAgency_When_ParamsAreValid()
      throws AgencyServiceHelperException {

    String consultantId = "12345678-1234-1234-1234-1234567890ab";

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("a-valid-role");

    Consultant consultant = this.easyRandom.nextObject(Consultant.class);
    Optional<Consultant> consultantOptional = Optional.of(consultant);
    when(consultantRepository.findById(consultantId)).thenReturn(consultantOptional);

    when(keycloakAdminClientHelper.userHasRole(consultantId, "a-valid-role")).thenReturn(true);

    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(false);
    when(agencyServiceHelper.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    when(sessionRepository.findByAgencyId(15L)).thenReturn(emptyList());

    LocalDateTime localDateTime = LocalDateTime.now();

    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setConsultant(consultant);
    consultantAgency.setAgencyId(agencyDTO.getId());
    consultantAgency.setCreateDate(localDateTime);
    consultantAgency.setUpdateDate(localDateTime);

    when(consultantAgencyService.saveConsultantAgency(any())).thenReturn(consultantAgency);

    ConsultantAgencyAdminResultDTO newConsultantAgency = consultantAgencyCreatorService
        .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);

    assertThat(newConsultantAgency, notNullValue());
    assertThat(newConsultantAgency.getEmbedded(), hasSize(1));

    ConsultantAgencyAdminDTO consultantAgencyAdminDTO = newConsultantAgency.getEmbedded().iterator()
        .next();

    assertThat(consultantAgencyAdminDTO.getConsultantId(), equalTo(consultant.getId()));
    assertThat(consultantAgencyAdminDTO.getAgencyId(), equalTo(agencyDTO.getId()));
    assertThat(consultantAgencyAdminDTO.getCreateDate(), equalTo(localDateTime.toString()));
    assertThat(consultantAgencyAdminDTO.getUpdateDate(), equalTo(localDateTime.toString()));
  }

  @Test
  public void createNewConsultantAgency_Should_returnBadRequestException_When_ConsultantIdIsUnknown() {

    String consultantId = "12345678-1234-1234-1234-1234567890ab";

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("a-role");

    Optional<Consultant> consultantOptional = Optional.empty();

    when(consultantRepository.findById(consultantId)).thenReturn(consultantOptional);

    try {

      ConsultantAgencyAdminResultDTO newConsultantAgency = consultantAgencyCreatorService
          .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
      fail("There was no BadRequestException");
    } catch (Exception e) {
      assertThat(e, instanceOf(BadRequestException.class));
      assertThat(e.getMessage(),
          is("Consultant with id 12345678-1234-1234-1234-1234567890ab does not exist"));
    }
  }

  @Test
  public void createNewConsultantAgency_Should_returnBadRequestException_When_ConsultantHasNotRequestedRole() {

    String consultantId = "12345678-1234-1234-1234-1234567890ab";

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("not-a-valid-role");

    Consultant consultant = this.easyRandom.nextObject(Consultant.class);
    Optional<Consultant> consultantOptional = Optional.of(consultant);
    when(consultantRepository.findById(consultantId)).thenReturn(consultantOptional);

    when(keycloakAdminClientHelper.userHasRole(consultantId, "not-a-valid-role")).thenReturn(false);

    try {

      ConsultantAgencyAdminResultDTO newConsultantAgency = consultantAgencyCreatorService
          .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
      fail("There was no BadRequestException");
    } catch (Exception e) {
      assertThat(e, instanceOf(BadRequestException.class));
      assertThat(e.getMessage(),
          is("Consultant with id 12345678-1234-1234-1234-1234567890ab does not have the role not-a-valid-role"));
    }
  }

  @Test
  public void createNewConsultantAgency_Should_throwInternalServerErrorException_When_AgencyCantBeFetched()
      throws AgencyServiceHelperException {

    String consultantId = "12345678-1234-1234-1234-1234567890ab";

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("a-valid-role");

    Consultant consultant = this.easyRandom.nextObject(Consultant.class);
    Optional<Consultant> consultantOptional = Optional.of(consultant);
    when(consultantRepository.findById(consultantId)).thenReturn(consultantOptional);

    when(keycloakAdminClientHelper.userHasRole(consultantId, "a-valid-role")).thenReturn(true);

    AgencyServiceHelperException agencyServiceHelperException = new AgencyServiceHelperException(
        new Exception());
    when(agencyServiceHelper.getAgencyWithoutCaching(15L)).thenThrow(agencyServiceHelperException);

    try {

      ConsultantAgencyAdminResultDTO newConsultantAgency = consultantAgencyCreatorService
          .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
      fail("There was no InternalServerErrorException");
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
      assertThat(e.getMessage(),
          is("AgencyService error while retrieving the agency for the ConsultantAgency-creating for agency 15"));
    }
  }

}

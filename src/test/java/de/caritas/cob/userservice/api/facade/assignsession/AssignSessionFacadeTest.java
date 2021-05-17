package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacadeTest.FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacadeTest.FIELD_NAME_ROCKET_CHAT_TECH_USERNAME;
import static de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacadeTest.FIELD_VALUE_ROCKET_CHAT_TECH_USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_WITH_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.FEEDBACKSESSION_WITH_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKET_CHAT_SYSTEM_USER_ID;
import static java.util.Arrays.asList;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.RegistrationType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatRollbackService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssignSessionFacadeTest {

  @InjectMocks
  AssignSessionFacade assignSessionFacade;
  @Mock
  SessionService sessionService;
  @Mock
  RocketChatFacade rocketChatFacade;
  @Mock
  KeycloakAdminClientService keycloakHelper;
  @Mock
  ConsultantService consultantService;
  @Mock
  RocketChatRollbackService rocketChatRollbackService;
  @Mock
  LogService logService;
  @Mock
  EmailNotificationFacade emailNotificationFacade;
  @Mock
  AuthenticatedUser authenticatedUser;
  @Mock
  SessionToConsultantVerifier sessionToConsultantVerifier;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    setField(assignSessionFacade, FIELD_NAME_ROCKET_CHAT_TECH_USERNAME,
        FIELD_VALUE_ROCKET_CHAT_TECH_USERNAME);
    setField(assignSessionFacade, FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID,
        ROCKET_CHAT_SYSTEM_USER_ID);
  }

  @Test(expected = InternalServerErrorException.class)
  public void assignSession_Should_ReturnInternalServerErrorAndLogErrorAndDoARollback_WhenAddConsultantToRcGroupFails_WhenSessionIsNoEnquiry() {
    doThrow(new InternalServerErrorException("")).when(rocketChatFacade).addUserToRocketChatGroup(
        CONSULTANT_WITH_AGENCY.getRocketChatId(), FEEDBACKSESSION_WITH_CONSULTANT.getGroupId());

    assignSessionFacade.assignSession(FEEDBACKSESSION_WITH_CONSULTANT, CONSULTANT_WITH_AGENCY);

    verify(logService, times(1)).logInternalServerError(anyString(), any());
    verify(sessionToConsultantVerifier, times(1)).verifyPreconditionsForAssignment(
        argThat(consultantSessionDTO ->
            consultantSessionDTO.getConsultant().equals(CONSULTANT_WITH_AGENCY)
                && consultantSessionDTO.getSession().equals(FEEDBACKSESSION_WITH_CONSULTANT)));
    verify(rocketChatRollbackService, times(1))
        .rollbackRemoveUsersFromRocketChatGroup(anyString(), any());
  }

  @Test
  public void assignSession_Should_removeOtherMembers_When_sessionIsNotATeamSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(false);
    session.setStatus(SessionStatus.NEW);
    session.setConsultant(null);
    session.getUser().setRcUserId("userRcId");
    session.setRegistrationType(RegistrationType.REGISTERED);
    session.setAgencyId(1L);
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setAgencyId(1L);
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    consultant.setRocketChatId("consultantRcId");
    when(this.rocketChatFacade.retrieveRocketChatMembers(anyString())).thenReturn(asList(
        new GroupMemberDTO("userRcId", null, "name", null, null),
        new GroupMemberDTO("consultantRcId", null, "name", null, null),
        new GroupMemberDTO("otherRcId", null, "name", null, null)
    ));
    Consultant consultantToRemove = new EasyRandom().nextObject(Consultant.class);
    consultantToRemove.setRocketChatId("otherRcId");
    when(this.consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(consultantToRemove));
    when(this.authenticatedUser.getUserId()).thenReturn("authenticatedUserId");

    this.assignSessionFacade.assignSession(session, consultant);

    verify(sessionToConsultantVerifier, times(1)).verifyPreconditionsForAssignment(
        argThat(consultantSessionDTO ->
            consultantSessionDTO.getConsultant().equals(consultant)
                && consultantSessionDTO.getSession().equals(session)));
    verify(this.rocketChatFacade, atLeastOnce())
        .removeUserFromGroup(eq(consultantToRemove.getRocketChatId()), eq(session.getGroupId()));
    verify(this.rocketChatFacade, atLeastOnce())
        .removeUserFromGroup(eq(consultantToRemove.getRocketChatId()),
            eq(session.getFeedbackGroupId()));
    verify(this.emailNotificationFacade, times(1))
        .sendAssignEnquiryEmailNotification(any(), any(), any());
  }

}

package de.caritas.cob.userservice.api.admin.service.rocketchat;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.function.Consumer;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatAddToGroupOperationServiceTest {

  private final EasyRandom easyRandom = new EasyRandom();

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;

  @Mock
  private Consumer<String> logMethod;

  @Test
  public void addToGroupsOrRollbackOnFailure_Should_performRocketChatGroupActions_When_paramsAreValid()
      throws Exception {

    Session session = easyRandom.nextObject(Session.class);
    session.setStatus(SessionStatus.NEW);
    Consultant consultant = easyRandom.nextObject(Consultant.class);

    RocketChatAddToGroupOperationService
        .getInstance(this.rocketChatService, this.keycloakAdminClientService, logMethod)
        .onSessions(singletonList(session))
        .withConsultant(consultant)
        .addToGroupsOrRollbackOnFailure();

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup(eq(session.getGroupId()));
    verify(this.rocketChatService, times(1)).addUserToGroup(eq(consultant.getRocketChatId()),
        eq(session.getGroupId()));
    verify(this.rocketChatService, times(1)).addUserToGroup(eq(consultant.getRocketChatId()),
        eq(session.getFeedbackGroupId()));
    verify(this.rocketChatService, times(1)).removeTechnicalUserFromGroup(eq(session.getGroupId()));
    verify(logMethod, times(2)).accept(anyString());
  }

  @Test
  public void addToGroupsOrRollbackOnFailure_Should_throwInternalErrorAndPerformRollback_When_addUserToRocketChatGroupFails()
      throws Exception {

    Session session = easyRandom.nextObject(Session.class);
    session.setStatus(SessionStatus.NEW);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    doThrow(new RuntimeException("")).when(this.logMethod).accept(anyString());
    GroupMemberDTO memberOfGroup = new GroupMemberDTO();
    memberOfGroup.set_id(consultant.getRocketChatId());
    when(this.rocketChatService.getMembersOfGroup(anyString())).thenReturn(singletonList(
        memberOfGroup));

    RocketChatAddToGroupOperationService operationService = RocketChatAddToGroupOperationService
        .getInstance(this.rocketChatService, this.keycloakAdminClientService, logMethod)
        .onSessions(singletonList(session))
        .withConsultant(consultant);

    try {
      operationService.addToGroupsOrRollbackOnFailure();
      fail("Internal Server Error was not thrown");
    } catch (InternalServerErrorException e) {
      verify(this.rocketChatService, times(1))
          .removeUserFromGroup(eq(consultant.getRocketChatId()), eq(session.getGroupId()));
      verify(this.rocketChatService, times(1)).removeUserFromGroup(eq(consultant.getRocketChatId()),
          eq(session.getFeedbackGroupId()));
    }
  }

  @Test
  public void addToGroupsOrRollbackOnFailure_Should_logErrorMessage_When_removeOfTechnicalUserFailes()
      throws Exception {

    Session session = easyRandom.nextObject(Session.class);
    session.setStatus(SessionStatus.NEW);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    doThrow(new RocketChatRemoveUserFromGroupException("")).when(this.rocketChatService)
        .removeTechnicalUserFromGroup(anyString());

    RocketChatAddToGroupOperationService
        .getInstance(this.rocketChatService, this.keycloakAdminClientService, logMethod)
        .onSessions(singletonList(session))
        .withConsultant(consultant)
        .addToGroupsOrRollbackOnFailure();

    verify(logMethod, times(1)).accept(
        eq("ERROR: Technical user could not be removed from rc group " + session.getGroupId()
            + " (enquiry)."));
  }

  @Test
  public void addToGroupsOrRollbackOnFailure_Should_throwInternalError_When_rollbackFails()
      throws Exception {

    Session session = easyRandom.nextObject(Session.class);
    session.setStatus(SessionStatus.NEW);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    doThrow(new RuntimeException("")).when(this.logMethod).accept(anyString());
    GroupMemberDTO memberOfGroup = new GroupMemberDTO();
    memberOfGroup.set_id(consultant.getRocketChatId());
    when(this.rocketChatService.getMembersOfGroup(anyString())).thenReturn(singletonList(
        memberOfGroup));
    doThrow(new RocketChatRemoveUserFromGroupException("")).when(this.rocketChatService)
        .removeUserFromGroup(anyString(), anyString());

    RocketChatAddToGroupOperationService operationService = RocketChatAddToGroupOperationService
        .getInstance(this.rocketChatService, this.keycloakAdminClientService, logMethod)
        .onSessions(singletonList(session))
        .withConsultant(consultant);
    try {
      operationService.addToGroupsOrRollbackOnFailure();
      fail("Internal Server Error was not thrown");
    } catch (InternalServerErrorException e) {
      assertThat(e.getMessage(),
          is("ERROR: Failed to rollback enquiry of group " + session.getGroupId() + ":"));
    }
  }

}

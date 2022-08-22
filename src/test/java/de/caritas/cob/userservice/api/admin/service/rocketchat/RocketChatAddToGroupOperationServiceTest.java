package de.caritas.cob.userservice.api.admin.service.rocketchat;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import java.util.function.Consumer;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatAddToGroupOperationServiceTest {

  private final EasyRandom easyRandom = new EasyRandom();

  @Mock private RocketChatFacade rocketChatFacade;

  @Mock private KeycloakService keycloakService;

  @Mock private Consumer<String> logMethod;

  @Mock private ConsultingTypeManager consultingTypeManager;

  @Test
  public void
      addToGroupsOrRollbackOnFailure_Should_performRocketChatGroupActions_When_paramsAreValid() {

    Session session = easyRandom.nextObject(Session.class);
    session.setStatus(SessionStatus.NEW);
    Consultant consultant = easyRandom.nextObject(Consultant.class);

    RocketChatAddToGroupOperationService.getInstance(
            this.rocketChatFacade, this.keycloakService, logMethod, consultingTypeManager)
        .onSessions(singletonList(session))
        .withConsultant(consultant)
        .addToGroupsOrRollbackOnFailure();

    verify(this.rocketChatFacade, times(1))
        .addUserToRocketChatGroup(eq(consultant.getRocketChatId()), eq(session.getGroupId()));
    verify(this.rocketChatFacade, times(1))
        .addUserToRocketChatGroup(
            eq(consultant.getRocketChatId()), eq(session.getFeedbackGroupId()));
    verify(logMethod, times(2)).accept(anyString());
  }

  @Test
  public void
      addToGroupsOrRollbackOnFailure_Should_throwInternalErrorAndPerformRollback_When_addUserToRocketChatGroupFails() {

    Session session = easyRandom.nextObject(Session.class);
    session.setStatus(SessionStatus.NEW);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    doThrow(new RuntimeException("")).when(this.logMethod).accept(anyString());
    GroupMemberDTO memberOfGroup = new GroupMemberDTO();
    memberOfGroup.set_id(consultant.getRocketChatId());
    when(this.rocketChatFacade.retrieveRocketChatMembers(anyString()))
        .thenReturn(singletonList(memberOfGroup));

    RocketChatAddToGroupOperationService operationService =
        RocketChatAddToGroupOperationService.getInstance(
                this.rocketChatFacade, this.keycloakService, logMethod, consultingTypeManager)
            .onSessions(singletonList(session))
            .withConsultant(consultant);

    try {
      operationService.addToGroupsOrRollbackOnFailure();
      fail("Internal Server Error was not thrown");
    } catch (InternalServerErrorException e) {
      verify(this.rocketChatFacade, times(1))
          .removeUserFromGroup(eq(consultant.getRocketChatId()), eq(session.getGroupId()));
      verify(this.rocketChatFacade, times(1))
          .removeUserFromGroup(eq(consultant.getRocketChatId()), eq(session.getFeedbackGroupId()));
    }
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      addToGroupsOrRollbackOnFailure_Should_logErrorMessage_When_removeOfTechnicalUserFailes() {

    Session session = easyRandom.nextObject(Session.class);
    session.setStatus(SessionStatus.NEW);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    doThrow(new RuntimeException(""))
        .when(this.rocketChatFacade)
        .addUserToRocketChatGroup(anyString(), anyString());

    RocketChatAddToGroupOperationService.getInstance(
            this.rocketChatFacade, this.keycloakService, logMethod, consultingTypeManager)
        .onSessions(singletonList(session))
        .withConsultant(consultant)
        .addToGroupsOrRollbackOnFailure();

    verify(logMethod, atLeastOnce()).accept(anyString());
  }

  @Test
  public void addToGroupsOrRollbackOnFailure_Should_throwInternalError_When_rollbackFails() {

    Session session = easyRandom.nextObject(Session.class);
    session.setStatus(SessionStatus.NEW);
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    doThrow(new RuntimeException("")).when(this.logMethod).accept(anyString());
    GroupMemberDTO memberOfGroup = new GroupMemberDTO();
    memberOfGroup.set_id(consultant.getRocketChatId());
    when(this.rocketChatFacade.retrieveRocketChatMembers(anyString()))
        .thenReturn(singletonList(memberOfGroup));
    doThrow(new RuntimeException(""))
        .when(this.rocketChatFacade)
        .removeUserFromGroup(anyString(), anyString());

    RocketChatAddToGroupOperationService operationService =
        RocketChatAddToGroupOperationService.getInstance(
                this.rocketChatFacade, this.keycloakService, logMethod, consultingTypeManager)
            .onSessions(singletonList(session))
            .withConsultant(consultant);
    try {
      operationService.addToGroupsOrRollbackOnFailure();
      fail("Internal Server Error was not thrown");
    } catch (InternalServerErrorException e) {
      assertThat(
          e.getMessage(),
          is("ERROR: Failed to rollback enquiry of group " + session.getGroupId() + ":"));
    }
  }
}

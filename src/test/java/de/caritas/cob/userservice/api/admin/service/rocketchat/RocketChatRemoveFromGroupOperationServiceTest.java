package de.caritas.cob.userservice.api.admin.service.rocketchat;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatRemoveFromGroupOperationServiceTest {

  private RocketChatRemoveFromGroupOperationService removeService;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;

  @Mock
  private Session session;

  @Mock
  private Consultant consultant;

  @Before
  public void setup() {
    Map<Session, List<Consultant>> sessionConsultants = new HashMap<>();
    sessionConsultants.put(session, singletonList(consultant));
    this.removeService =
        RocketChatRemoveFromGroupOperationService
            .getInstance(this.rocketChatService, this.keycloakAdminClientService)
            .onSessionConsultants(sessionConsultants);
  }

  @Test
  public void removeFromGroupsOrRollbackOnFailure_Should_notCollectMembers_When_rcUserIdIsEmpty()
      throws RocketChatGetGroupMembersException {
    this.removeService.removeFromGroupsOrRollbackOnFailure();

    verify(this.rocketChatService, times(0)).getMembersOfGroup(any());
  }

  @Test
  public void removeFromGroupsOrRollbackOnFailure_Should_executeRemove_When_rcUserIdIsGiven()
      throws Exception {
    when(this.session.getGroupId()).thenReturn("group");
    when(this.session.getFeedbackGroupId()).thenReturn("feedback");
    when(this.consultant.getRocketChatId()).thenReturn("rcId");
    GroupMemberDTO groupMemberDTO = new GroupMemberDTO();
    groupMemberDTO.set_id(this.consultant.getRocketChatId());
    when(this.rocketChatService.getMembersOfGroup(any())).thenReturn(singletonList(groupMemberDTO));

    this.removeService.removeFromGroupsOrRollbackOnFailure();

    verify(this.rocketChatService, times(1)).removeUserFromGroup("rcId", "group");
    verify(this.rocketChatService, times(1)).removeUserFromGroup("rcId", "feedback");
  }

  @Test
  public void removeFromGroupsOrRollbackOnFailure_Should_throwInternalServerError_When_rollbackFailes()
      throws Exception {
    when(this.session.getGroupId()).thenReturn("group");
    when(this.session.getStatus()).thenReturn(SessionStatus.NEW);
    when(this.consultant.getRocketChatId()).thenReturn("rcId");
    GroupMemberDTO groupMemberDTO = new GroupMemberDTO();
    groupMemberDTO.set_id(this.consultant.getRocketChatId());
    doThrow(new RuntimeException("")).when(this.rocketChatService)
        .addTechnicalUserToGroup(any());

    try {
      this.removeService.removeFromGroupsOrRollbackOnFailure();
      fail("No Exception thrown");
    } catch (InternalServerErrorException e) {
      verify(this.rocketChatService, times(0)).addUserToGroup(any(), any());
      assertThat(e.getMessage(), containsString("ERROR: Failed to rollback"));
    }
  }

  @Test
  public void removeFromGroupsOrRollbackOnFailure_Should_throwInternalServerErrorAndPerformRollback_When_error()
      throws Exception {
    when(this.session.getGroupId()).thenReturn("group");
    when(this.session.getStatus()).thenReturn(SessionStatus.NEW);
    when(this.consultant.getRocketChatId()).thenReturn("rcId");
    GroupMemberDTO groupMemberDTO = new GroupMemberDTO();
    groupMemberDTO.set_id(this.consultant.getRocketChatId());
    when(this.rocketChatService.getMembersOfGroup(any())).thenReturn(singletonList(groupMemberDTO));
    doThrow(new RocketChatRemoveUserFromGroupException("")).when(this.rocketChatService)
        .removeUserFromGroup(any(), any());

    try {
      this.removeService.removeFromGroupsOrRollbackOnFailure();
      fail("No Exception thrown");
    } catch (InternalServerErrorException e) {
      verify(this.rocketChatService, times(1)).addUserToGroup("rcId", "group");
    }
  }

}

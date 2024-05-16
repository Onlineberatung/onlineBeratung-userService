package de.caritas.cob.userservice.api.facade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mongodb.MongoClientException;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLeaveFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RocketChatFacadeTest {

  @InjectMocks private RocketChatFacade rocketChatFacade;

  @Mock private RocketChatService rocketChatService;

  @Test
  public void addUserToRocketChatGroup_Should_addUserToGroup() throws Exception {
    this.rocketChatFacade.addUserToRocketChatGroup("user", "group");

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup("group");
    verify(this.rocketChatService, times(1)).addUserToGroup("user", "group");
    verify(this.rocketChatService, times(1)).leaveFromGroupAsTechnicalUser("group");
  }

  @Test
  public void
      addUserToRocketChatGroup_Should_throwInternalServerErrorException_When_RocketChatAddUserToGroupExceptionIsThrown()
          throws RocketChatAddUserToGroupException {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          doThrow(new RocketChatAddUserToGroupException(""))
              .when(this.rocketChatService)
              .addUserToGroup(anyString(), anyString());

          this.rocketChatFacade.addUserToRocketChatGroup("user", "group");
        });
  }

  @Test
  public void
      removeSystemMessagesFromRocketChatGroup_Should_removeSystemMessagesFromRocketChatGroup()
          throws Exception {
    this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup("group");

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup("group");
    verify(this.rocketChatService, times(1)).removeSystemMessages(eq("group"), any(), any());
    verify(this.rocketChatService, times(1)).leaveFromGroupAsTechnicalUser("group");
  }

  @Test
  public void
      removeSystemMessagesFromRocketChatGroup_Should_throwInternalServerErrorException_When_RocketChatRemoveSystemMessagesExceptionIsThrown()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          doThrow(new RocketChatRemoveSystemMessagesException(""))
              .when(this.rocketChatService)
              .removeSystemMessages(anyString(), any(), any());

          this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup("group");
        });
  }

  @Test
  public void retrieveRocketChatMembers_Should_retrieveRocketChatMembers() throws Exception {
    this.rocketChatFacade.retrieveRocketChatMembers("group");

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup("group");
    verify(this.rocketChatService, times(1)).getChatUsers("group");
    verify(this.rocketChatService, times(1)).leaveFromGroupAsTechnicalUser("group");
  }

  @Test
  public void
      retrieveRocketChatMembers_Should_throwInternalServerErrorException_When_RocketChatGetGroupMembersException() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          doThrow(new MongoClientException(""))
              .when(this.rocketChatService)
              .getChatUsers(anyString());

          this.rocketChatFacade.retrieveRocketChatMembers("group");
        });
  }

  @Test
  public void addTechnicalUserToGroup_Should_addTechnicalUserToGroup() throws Exception {
    this.rocketChatFacade.addTechnicalUserToGroup("group");

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup("group");
  }

  @Test
  public void
      addTechnicalUserToGroup_Should_throwInternalServerErrorException_When_RocketChatGetGroupMembersException()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          doThrow(new RocketChatAddUserToGroupException(""))
              .when(this.rocketChatService)
              .addTechnicalUserToGroup(anyString());

          this.rocketChatFacade.addTechnicalUserToGroup("group");
        });
  }

  @Test
  public void leaveFromGroupAsTechnicalUser_Should_leaveFromGroupAsTechnicalUser()
      throws Exception {
    this.rocketChatFacade.leaveFromGroupAsTechnicalUser("group");

    verify(this.rocketChatService, times(1)).leaveFromGroupAsTechnicalUser("group");
  }

  @Test
  public void
      leaveFromGroupAsTechnicalUser_Should_throwInternalServerErrorException_When_RocketChatLeaveFromGroupException()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          doThrow(new RocketChatLeaveFromGroupException(""))
              .when(this.rocketChatService)
              .leaveFromGroupAsTechnicalUser(anyString());

          this.rocketChatFacade.leaveFromGroupAsTechnicalUser("group");
        });
  }

  @Test
  public void removeUserFromGroup_Should_removeUserFromGroup() throws Exception {
    this.rocketChatFacade.removeUserFromGroup("user", "group");

    verify(this.rocketChatService, times(1)).removeUserFromGroup("user", "group");
  }

  @Test
  public void
      removeUserFromGroup_Should_throwInternalServerErrorException_When_RocketChatRemoveUserFromGroupExceptionIsThrown()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          doThrow(new RocketChatRemoveUserFromGroupException(""))
              .when(this.rocketChatService)
              .removeUserFromGroup(anyString(), anyString());

          this.rocketChatFacade.removeUserFromGroup("user", "group");
        });
  }

  @Test
  public void getStandardMembersOfGroup_Should_getStandardMembersOfGroup() throws Exception {
    this.rocketChatFacade.getStandardMembersOfGroup("group");

    verify(this.rocketChatService, times(1)).getStandardMembersOfGroup("group");
  }

  @Test
  public void getStandardMembersOfGroup_Should_throwInternalServerError_When_rocketChatAccessFails()
      throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          doThrow(new RocketChatGetGroupMembersException(""))
              .when(this.rocketChatService)
              .getStandardMembersOfGroup(any());

          this.rocketChatFacade.getStandardMembersOfGroup("");
        });
  }

  @Test
  public void retrieveRocketChatMembers_Should_returnEmptyList_When_rcGroupIdIsNull() {
    var result = this.rocketChatFacade.retrieveRocketChatMembers(null);

    assertThat(result, hasSize(0));
  }
}

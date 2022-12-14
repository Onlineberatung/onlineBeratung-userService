package de.caritas.cob.userservice.api.facade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatFacadeTest {

  @InjectMocks private RocketChatFacade rocketChatFacade;

  @Mock private RocketChatService rocketChatService;

  @Test
  public void addUserToRocketChatGroup_Should_addUserToGroup() throws Exception {
    this.rocketChatFacade.addUserToRocketChatGroup("user", "group");

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup("group");
    verify(this.rocketChatService, times(1)).addUserToGroup("user", "group");
    verify(this.rocketChatService, times(1)).removeTechnicalUserFromGroup("group");
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      addUserToRocketChatGroup_Should_throwInternalServerErrorException_When_RocketChatAddUserToGroupExceptionIsThrown()
          throws RocketChatAddUserToGroupException {
    doThrow(new RocketChatAddUserToGroupException(""))
        .when(this.rocketChatService)
        .addUserToGroup(anyString(), anyString());

    this.rocketChatFacade.addUserToRocketChatGroup("user", "group");
  }

  @Test
  public void
      removeSystemMessagesFromRocketChatGroup_Should_removeSystemMessagesFromRocketChatGroup()
          throws Exception {
    this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup("group");

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup("group");
    verify(this.rocketChatService, times(1)).removeSystemMessages(eq("group"), any(), any());
    verify(this.rocketChatService, times(1)).removeTechnicalUserFromGroup("group");
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      removeSystemMessagesFromRocketChatGroup_Should_throwInternalServerErrorException_When_RocketChatRemoveSystemMessagesExceptionIsThrown()
          throws Exception {
    doThrow(new RocketChatRemoveSystemMessagesException(""))
        .when(this.rocketChatService)
        .removeSystemMessages(anyString(), any(), any());

    this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup("group");
  }

  @Test
  public void retrieveRocketChatMembers_Should_retrieveRocketChatMembers() throws Exception {
    this.rocketChatFacade.retrieveRocketChatMembers("group");

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup("group");
    verify(this.rocketChatService, times(1)).getMembersOfGroup("group");
    verify(this.rocketChatService, times(1)).removeTechnicalUserFromGroup("group");
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      retrieveRocketChatMembers_Should_throwInternalServerErrorException_When_RocketChatGetGroupMembersException()
          throws Exception {
    doThrow(new RocketChatGetGroupMembersException(""))
        .when(this.rocketChatService)
        .getMembersOfGroup(anyString());

    this.rocketChatFacade.retrieveRocketChatMembers("group");
  }

  @Test
  public void addTechnicalUserToGroup_Should_addTechnicalUserToGroup() throws Exception {
    this.rocketChatFacade.addTechnicalUserToGroup("group");

    verify(this.rocketChatService, times(1)).addTechnicalUserToGroup("group");
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      addTechnicalUserToGroup_Should_throwInternalServerErrorException_When_RocketChatGetGroupMembersException()
          throws Exception {
    doThrow(new RocketChatAddUserToGroupException(""))
        .when(this.rocketChatService)
        .addTechnicalUserToGroup(anyString());

    this.rocketChatFacade.addTechnicalUserToGroup("group");
  }

  @Test
  public void removeTechnicalUserFromGroup_Should_removeTechnicalUserFromGroup() throws Exception {
    this.rocketChatFacade.removeTechnicalUserFromGroup("group");

    verify(this.rocketChatService, times(1)).removeTechnicalUserFromGroup("group");
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      removeTechnicalUserFromGroup_Should_throwInternalServerErrorException_When_RocketChatRemoveUserFromGroupException()
          throws Exception {
    doThrow(new RocketChatRemoveUserFromGroupException(""))
        .when(this.rocketChatService)
        .removeTechnicalUserFromGroup(anyString());

    this.rocketChatFacade.removeTechnicalUserFromGroup("group");
  }

  @Test
  public void removeUserFromGroup_Should_removeUserFromGroup() throws Exception {
    this.rocketChatFacade.removeUserFromGroup("user", "group");

    verify(this.rocketChatService, times(1)).removeUserFromGroup("user", "group");
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      removeUserFromGroup_Should_throwInternalServerErrorException_When_RocketChatRemoveUserFromGroupExceptionIsThrown()
          throws Exception {
    doThrow(new RocketChatRemoveUserFromGroupException(""))
        .when(this.rocketChatService)
        .removeUserFromGroup(anyString(), anyString());

    this.rocketChatFacade.removeUserFromGroup("user", "group");
  }

  @Test
  public void getStandardMembersOfGroup_Should_getStandardMembersOfGroup() throws Exception {
    this.rocketChatFacade.getStandardMembersOfGroup("group");

    verify(this.rocketChatService, times(1)).getStandardMembersOfGroup("group");
  }

  @Test(expected = InternalServerErrorException.class)
  public void getStandardMembersOfGroup_Should_throwInternalServerError_When_rocketChatAccessFails()
      throws Exception {
    doThrow(new RocketChatGetGroupMembersException(""))
        .when(this.rocketChatService)
        .getStandardMembersOfGroup(any());

    this.rocketChatFacade.getStandardMembersOfGroup("");
  }

  @Test
  public void retrieveRocketChatMembers_Should_returnEmptyList_When_rcGroupIdIsNull() {
    var result = this.rocketChatFacade.retrieveRocketChatMembers(null);

    assertThat(result, hasSize(0));
  }
}

package de.caritas.cob.UserService.api.service.helper;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;

import de.caritas.cob.UserService.api.model.rocketChat.RocketChatCredentials;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.UserService.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.RocketChatService;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatRollbackHelperTest {

  private final String MEMBER_ID = "asdkds9";
  private final String STATUS = "offline";
  private final String USERNAME = "maxattacks";
  private final String NAME = "Max";
  private final String UTC_OFFSET = "0";
  private final String GROUP_ID = "xxxYYY";
  private final GroupMemberDTO GROUP_MEMBER_DTO =
      new GroupMemberDTO(MEMBER_ID, STATUS, USERNAME, NAME, UTC_OFFSET);
  private final List<GroupMemberDTO> GROUP_MEMBER_DTO_LIST = Arrays.asList(GROUP_MEMBER_DTO);
  private final GroupMemberDTO GROUP_MEMBER_DTO_2 =
      new GroupMemberDTO(MEMBER_ID + "2", STATUS, USERNAME + "2", NAME + "2", UTC_OFFSET);
  private final List<GroupMemberDTO> CURRENT_GROUP_MEMBER_DTO_LIST =
      Arrays.asList(GROUP_MEMBER_DTO, GROUP_MEMBER_DTO_2);
  private final Exception EXCEPTION = new Exception();
  private final RocketChatAddUserToGroupException RC_EXCEPTION =
      new RocketChatAddUserToGroupException(EXCEPTION);
  private final RocketChatCredentials RCC_TECHNICAL_UESR = new RocketChatCredentials(
      "dummyToken", "dummyUserID", "dummyUsername", null);

  @InjectMocks
  private RocketChatRollbackHelper rocketChatRollbackHelper;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  LogService logService;
  @Mock
  private RocketChatCredentialsHelper rcCredentialHelper;

  /**
   * Method: rollbackRemoveUsersFromRocketChatGroup
   * 
   */

  @Test
  public void rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenTechUserAddToGroupFails() {

    when(rocketChatService.getMembersOfGroup(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);
    when(rocketChatService.addTechnicalUserToGroup(Mockito.anyString())).thenReturn(false);

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RCC_TECHNICAL_UESR);

    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(GROUP_ID,
        GROUP_MEMBER_DTO_LIST);

    verify(logService, times(1)).logInternalServerError(Mockito.anyString());
  }

  @Test
  public void rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenTechUserRemoveFromGroupFails() {

    when(rocketChatService.getMembersOfGroup(Mockito.anyString()))
        .thenReturn(GROUP_MEMBER_DTO_LIST);
    when(rocketChatService.addTechnicalUserToGroup(Mockito.anyString())).thenReturn(true);
    when(rocketChatService.removeTechnicalUserFromGroup(Mockito.anyString())).thenReturn(false);

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RCC_TECHNICAL_UESR);

    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(GROUP_ID,
        GROUP_MEMBER_DTO_LIST);

    verify(logService, times(1)).logInternalServerError(Mockito.anyString());
  }

  @Test
  public void rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenAddUserToGroupFails() {

    when(rocketChatService.getMembersOfGroup(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);
    when(rocketChatService.addTechnicalUserToGroup(Mockito.anyString())).thenReturn(true);

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RCC_TECHNICAL_UESR);

    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(GROUP_ID,
        GROUP_MEMBER_DTO_LIST);

    verify(logService, times(1)).logInternalServerError(Mockito.anyString());
  }

  @Test
  public void rollbackRemoveUsersFromRocketChatGroup_Should_AddMissingUserToGroup_WhenUserWasRemovedBeforeRollback() {

    when(rocketChatService.getMembersOfGroup(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);
    when(rocketChatService.addTechnicalUserToGroup(Mockito.anyString())).thenReturn(true);
    doThrow(RC_EXCEPTION).when(rocketChatService).addUserToGroup(Mockito.anyString(),
        Mockito.anyString());

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RCC_TECHNICAL_UESR);

    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(GROUP_ID,
        GROUP_MEMBER_DTO_LIST);

    verify(logService, times(1)).logInternalServerError(Mockito.anyString(),
        Mockito.eq(RC_EXCEPTION));
  }
}

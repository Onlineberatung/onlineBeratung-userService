package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

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

  @InjectMocks
  private RocketChatRollbackHelper rocketChatRollbackHelper;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  Logger logger;
  @Mock
  private RocketChatCredentialsHelper rcCredentialHelper;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  /**
   * Method: rollbackRemoveUsersFromRocketChatGroup
   */

  @Test
  public void rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenTechUserAddToGroupFails()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);
    doThrow(new RocketChatAddUserToGroupException("error"))
        .when(rocketChatService).addTechnicalUserToGroup(anyString());

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(GROUP_ID,
        GROUP_MEMBER_DTO_LIST);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenTechUserRemoveFromGroupFails()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(Mockito.anyString()))
        .thenReturn(GROUP_MEMBER_DTO_LIST);
    doThrow(new RocketChatRemoveUserFromGroupException("error")).when(rocketChatService)
        .removeTechnicalUserFromGroup(anyString());

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(GROUP_ID,
        GROUP_MEMBER_DTO_LIST);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenAddUserToGroupFails()
      throws Exception {

    doThrow(new RocketChatRemoveUserFromGroupException("error")).when(rocketChatService)
        .removeTechnicalUserFromGroup(anyString());
    when(rocketChatService.getMembersOfGroup(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(GROUP_ID,
        GROUP_MEMBER_DTO_LIST);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void rollbackRemoveUsersFromRocketChatGroup_Should_AddMissingUserToGroup_WhenUserWasRemovedBeforeRollback()
      throws Exception {

    when(rocketChatService.getMembersOfGroup(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);
    doThrow(RC_EXCEPTION).when(rocketChatService).addUserToGroup(Mockito.anyString(),
        Mockito.anyString());

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(GROUP_ID,
        GROUP_MEMBER_DTO_LIST);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }
}

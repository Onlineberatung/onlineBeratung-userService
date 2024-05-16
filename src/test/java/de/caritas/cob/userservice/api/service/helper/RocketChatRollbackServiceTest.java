package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatRollbackService;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLeaveFromGroupException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class RocketChatRollbackServiceTest {

  private final String MEMBER_ID = "asdkds9";
  private final String STATUS = "offline";
  private final String USERNAME = "maxattacks";
  private final String NAME = "Max";
  private final String UTC_OFFSET = "0";
  private final String GROUP_ID = "xxxYYY";
  private final GroupMemberDTO GROUP_MEMBER_DTO =
      new GroupMemberDTO(MEMBER_ID, STATUS, USERNAME, NAME, UTC_OFFSET);
  private final List<GroupMemberDTO> GROUP_MEMBER_DTO_LIST = List.of(GROUP_MEMBER_DTO);
  private final GroupMemberDTO GROUP_MEMBER_DTO_2 =
      new GroupMemberDTO(MEMBER_ID + "2", STATUS, USERNAME + "2", NAME + "2", UTC_OFFSET);
  private final List<GroupMemberDTO> CURRENT_GROUP_MEMBER_DTO_LIST =
      Arrays.asList(GROUP_MEMBER_DTO, GROUP_MEMBER_DTO_2);
  private final Exception EXCEPTION = new Exception();
  private final RocketChatAddUserToGroupException RC_EXCEPTION =
      new RocketChatAddUserToGroupException(EXCEPTION);

  @InjectMocks private RocketChatRollbackService rocketChatRollbackService;
  @Mock private RocketChatService rocketChatService;
  @Mock Logger logger;
  @Mock private RocketChatCredentialsProvider rcCredentialHelper;

  @BeforeEach
  public void setup() {
    setInternalState(RocketChatRollbackService.class, "log", logger);
  }

  /** Method: rollbackRemoveUsersFromRocketChatGroup */
  @Test
  public void
      rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenTechUserAddToGroupFails()
          throws Exception {

    when(rocketChatService.getChatUsers(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);
    doThrow(new RocketChatAddUserToGroupException("error"))
        .when(rocketChatService)
        .addTechnicalUserToGroup(anyString());

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(
        GROUP_ID, GROUP_MEMBER_DTO_LIST);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void
      rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenTechUserLeaveFromGroupFails()
          throws Exception {

    when(rocketChatService.getChatUsers(Mockito.anyString())).thenReturn(GROUP_MEMBER_DTO_LIST);
    doThrow(new RocketChatLeaveFromGroupException("error"))
        .when(rocketChatService)
        .leaveFromGroupAsTechnicalUser(anyString());

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(
        GROUP_ID, GROUP_MEMBER_DTO_LIST);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void
      rollbackRemoveUsersFromRocketChatGroup_Should_LogInternalServerError_WhenAddUserToGroupFails()
          throws Exception {

    doThrow(new RocketChatLeaveFromGroupException("error"))
        .when(rocketChatService)
        .leaveFromGroupAsTechnicalUser(anyString());
    when(rocketChatService.getChatUsers(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(
        GROUP_ID, GROUP_MEMBER_DTO_LIST);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void
      rollbackRemoveUsersFromRocketChatGroup_Should_AddMissingUserToGroup_WhenUserWasRemovedBeforeRollback()
          throws Exception {

    when(rocketChatService.getChatUsers(Mockito.anyString()))
        .thenReturn(CURRENT_GROUP_MEMBER_DTO_LIST);
    doThrow(RC_EXCEPTION)
        .when(rocketChatService)
        .addUserToGroup(Mockito.anyString(), Mockito.anyString());

    when(rcCredentialHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(
        GROUP_ID, GROUP_MEMBER_DTO_LIST);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), any(Exception.class));
  }
}

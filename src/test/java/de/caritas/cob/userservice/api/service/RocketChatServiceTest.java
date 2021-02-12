package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.HTTP_STATUS_CODE_UNAUTHORIZED_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_CLEAN_ROOM_HISTORY;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_GROUP_CREATE_URL;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_GROUP_DELETE_URL;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_POST_ADD_USER_URL;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGIN;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGOUT;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_ROOMS_GET_URL;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_SUBSCRIPTIONS_GET_URL;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_API_USER_INFO;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_HEADER_AUTH_TOKEN;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_HEADER_USER_ID;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_REMOVE_USER_FROM_GROUP_URL;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_TECH_AUTH_TOKEN;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_TECH_USER_ID;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_CHAT_ADD_USER;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_CHAT_USER_LOGIN;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_CHAT_USER_LOGOUT;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_CLEAN_ROOM_HISTORY;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_GROUPS_CREATE;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_GROUPS_DELETE;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_GROUPS_REMOVE_USER;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_ROOMS_GET;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_SUBSCRIPTIONS_GET;
import static de.caritas.cob.userservice.testHelper.FieldConstants.RC_URL_USERS_INFO_GET;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.GROUP_MEMBER_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.GROUP_MEMBER_USER_1;
import static de.caritas.cob.userservice.testHelper.TestConstants.GROUP_MEMBER_USER_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_INFO_RESPONSE_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_INFO_RESPONSE_DTO_FAILED;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.UnauthorizedException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.rocketchat.StandardResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupCounterResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDeleteResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.DataDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.logout.LogoutResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketchat.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.model.rocketchat.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatServiceTest {

  private final String RC_TOKEN_HEADER_PARAMETER_NAME = "X-Auth-Token";
  private final String RC_USER_ID_HEADER_PARAMETER_NAME = "X-User-Id";
  private final String RC_TECHNICAL_USERNAME = "technical";
  private final String RC_TECHNICAL_PASSWORD = "technical";
  private final String RC_SYSTEM_USERNAME = "system";
  private final String RC_SYSTEM_PASSWORD = "system";
  private final String MESSAGE = "Lorem Ipsum";
  private final String GROUP_ID = "xxxYYY";
  private final String GROUP_NAME = "group";
  private final GroupCounterResponseDTO GROUP_COUNTER_RESPONSE_DTO =
      new GroupCounterResponseDTO(null, 0, null, null, 0, null, null, true);
  private final GroupResponseDTO EMPTY_GROUP_RESPONSE_DTO =
      new GroupResponseDTO(null, false, null, null);
  private final GroupMemberDTO GROUP_MEMBER_DTO_1 =
      new GroupMemberDTO(RC_CREDENTIALS_SYSTEM_A.getRocketChatUserId(), null, null, null, null);
  private final GroupMemberDTO GROUP_MEMBER_DTO_2 =
      new GroupMemberDTO(RC_USER_ID, null, null, null, null);
  private final GroupMemberDTO GROUP_MEMBER_DTO_3 =
      new GroupMemberDTO(RC_CREDENTIALS_TECHNICAL_A.getRocketChatUserId(), null, null, null, null);
  private final GroupMemberDTO[] GROUP_MEMBER_DTO =
      new GroupMemberDTO[]{GROUP_MEMBER_DTO_1, GROUP_MEMBER_DTO_2, GROUP_MEMBER_DTO_3};
  private final GroupMemberResponseDTO GROUP_MEMBER_RESPONSE_DTO =
      new GroupMemberResponseDTO(GROUP_MEMBER_DTO, null, null, null, true, null, null);
  private final SubscriptionsGetDTO SUBSCRIPTIONS_GET_DTO =
      new SubscriptionsGetDTO(new SubscriptionsUpdateDTO[]{}, false, null, null);
  private final RoomsGetDTO ROOMS_GET_DTO =
      new RoomsGetDTO(new RoomsUpdateDTO[]{}, true, null, null);
  private final ResponseEntity<SubscriptionsGetDTO> SUBSCRIPTIONS_GET_RESPONSE_ENTITY =
      new ResponseEntity<SubscriptionsGetDTO>(SUBSCRIPTIONS_GET_DTO, HttpStatus.OK);
  private final ResponseEntity<RoomsGetDTO> ROOMS_GET_RESPONSE_ENTITY =
      new ResponseEntity<RoomsGetDTO>(ROOMS_GET_DTO, HttpStatus.OK);
  private final ResponseEntity<GroupMemberResponseDTO> GROUP_MEMBER_RESPONSE_ENTITY =
      new ResponseEntity<GroupMemberResponseDTO>(GROUP_MEMBER_RESPONSE_DTO, HttpStatus.OK);
  private final ResponseEntity<GroupMemberResponseDTO> GROUP_MEMBER_RESPONSE_ENTITY_NOT_OK =
      new ResponseEntity<GroupMemberResponseDTO>(GROUP_MEMBER_RESPONSE_DTO, HttpStatus.BAD_REQUEST);
  private final ResponseEntity<SubscriptionsGetDTO> SUBSCRIPTIONS_GET_RESPONSE_ENTITY_NOT_OK =
      new ResponseEntity<SubscriptionsGetDTO>(SUBSCRIPTIONS_GET_DTO, HttpStatus.BAD_REQUEST);
  private final ResponseEntity<RoomsGetDTO> ROOMS_GET_RESPONSE_ENTITY_NOT_OK =
      new ResponseEntity<RoomsGetDTO>(ROOMS_GET_DTO, HttpStatus.BAD_REQUEST);
  private final String ERROR_MSG = "error";
  private final StandardResponseDTO STANDARD_RESPONSE_DTO_SUCCESS =
      new StandardResponseDTO(true, null);
  private final StandardResponseDTO STANDARD_RESPONSE_DTO_ERROR =
      new StandardResponseDTO(false, ERROR_MSG);
  private final LoginResponseDTO LOGIN_RESPONSE_DTO_TECH_USER =
      new LoginResponseDTO("status", new DataDTO(FIELD_NAME_ROCKET_CHAT_TECH_AUTH_TOKEN,
          FIELD_NAME_ROCKET_CHAT_TECH_USER_ID, null));
  private final LogoutResponseDTO LOGOUT_RESPONSE_DTO_WITH =
      new LogoutResponseDTO(null, null, null);
  private final GroupDTO GROUP_DTO =
      new GroupDTO(GROUP_ID, GROUP_NAME, null, null, 0, 0, null, null, false, false, null);
  private final GroupResponseDTO GROUP_RESPONSE_DTO =
      new GroupResponseDTO(GROUP_DTO, true, null, null);
  private final LocalDateTime DATETIME_OLDEST = nowInUtc();
  private final LocalDateTime DATETIME_LATEST = nowInUtc();
  private final String PASSWORD = "password";
  MultiValueMap<String, String> MULTI_VALUE_MAP_WITH_TECH_USER_CREDENTIALS =
      new LinkedMultiValueMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
          add("username", RC_TECHNICAL_USERNAME);
          add("password", RC_TECHNICAL_PASSWORD);
        }
      };
  MultiValueMap<String, String> MULTI_VALUE_MAP_WITH_SYSTEM_USER_CREDENTIALS =
      new LinkedMultiValueMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
          add("username", RC_SYSTEM_USERNAME);
          add("password", RC_SYSTEM_PASSWORD);
        }
      };
  @Mock
  Logger logger;
  @Mock
  RocketChatCredentialsProvider rcCredentialsHelper;
  private HttpHeaders HTTP_HEADERS = new HttpHeaders() {
    private static final long serialVersionUID = 1L;

    {
      setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }
  };
  HttpEntity<MultiValueMap<String, String>> RC_LOGIN_REQUEST_TECH_USER =
      new HttpEntity<MultiValueMap<String, String>>(MULTI_VALUE_MAP_WITH_TECH_USER_CREDENTIALS,
          HTTP_HEADERS);
  HttpEntity<MultiValueMap<String, String>> RC_LOGIN_REQUEST_SYSTEM_USER =
      new HttpEntity<MultiValueMap<String, String>>(MULTI_VALUE_MAP_WITH_SYSTEM_USER_CREDENTIALS,
          HTTP_HEADERS);
  @InjectMocks
  private RocketChatService rocketChatService;
  @Mock
  private RestTemplate restTemplate;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_HEADER_AUTH_TOKEN),
        RC_TOKEN_HEADER_PARAMETER_NAME);
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_HEADER_USER_ID),
        RC_USER_ID_HEADER_PARAMETER_NAME);
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGIN),
        RC_URL_CHAT_USER_LOGIN);
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGOUT),
        RC_URL_CHAT_USER_LOGOUT);
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_POST_ADD_USER_URL),
        RC_URL_CHAT_ADD_USER);
    FieldSetter.setField(rocketChatService, rocketChatService.getClass().getDeclaredField(
        FIELD_NAME_ROCKET_CHAT_REMOVE_USER_FROM_GROUP_URL), RC_URL_GROUPS_REMOVE_USER);
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_GROUP_DELETE_URL),
        RC_URL_GROUPS_DELETE);
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_GROUP_CREATE_URL),
        RC_URL_GROUPS_CREATE);
    FieldSetter.setField(rocketChatService, rocketChatService.getClass().getDeclaredField(
        FIELD_NAME_ROCKET_CHAT_API_CLEAN_ROOM_HISTORY), RC_URL_CLEAN_ROOM_HISTORY);
    FieldSetter.setField(rocketChatService, rocketChatService.getClass().getDeclaredField(
        FIELD_NAME_ROCKET_CHAT_API_SUBSCRIPTIONS_GET_URL), RC_URL_SUBSCRIPTIONS_GET);
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_ROOMS_GET_URL),
        RC_URL_ROOMS_GET);
    FieldSetter.setField(rocketChatService,
        rocketChatService.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_USER_INFO),
        RC_URL_USERS_INFO_GET);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  /**
   * Method: createPrivateGroup
   **/

  @Test
  public void createPrivateGroup_Should_ReturnTheGroupId_WhenRocketChatApiCallWasSuccessfully()
      throws NoSuchFieldException, SecurityException, RocketChatCreateGroupException {

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupResponseDTO>>any())).thenReturn(GROUP_RESPONSE_DTO);

    Optional<GroupResponseDTO> result =
        rocketChatService.createPrivateGroup(GROUP_NAME, RC_CREDENTIALS);

    assertTrue(result.isPresent());
    assertEquals(GROUP_ID, result.get().getGroup().getId());

  }

  @Test
  public void createPrivateGroup_Should_ThrowRocketChatCreateGroupException_WhenApiCallFailsWithAnException()
      throws NoSuchFieldException, SecurityException {

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");
    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupResponseDTO>>any())).thenThrow(httpServerErrorException);

    try {
      rocketChatService.createPrivateGroup(GROUP_NAME, RC_CREDENTIALS);
      fail("Expected exception: RocketChatCreateGroupException");
    } catch (RocketChatCreateGroupException rocketChatCreateGroupException) {
      assertTrue("Excepted RocketChatCreateGroupException thrown", true);
    }

  }

  /**
   * Method: deleteGroup
   **/

  @Test
  public void deleteGroup_Should_ReturnTrue_WhenApiCallIsSuccessfull()
      throws SecurityException {

    GroupDeleteResponseDTO response = new GroupDeleteResponseDTO(true);

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupDeleteResponseDTO>>any())).thenReturn(response);

    boolean result = rocketChatService.rollbackGroup(GROUP_ID, RC_CREDENTIALS);

    assertTrue(result);

  }

  @Test
  public void deleteGroup_Should_ReturnFalseAndLog_WhenApiCallIsNotSuccessfull()
      throws NoSuchFieldException, SecurityException {

    GroupDeleteResponseDTO response = new GroupDeleteResponseDTO(false);

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupDeleteResponseDTO>>any())).thenReturn(response);

    boolean result = rocketChatService.rollbackGroup(GROUP_ID, RC_CREDENTIALS);

    assertFalse(result);

    verify(logger, atLeastOnce())
        .error(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  public void rollbackGroup_Should_Log_WhenApiCallFailsWithAnException()
      throws SecurityException {

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");
    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupResponseDTO>>any())).thenThrow(httpServerErrorException);

    rocketChatService.rollbackGroup(GROUP_ID, RC_CREDENTIALS);

    verify(logger, atLeastOnce())
        .error(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  /**
   * Method: addUserToGroup
   **/

  @Test
  public void addUserToGroup_Should_ThrowRocketChatAddUserToGroupException_WheApiCallFails() {

    try {
      rocketChatService.addUserToGroup(RC_USER_ID, GROUP_ID);
      fail("Expected exception: RocketChatAddUserToGroupException");
    } catch (RocketChatAddUserToGroupException rcAddToGroupEx) {
      assertTrue("Excepted RocketChatAddUserToGroupException thrown", true);
    }
  }

  @Test
  public void addUserToGroup_Should_ThrowRocketChatLoginException_WhenResponseIsNotSuccessfull()
      throws RocketChatUserNotInitializedException {

    GroupResponseDTO groupResponseDTO = new GroupResponseDTO(null, false, "error", "errorType");

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupResponseDTO>>any())).thenReturn(groupResponseDTO);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    try {
      rocketChatService.addUserToGroup(RC_USER_ID, GROUP_ID);
      fail("Expected exception: RocketChatAddUserToGroupException");
    } catch (RocketChatAddUserToGroupException rcAddToGroupEx) {
      assertTrue("Excepted RocketChatAddUserToGroupException thrown", true);
    }
  }

  /**
   * Method: removeUserFromGroup
   */

  @Test
  public void removeUserFromGroup_Should_ThrowRocketChatRemoveUserFromGroupException_WhenAPICallIsNotSuccessfull()
      throws RocketChatUserNotInitializedException {

    Exception exception = new RuntimeException(MESSAGE);

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupResponseDTO>>any())).thenThrow(exception);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    try {
      rocketChatService.removeUserFromGroup(RC_USER_ID, GROUP_ID);
      fail("Expected exception: RocketChatRemoveUserFromGroupException");
    } catch (RocketChatRemoveUserFromGroupException ex) {
      assertTrue("Excepted RocketChatRemoveUserFromGroupException thrown", true);
    }
  }

  @Test
  public void removeUserFromGroup_Should_ThrowRocketChatRemoveUserFromGroupException_WhenAPIResponseIsUnsuccessfull()
      throws Exception {

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupResponseDTO>>any())).thenReturn(EMPTY_GROUP_RESPONSE_DTO);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    try {
      rocketChatService.removeUserFromGroup(RC_USER_ID, GROUP_ID);
      fail("Expected exception: RocketChatRemoveUserFromGroupException");
    } catch (RocketChatRemoveUserFromGroupException ex) {
      assertTrue("Excepted RocketChatRemoveUserFromGroupException thrown", true);
    }
  }

  /**
   * Method: getMembersOfGroup
   */

  @Test
  public void getMembersOfGroup_Should_ThrowRocketChatGetGroupMembersException_WhenAPICallIsNotSuccessfull()
      throws RocketChatUserNotInitializedException {

    Exception exception = new RuntimeException(MESSAGE);

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<GroupMemberResponseDTO>>any()))
        .thenThrow(exception);

    try {
      rocketChatService.getMembersOfGroup(GROUP_ID);
      fail("Expected exception: RocketChatGetGroupMembersException");
    } catch (RocketChatGetGroupMembersException ex) {
      assertTrue("Excepted RocketChatGetGroupMembersException thrown", true);
    }
  }

  @Test
  public void getMembersOfGroup_Should_ThrowRocketChatGetGroupMembersException_WhenAPIResponseIsUnsuccessfull()
      throws Exception {

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<GroupMemberResponseDTO>>any()))
        .thenReturn(GROUP_MEMBER_RESPONSE_ENTITY_NOT_OK);

    try {
      rocketChatService.getMembersOfGroup(GROUP_ID);
      fail("Expected exception: RocketChatGetGroupMembersException");
    } catch (RocketChatGetGroupMembersException ex) {
      assertTrue("Excepted RocketChatGetGroupMembersException thrown", true);
    }
  }

  @Test
  public void getMembersOfGroup_Should_ReturnListOfGroupMemberDTO_WhenAPICallIsSuccessfull()
      throws Exception {

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<GroupMemberResponseDTO>>any()))
        .thenReturn(GROUP_MEMBER_RESPONSE_ENTITY);

    assertThat(rocketChatService.getMembersOfGroup(GROUP_ID),
        everyItem(instanceOf(GroupMemberDTO.class)));
  }

  /**
   * Method: createPrivateGroupWithSystemUser
   **/

  @Test
  public void createPrivateGroupWithSystemUser_Should_ReturnTheGroupId_When_RocketChatApiCallWasSuccessful()
      throws Exception {

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<GroupResponseDTO>>any())).thenReturn(GROUP_RESPONSE_DTO);

    Optional<GroupResponseDTO> result =
        rocketChatService.createPrivateGroupWithSystemUser(GROUP_NAME);

    assertTrue(result.isPresent());
    assertEquals(GROUP_ID, result.get().getGroup().getId());
  }

  /**
   * Method: removeSystemMessages
   **/

  @Test
  public void removeSystemMessages_Should_ThrowRocketChatRemoveSystemMessagesException_WhenApiCallFailsWithAnException()
      throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<StandardResponseDTO>>any())).thenThrow(httpServerErrorException);

    try {
      rocketChatService.removeSystemMessages(GROUP_ID, DATETIME_OLDEST, DATETIME_LATEST);
      fail("Expected exception: RocketChatRemoveSystemMessagesException");
    } catch (RocketChatRemoveSystemMessagesException rocketChatRemoveSystemMessagesException) {
      assertTrue("Excepted RocketChatRemoveSystemMessagesException thrown", true);
    }
  }

  @Test
  public void removeSystemMessages_Should_ThrowRocketChatRemoveSystemMessagesException_WhenDateFormatIsWrong()
      throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    try {
      rocketChatService.removeSystemMessages(GROUP_ID, DATETIME_OLDEST, null);
      fail("Expected exception: RocketChatRemoveSystemMessagesException");
    } catch (RocketChatRemoveSystemMessagesException rocketChatRemoveSystemMessagesException) {
      assertTrue("Excepted RocketChatRemoveSystemMessagesException thrown", true);
    }
  }

  @Test(expected = RocketChatRemoveSystemMessagesException.class)
  public void removeSystemMessages_Should_ReturnFalseAndLogError_WhenApiCallIsUnsuccessfull()
      throws Exception {

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<StandardResponseDTO>>any()))
        .thenReturn(STANDARD_RESPONSE_DTO_ERROR);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatService.removeSystemMessages(GROUP_ID, DATETIME_OLDEST, DATETIME_LATEST);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void removeSystemMessages_Should_NotThrowException_WhenApiCallIsSuccessfull()
      throws Exception {

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<StandardResponseDTO>>any()))
        .thenReturn(STANDARD_RESPONSE_DTO_SUCCESS);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatService.removeSystemMessages(GROUP_ID, DATETIME_OLDEST, DATETIME_LATEST);
  }

  /**
   * Method: getUserId
   **/

  @Test
  public void getUserId_Should_LoginUser() throws RocketChatLoginException {

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        any(), ArgumentMatchers.<Class<LoginResponseDTO>>any())).thenReturn(
        new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_TECH_USER, HttpStatus.OK));

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGOUT),
        any(), ArgumentMatchers.<Class<LogoutResponseDTO>>any())).thenReturn(
        new ResponseEntity<LogoutResponseDTO>(LOGOUT_RESPONSE_DTO_WITH, HttpStatus.OK));

    rocketChatService.getUserID(USERNAME, PASSWORD, false);

    verify(restTemplate, times(1)).postForEntity(Mockito.eq(RC_URL_CHAT_USER_LOGIN),
        any(), Mockito.eq(LoginResponseDTO.class));

  }

  @Test
  public void getUserId_Should_LogoutUser() throws RocketChatLoginException {

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        any(), ArgumentMatchers.<Class<LoginResponseDTO>>any())).thenReturn(
        new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_TECH_USER, HttpStatus.OK));

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGOUT),
        any(), ArgumentMatchers.<Class<LogoutResponseDTO>>any())).thenReturn(
        new ResponseEntity<LogoutResponseDTO>(LOGOUT_RESPONSE_DTO_WITH, HttpStatus.OK));

    rocketChatService.getUserID(USERNAME, PASSWORD, false);

    verify(restTemplate, times(1)).postForEntity(Mockito.eq(RC_URL_CHAT_USER_LOGOUT),
        any(), Mockito.eq(LogoutResponseDTO.class));

  }

  @Test
  public void getUserId_Should_ReturnCorrectUserId() throws RocketChatLoginException {

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        any(), ArgumentMatchers.<Class<LoginResponseDTO>>any())).thenReturn(
        new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_TECH_USER, HttpStatus.OK));

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGOUT),
        any(), ArgumentMatchers.<Class<LogoutResponseDTO>>any())).thenReturn(
        new ResponseEntity<LogoutResponseDTO>(LOGOUT_RESPONSE_DTO_WITH, HttpStatus.OK));

    String result = rocketChatService.getUserID(USERNAME, PASSWORD, false);

    assertEquals(LOGIN_RESPONSE_DTO_TECH_USER.getData().getUserId(), result);

  }

  /**
   * Method: getSubscriptionsOfUser
   */

  @Test
  public void getSubscriptionsOfUser_Should_ThrowInternalServerErrorException_When_APICallIsNotSuccessfull() {

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<SubscriptionsGetDTO>>any()))
        .thenThrow(HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR_EXCEPTION);

    try {
      rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException ex) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getSubscriptionsOfUser_Should_ThrowInternalServerErrorException_When_APIResponseIsUnsuccessfull() {

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<SubscriptionsGetDTO>>any()))
        .thenReturn(SUBSCRIPTIONS_GET_RESPONSE_ENTITY_NOT_OK);

    try {
      rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException ex) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getSubscriptionsOfUser_Should_ThrowUnauthorizedException_When_RocketChatReturnsUnauthorized() {

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<SubscriptionsGetDTO>>any()))
        .thenThrow(HTTP_STATUS_CODE_UNAUTHORIZED_EXCEPTION);

    try {
      rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS);
      fail("Expected exception: UnauthorizedException");
    } catch (UnauthorizedException ex) {
      assertTrue("Excepted UnauthorizedException thrown", true);
    }
  }

  @Test
  public void getSubscriptionsOfUser_Should_ReturnListOfSubscriptionsUpdateDTO_When_APICallIsSuccessfull() {

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<SubscriptionsGetDTO>>any()))
        .thenReturn(SUBSCRIPTIONS_GET_RESPONSE_ENTITY);

    assertThat(rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS),
        everyItem(instanceOf(SubscriptionsUpdateDTO.class)));
  }

  /**
   * Method: getRoomsOfUser
   */

  @Test
  public void getRoomsOfUser_Should_ThrowInternalServerErrorException_When_APICallIsNotSuccessfull() {

    Exception exception = new RuntimeException(MESSAGE);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<RoomsGetDTO>>any())).thenThrow(exception);

    try {
      rocketChatService.getRoomsOfUser(RC_CREDENTIALS);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException ex) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getRoomsOfUser_Should_ThrowInternalServerErrorException_When_APIResponseIsUnsuccessfull() {

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<RoomsGetDTO>>any()))
        .thenReturn(ROOMS_GET_RESPONSE_ENTITY_NOT_OK);

    try {
      rocketChatService.getRoomsOfUser(RC_CREDENTIALS);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException ex) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getRoomsOfUser_Should_ReturnListOfRoomsUpdateDTO_WhenAPICallIsSuccessfull()
      throws Exception {

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<RoomsGetDTO>>any()))
        .thenReturn(ROOMS_GET_RESPONSE_ENTITY);

    assertThat(rocketChatService.getRoomsOfUser(RC_CREDENTIALS),
        everyItem(instanceOf(RoomsUpdateDTO.class)));
  }

  /**
   * Method: removeAllStandardUsersFromGroup
   **/

  @Test
  public void removeAllStandardUsersFromGroup_Should_ThrowRocketChatGetGroupMembersException_WhenGroupListIsEmpty()
      throws Exception {

    RocketChatService spy = Mockito.spy(rocketChatService);

    Mockito.doReturn(new ArrayList<GroupMemberDTO>()).when(spy)
        .getMembersOfGroup(Mockito.anyString());

    try {
      spy.removeAllStandardUsersFromGroup(GROUP_ID);
      fail("Expected exception: RocketChatGetGroupMembersException");
    } catch (RocketChatGetGroupMembersException rocketChatGetGroupMembersException) {
      assertTrue("Excepted RocketChatGetGroupMembersException thrown", true);
    }
  }

  @Test
  public void removeAllStandardUsersFromGroup_Should_RemoveAllStandardUsersAndNotTechnicalOrSystemUser()
      throws Exception {

    RocketChatService spy = Mockito.spy(rocketChatService);

    Mockito.doReturn(GROUP_MEMBER_DTO_LIST).when(spy).getMembersOfGroup(Mockito.anyString());

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    spy.removeAllStandardUsersFromGroup(GROUP_ID);

    verify(spy, times(0)).removeUserFromGroup(RC_CREDENTIALS_SYSTEM_A.getRocketChatUserId(),
        GROUP_ID);
    verify(spy, times(0)).removeUserFromGroup(RC_CREDENTIALS_TECHNICAL_A.getRocketChatUserId(),
        GROUP_ID);
    verify(spy, times(1)).removeUserFromGroup(GROUP_MEMBER_USER_1.get_id(), GROUP_ID);
    verify(spy, times(1)).removeUserFromGroup(GROUP_MEMBER_USER_2.get_id(), GROUP_ID);
  }

  /**
   * Method: removeAllMessages
   **/

  @Test
  public void removeAllMessages_Should_NotThrowException_WhenRemoveMessagesSucceeded()
      throws Exception {

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<StandardResponseDTO>>any()))
        .thenReturn(STANDARD_RESPONSE_DTO_SUCCESS);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatService.removeAllMessages(GROUP_ID);
  }

  @Test(expected = RocketChatRemoveSystemMessagesException.class)
  public void removeAllMessages_Should_ThrowRocketChatRemoveSystemMessagesException_WhenRemoveMessagesFails()
      throws Exception {

    when(restTemplate.postForObject(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<StandardResponseDTO>>any()))
        .thenReturn(STANDARD_RESPONSE_DTO_ERROR);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    rocketChatService.removeAllMessages(GROUP_ID);
  }

  /**
   * Method: getStandardMembersOfGroup
   */
  @Test
  public void getStandardMembersOfGroup_Should_ThrowRocketChatGetGroupMembersException_WhenAPICallIsNotSuccessfull()
      throws RocketChatUserNotInitializedException {

    Exception exception = new RuntimeException(MESSAGE);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<GroupMemberResponseDTO>>any()))
        .thenThrow(exception);

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);

    try {
      rocketChatService.getStandardMembersOfGroup(GROUP_ID);
      fail("Expected exception: RocketChatGetGroupMembersException");
    } catch (RocketChatGetGroupMembersException | RocketChatUserNotInitializedException ex) {
      assertTrue("Excepted RocketChatGetGroupMembersException thrown", true);
    }
  }

  @Test
  public void getStandardMembersOfGroup_Should_ThrowRocketChatGetGroupMembersException_WhenAPIResponseIsUnsuccessfull()
      throws Exception {

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<GroupMemberResponseDTO>>any()))
        .thenReturn(GROUP_MEMBER_RESPONSE_ENTITY_NOT_OK);

    try {
      rocketChatService.getStandardMembersOfGroup(GROUP_ID);
      fail("Expected exception: RocketChatGetGroupMembersException");
    } catch (RocketChatGetGroupMembersException ex) {
      assertTrue("Excepted RocketChatGetGroupMembersException thrown", true);
    }
  }

  @Test
  public void getStandardMembersOfGroup_Should_ReturnListFilteredOfGroupMemberDTO_WhenAPICallIsSuccessfull()
      throws Exception {

    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<GroupMemberResponseDTO>>any()))
        .thenReturn(GROUP_MEMBER_RESPONSE_ENTITY);

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    List<GroupMemberDTO> result = rocketChatService.getStandardMembersOfGroup(GROUP_ID);

    assertTrue(result.size() == 1);
    assertTrue(result.get(0).get_id() != RC_CREDENTIALS_TECHNICAL_A.getRocketChatUserId());
    assertTrue(result.get(0).get_id() != RC_CREDENTIALS_SYSTEM_A.getRocketChatUserId());
  }

  /**
   * Method: getUserInfo
   */
  @Test(expected = InternalServerErrorException.class)
  public void getUserInfo_Should_ThrowInternalServerExceptionException_WhenAPICallFails()
      throws RocketChatUserNotInitializedException {

    Exception exception = new RestClientResponseException(MESSAGE, HttpStatus.BAD_REQUEST.value(),
        ERROR, null, null, null);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<UserInfoResponseDTO>>any(), anyString()))
        .thenThrow(exception);

    rocketChatService.getUserInfo(RC_USER_ID);

  }

  @Test(expected = InternalServerErrorException.class)
  public void getUserInfo_Should_ThrowInternalServerErrorException_WhenAPICallIsNotSuccessfull()
      throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate
        .exchange(anyString(), eq(HttpMethod.GET), any(), eq(UserInfoResponseDTO.class),
            anyString()))
        .thenReturn(new ResponseEntity<>(USER_INFO_RESPONSE_DTO_FAILED, HttpStatus.OK));

    rocketChatService.getUserInfo(RC_USER_ID);
  }

  @Test
  public void getUserInfo_Should_ReturnUserInfoResponseDTOWithSameUserId_WhenAPICallIsSuccessfull()
      throws Exception {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(ArgumentMatchers.anyString(), any(),
        any(), ArgumentMatchers.<Class<UserInfoResponseDTO>>any(), anyString())).thenReturn(
        new ResponseEntity<>(USER_INFO_RESPONSE_DTO, HttpStatus.OK));

    UserInfoResponseDTO result = rocketChatService.getUserInfo(RC_USER_ID);

    assertEquals(result.getUser().getId(), RC_USER_ID);
  }
}

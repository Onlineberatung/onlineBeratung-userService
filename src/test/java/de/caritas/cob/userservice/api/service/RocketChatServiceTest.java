package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.testHelper.ExceptionConstants.HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR_EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.ExceptionConstants.HTTP_STATUS_CODE_UNAUTHORIZED_EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_TECH_AUTH_TOKEN;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_TECH_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.RC_URL_CHAT_USER_DELETE;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.RC_URL_CHAT_USER_LOGOUT;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.RC_URL_CHAT_USER_UPDATE;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.RC_URL_GROUPS_DELETE;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.RC_URL_GROUPS_SET_READ_ONLY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_MEMBER_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_MEMBER_USER_1;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_MEMBER_USER_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKET_CHAT_USER_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKET_CHAT_USER_DTO_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_INFO_RESPONSE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_INFO_RESPONSE_DTO_FAILED;
import static java.util.Objects.nonNull;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.config.RocketChatConfig;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.StandardResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDeleteResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupsListAllResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.DataDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.logout.LogoutResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.SetRoomReadOnlyBodyDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UsersListReponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.RocketChatUnauthorizedException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteUserException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupsListAllException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetUserIdException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RocketChatServiceTest {

  private final String MESSAGE = "Lorem Ipsum";
  private final String GROUP_ID = "xxxYYY";
  private final String GROUP_NAME = "group";
  private final GroupResponseDTO EMPTY_GROUP_RESPONSE_DTO =
      new GroupResponseDTO(null, false, null, null);
  private final SubscriptionsGetDTO SUBSCRIPTIONS_GET_DTO =
      new SubscriptionsGetDTO(new SubscriptionsUpdateDTO[] {}, false, null, null);
  private final RoomsGetDTO ROOMS_GET_DTO =
      new RoomsGetDTO(new RoomsUpdateDTO[] {}, true, null, null);
  private final ResponseEntity<SubscriptionsGetDTO> SUBSCRIPTIONS_GET_RESPONSE_ENTITY =
      new ResponseEntity<>(SUBSCRIPTIONS_GET_DTO, HttpStatus.OK);
  private final ResponseEntity<RoomsGetDTO> ROOMS_GET_RESPONSE_ENTITY =
      new ResponseEntity<>(ROOMS_GET_DTO, HttpStatus.OK);
  private final ResponseEntity<SubscriptionsGetDTO> SUBSCRIPTIONS_GET_RESPONSE_ENTITY_NOT_OK =
      new ResponseEntity<>(SUBSCRIPTIONS_GET_DTO, HttpStatus.BAD_REQUEST);
  private final ResponseEntity<RoomsGetDTO> ROOMS_GET_RESPONSE_ENTITY_NOT_OK =
      new ResponseEntity<>(ROOMS_GET_DTO, HttpStatus.BAD_REQUEST);
  private final String ERROR_MSG = "error";
  private final StandardResponseDTO STANDARD_RESPONSE_DTO_SUCCESS =
      new StandardResponseDTO(true, null);
  private final StandardResponseDTO STANDARD_RESPONSE_DTO_ERROR =
      new StandardResponseDTO(false, ERROR_MSG);
  private final LoginResponseDTO LOGIN_RESPONSE_DTO_TECH_USER =
      new LoginResponseDTO(
          "status",
          new DataDTO(
              FIELD_NAME_ROCKET_CHAT_TECH_AUTH_TOKEN, FIELD_NAME_ROCKET_CHAT_TECH_USER_ID, null));
  private final LogoutResponseDTO LOGOUT_RESPONSE_DTO_WITH =
      new LogoutResponseDTO(null, null, null);
  private final GroupDTO GROUP_DTO =
      new GroupDTO(GROUP_ID, GROUP_NAME, null, null, 0, 0, null, null, false, false, null);
  private final GroupDTO GROUP_DTO_2 =
      new GroupDTO(RC_GROUP_ID_2, GROUP_NAME, null, null, 0, 0, null, null, false, false, null);
  private final GroupResponseDTO GROUP_RESPONSE_DTO =
      new GroupResponseDTO(GROUP_DTO, true, null, null);
  private final UsersListReponseDTO USERS_LIST_RESPONSE_DTO_EMPTY =
      new UsersListReponseDTO(new RocketChatUserDTO[0]);
  private final UsersListReponseDTO USERS_LIST_RESPONSE_DTO =
      new UsersListReponseDTO(new RocketChatUserDTO[] {ROCKET_CHAT_USER_DTO});
  private final UsersListReponseDTO USERS_LIST_RESPONSE_DTO_WITH_2_USERS =
      new UsersListReponseDTO(
          new RocketChatUserDTO[] {ROCKET_CHAT_USER_DTO, ROCKET_CHAT_USER_DTO_2});
  private final GroupsListAllResponseDTO GROUPS_LIST_ALL_RESPONSE_DTO_EMPTY =
      new GroupsListAllResponseDTO(new GroupDTO[0], 1, 0, 0);
  private final GroupsListAllResponseDTO GROUPS_LIST_ALL_RESPONSE_DTO =
      new GroupsListAllResponseDTO(new GroupDTO[] {GROUP_DTO, GROUP_DTO_2}, 0, 2, 10);

  private final GroupsListAllResponseDTO GROUPS_LIST_ALL_RESPONSE_DTO_PAGINATED =
      new GroupsListAllResponseDTO(new GroupDTO[] {GROUP_DTO, GROUP_DTO_2}, 0, 100, 1000);

  private final GroupsListAllResponseDTO
      GROUPS_LIST_ALL_RESPONSE_DTO_PAGINATED_WITH_TOTAL_ZERO_ELEMENTS =
          new GroupsListAllResponseDTO(new GroupDTO[] {GROUP_DTO, GROUP_DTO_2}, 0, 0, 0);
  private final LocalDateTime DATETIME_OLDEST = nowInUtc();
  private final LocalDateTime DATETIME_LATEST = nowInUtc();
  private final String PASSWORD = "password";
  private final RocketChatConfig rocketChatConfig =
      new RocketChatConfig(new MockHttpServletRequest());
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Mock Logger logger;
  @Mock RocketChatCredentialsProvider rcCredentialsHelper;
  @InjectMocks private RocketChatService rocketChatService;
  @Mock private RestTemplate restTemplate;
  @Mock private MongoClient mockedMongoClient;

  @Mock private MongoDatabase mongoDatabase;

  @Mock private MongoCollection<Document> mongoCollection;

  @Mock private MongoCursor<Document> mongoCursor;

  @Mock private FindIterable<Document> findIterable;

  @BeforeEach
  void setup() {
    rocketChatConfig.setBaseUrl("http://localhost/api/v1");
    setField(rocketChatService, "rocketChatConfig", rocketChatConfig);

    setInternalState(RocketChatService.class, "log", logger);
  }

  /** Method: createPrivateGroup */
  @Test
  void createPrivateGroup_Should_ReturnTheGroupId_WhenRocketChatApiCallWasSuccessfully()
      throws SecurityException, RocketChatCreateGroupException {

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<GroupResponseDTO>>any()))
        .thenReturn(GROUP_RESPONSE_DTO);

    Optional<GroupResponseDTO> result =
        rocketChatService.createPrivateGroup(GROUP_NAME, RC_CREDENTIALS);

    assertTrue(result.isPresent());
    assertEquals(GROUP_ID, result.get().getGroup().getId());
  }

  @Test
  void
      createPrivateGroup_Should_ThrowRocketChatCreateGroupException_WhenApiCallFailsWithAnException()
          throws SecurityException {

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");
    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<GroupResponseDTO>>any()))
        .thenThrow(httpServerErrorException);

    try {
      rocketChatService.createPrivateGroup(GROUP_NAME, RC_CREDENTIALS);
      fail("Expected exception: RocketChatCreateGroupException");
    } catch (RocketChatCreateGroupException rocketChatCreateGroupException) {
      assertTrue(true, "Excepted RocketChatCreateGroupException thrown");
    }
  }

  /** Method: deleteGroup */
  @Test
  void deleteGroup_Should_ReturnTrue_WhenApiCallIsSuccessful() throws SecurityException {

    GroupDeleteResponseDTO response = new GroupDeleteResponseDTO(true);

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(),
            any(),
            ArgumentMatchers.<Class<GroupDeleteResponseDTO>>any()))
        .thenReturn(response);

    boolean result = rocketChatService.rollbackGroup(GROUP_ID, RC_CREDENTIALS);

    assertTrue(result);
  }

  @Test
  void deleteGroup_Should_ReturnFalseAndLog_WhenApiCallIsNotSuccessful() throws SecurityException {

    GroupDeleteResponseDTO response = new GroupDeleteResponseDTO(false);

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(),
            any(),
            ArgumentMatchers.<Class<GroupDeleteResponseDTO>>any()))
        .thenReturn(response);

    boolean result = rocketChatService.rollbackGroup(GROUP_ID, RC_CREDENTIALS);

    assertFalse(result);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  void rollbackGroup_Should_Log_WhenApiCallFailsWithAnException() throws SecurityException {

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");
    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<GroupResponseDTO>>any()))
        .thenThrow(httpServerErrorException);

    rocketChatService.rollbackGroup(GROUP_ID, RC_CREDENTIALS);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), any(Exception.class));
  }

  /** Method: addUserToGroup */
  @Test
  void addUserToGroup_Should_ThrowRocketChatAddUserToGroupException_WheApiCallFails() {

    try {
      rocketChatService.addUserToGroup(RC_USER_ID, GROUP_ID);
      fail("Expected exception: RocketChatAddUserToGroupException");
    } catch (RocketChatAddUserToGroupException rcAddToGroupEx) {
      assertTrue(true, "Excepted RocketChatAddUserToGroupException thrown");
    }
  }

  @Test
  void addUserToGroup_Should_ThrowRocketChatLoginException_WhenResponseIsNotSuccessful()
      throws RocketChatUserNotInitializedException {

    GroupResponseDTO groupResponseDTO = new GroupResponseDTO(null, false, "error", "errorType");

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<GroupResponseDTO>>any()))
        .thenReturn(groupResponseDTO);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    try {
      rocketChatService.addUserToGroup(RC_USER_ID, GROUP_ID);
      fail("Expected exception: RocketChatAddUserToGroupException");
    } catch (RocketChatAddUserToGroupException rcAddToGroupEx) {
      assertTrue(true, "Excepted RocketChatAddUserToGroupException thrown");
    }
  }

  /** Method: removeUserFromGroup */
  @Test
  void
      removeUserFromGroup_Should_ThrowRocketChatRemoveUserFromGroupException_WhenAPICallIsNotSuccessful()
          throws RocketChatUserNotInitializedException {

    Exception exception = new RuntimeException(MESSAGE);

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<GroupResponseDTO>>any()))
        .thenThrow(exception);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    try {
      rocketChatService.removeUserFromGroup(RC_USER_ID, GROUP_ID);
      fail("Expected exception: RocketChatRemoveUserFromGroupException");
    } catch (RocketChatRemoveUserFromGroupException ex) {
      assertTrue(true, "Excepted RocketChatRemoveUserFromGroupException thrown");
    }
  }

  @Test
  void
      removeUserFromGroup_Should_ThrowRocketChatRemoveUserFromGroupException_WhenAPIResponseIsUnSuccessful()
          throws Exception {

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<GroupResponseDTO>>any()))
        .thenReturn(EMPTY_GROUP_RESPONSE_DTO);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    try {
      rocketChatService.removeUserFromGroup(RC_USER_ID, GROUP_ID);
      fail("Expected exception: RocketChatRemoveUserFromGroupException");
    } catch (RocketChatRemoveUserFromGroupException ex) {
      assertTrue(true, "Excepted RocketChatRemoveUserFromGroupException thrown");
    }
  }

  /** Method: createPrivateGroupWithSystemUser */
  @Test
  void
      createPrivateGroupWithSystemUser_Should_ReturnTheGroupId_When_RocketChatApiCallWasSuccessful()
          throws Exception {

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<GroupResponseDTO>>any()))
        .thenReturn(GROUP_RESPONSE_DTO);

    Optional<GroupResponseDTO> result =
        rocketChatService.createPrivateGroupWithSystemUser(GROUP_NAME);

    assertTrue(result.isPresent());
    assertEquals(GROUP_ID, result.get().getGroup().getId());
  }

  /** Method: removeSystemMessages */
  @Test
  void
      removeSystemMessages_Should_ThrowRocketChatRemoveSystemMessagesException_WhenApiCallFailsWithAnException()
          throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(),
            any(),
            ArgumentMatchers.<Class<StandardResponseDTO>>any()))
        .thenThrow(httpServerErrorException);

    try {
      rocketChatService.removeSystemMessages(GROUP_ID, DATETIME_OLDEST, DATETIME_LATEST);
      fail("Expected exception: RocketChatRemoveSystemMessagesException");
    } catch (RocketChatRemoveSystemMessagesException rocketChatRemoveSystemMessagesException) {
      assertTrue(true, "Excepted RocketChatRemoveSystemMessagesException thrown");
    }
  }

  @Test
  void
      removeSystemMessages_Should_ThrowRocketChatRemoveSystemMessagesException_WhenDateFormatIsWrong()
          throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    try {
      rocketChatService.removeSystemMessages(GROUP_ID, DATETIME_OLDEST, null);
      fail("Expected exception: RocketChatRemoveSystemMessagesException");
    } catch (RocketChatRemoveSystemMessagesException rocketChatRemoveSystemMessagesException) {
      assertTrue(true, "Excepted RocketChatRemoveSystemMessagesException thrown");
    }
  }

  @Test
  void removeSystemMessages_Should_ReturnFalseAndLogError_WhenApiCallIsUnSuccessful()
      throws Exception {
    assertThrows(
        RocketChatRemoveSystemMessagesException.class,
        () -> {
          when(restTemplate.postForObject(
                  ArgumentMatchers.anyString(),
                  any(),
                  ArgumentMatchers.<Class<StandardResponseDTO>>any()))
              .thenReturn(STANDARD_RESPONSE_DTO_ERROR);

          when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

          rocketChatService.removeSystemMessages(GROUP_ID, DATETIME_OLDEST, DATETIME_LATEST);

          verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
        });
  }

  @Test
  void removeSystemMessages_Should_NotThrowException_WhenApiCallIsSuccessful() throws Exception {

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(),
            any(),
            ArgumentMatchers.<Class<StandardResponseDTO>>any()))
        .thenReturn(STANDARD_RESPONSE_DTO_SUCCESS);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    assertDoesNotThrow(
        () -> rocketChatService.removeSystemMessages(GROUP_ID, DATETIME_OLDEST, DATETIME_LATEST));
  }

  /** Method: getUserId */
  @Test
  void getUserId_Should_LoginUser() throws RocketChatLoginException {

    when(rcCredentialsHelper.loginUser(any(), any()))
        .thenReturn(new ResponseEntity<>(LOGIN_RESPONSE_DTO_TECH_USER, HttpStatus.OK));

    when(restTemplate.postForEntity(
            ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGOUT),
            any(),
            ArgumentMatchers.<Class<LogoutResponseDTO>>any()))
        .thenReturn(new ResponseEntity<>(LOGOUT_RESPONSE_DTO_WITH, HttpStatus.OK));

    rocketChatService.getUserID(USERNAME, PASSWORD, false);

    verify(this.rcCredentialsHelper, times(1)).loginUser(USERNAME, PASSWORD);
  }

  @Test
  void getUserId_Should_LogoutUser() throws RocketChatLoginException {

    when(rcCredentialsHelper.loginUser(any(), any()))
        .thenReturn(new ResponseEntity<>(LOGIN_RESPONSE_DTO_TECH_USER, HttpStatus.OK));

    when(restTemplate.postForEntity(
            ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGOUT),
            any(),
            ArgumentMatchers.<Class<LogoutResponseDTO>>any()))
        .thenReturn(new ResponseEntity<>(LOGOUT_RESPONSE_DTO_WITH, HttpStatus.OK));

    rocketChatService.getUserID(USERNAME, PASSWORD, false);

    verify(this.rcCredentialsHelper, times(1)).loginUser(USERNAME, PASSWORD);
  }

  @Test
  void getUserId_Should_ReturnCorrectUserId() throws RocketChatLoginException {

    when(rcCredentialsHelper.loginUser(any(), any()))
        .thenReturn(new ResponseEntity<>(LOGIN_RESPONSE_DTO_TECH_USER, HttpStatus.OK));

    when(restTemplate.postForEntity(
            ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGOUT),
            any(),
            ArgumentMatchers.<Class<LogoutResponseDTO>>any()))
        .thenReturn(new ResponseEntity<>(LOGOUT_RESPONSE_DTO_WITH, HttpStatus.OK));

    String result = rocketChatService.getUserID(USERNAME, PASSWORD, false);

    assertEquals(LOGIN_RESPONSE_DTO_TECH_USER.getData().getUserId(), result);
  }

  /** Method: getSubscriptionsOfUser */
  @Test
  void
      getSubscriptionsOfUser_Should_ThrowInternalServerErrorException_When_APICallIsNotSuccessful() {

    when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            any(),
            any(),
            ArgumentMatchers.<Class<SubscriptionsGetDTO>>any()))
        .thenThrow(HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR_EXCEPTION);

    try {
      rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException ex) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  void
      getSubscriptionsOfUser_Should_ThrowInternalServerErrorException_When_APIResponseIsUnSuccessful() {

    when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            any(),
            any(),
            ArgumentMatchers.<Class<SubscriptionsGetDTO>>any()))
        .thenReturn(SUBSCRIPTIONS_GET_RESPONSE_ENTITY_NOT_OK);

    try {
      rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException ex) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  void
      getSubscriptionsOfUser_Should_ThrowUnauthorizedException_When_RocketChatReturnsUnauthorized() {

    when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            any(),
            any(),
            ArgumentMatchers.<Class<SubscriptionsGetDTO>>any()))
        .thenThrow(HTTP_STATUS_CODE_UNAUTHORIZED_EXCEPTION);

    var thrown =
        assertThrows(
            RocketChatUnauthorizedException.class,
            () -> rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS));

    var prefix = "Could not get Rocket.Chat subscriptions for user ID";
    assertTrue(thrown.getMessage().startsWith(prefix));
  }

  @Test
  void getSubscriptionsOfUser_Should_ReturnListOfSubscriptionsUpdateDTO_When_APICallIsSuccessful() {

    when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            any(),
            any(),
            ArgumentMatchers.<Class<SubscriptionsGetDTO>>any()))
        .thenReturn(SUBSCRIPTIONS_GET_RESPONSE_ENTITY);

    assertThat(
        rocketChatService.getSubscriptionsOfUser(RC_CREDENTIALS),
        everyItem(instanceOf(SubscriptionsUpdateDTO.class)));
  }

  /** Method: getRoomsOfUser */
  @Test
  void getRoomsOfUser_Should_ThrowInternalServerErrorException_When_APICallIsNotSuccessful() {

    Exception exception = new RuntimeException(MESSAGE);

    when(restTemplate.exchange(
            ArgumentMatchers.anyString(), any(), any(), ArgumentMatchers.<Class<RoomsGetDTO>>any()))
        .thenThrow(exception);

    try {
      rocketChatService.getRoomsOfUser(RC_CREDENTIALS);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException ex) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  void getRoomsOfUser_Should_ThrowInternalServerErrorException_When_APIResponseIsUnSuccessful() {

    when(restTemplate.exchange(
            ArgumentMatchers.anyString(), any(), any(), ArgumentMatchers.<Class<RoomsGetDTO>>any()))
        .thenReturn(ROOMS_GET_RESPONSE_ENTITY_NOT_OK);

    try {
      rocketChatService.getRoomsOfUser(RC_CREDENTIALS);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException ex) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  void getRoomsOfUser_Should_ReturnListOfRoomsUpdateDTO_WhenAPICallIsSuccessful() {

    when(restTemplate.exchange(
            ArgumentMatchers.anyString(), any(), any(), ArgumentMatchers.<Class<RoomsGetDTO>>any()))
        .thenReturn(ROOMS_GET_RESPONSE_ENTITY);

    assertThat(
        rocketChatService.getRoomsOfUser(RC_CREDENTIALS),
        everyItem(instanceOf(RoomsUpdateDTO.class)));
  }

  /** Method: removeAllStandardUsersFromGroup */
  @Test
  void
      removeAllStandardUsersFromGroup_Should_ThrowRocketChatGetGroupMembersException_WhenGroupListIsEmpty()
          throws Exception {

    RocketChatService spy = Mockito.spy(rocketChatService);

    Mockito.doReturn(new ArrayList<GroupMemberDTO>()).when(spy).getChatUsers(Mockito.anyString());

    try {
      spy.removeAllStandardUsersFromGroup(GROUP_ID);
      fail("Expected exception: RocketChatGetGroupMembersException");
    } catch (RocketChatGetGroupMembersException rocketChatGetGroupMembersException) {
      assertTrue(true, "Excepted RocketChatGetGroupMembersException thrown");
    }
  }

  @Test
  void removeAllStandardUsersFromGroup_Should_RemoveAllStandardUsersAndNotTechnicalOrSystemUser()
      throws Exception {

    RocketChatService spy = Mockito.spy(rocketChatService);

    Mockito.doReturn(GROUP_MEMBER_DTO_LIST).when(spy).getChatUsers(Mockito.anyString());

    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    spy.removeAllStandardUsersFromGroup(GROUP_ID);

    verify(spy, times(0))
        .removeUserFromGroup(RC_CREDENTIALS_SYSTEM_A.getRocketChatUserId(), GROUP_ID);
    verify(spy, times(0))
        .removeUserFromGroup(RC_CREDENTIALS_TECHNICAL_A.getRocketChatUserId(), GROUP_ID);
    verify(spy, times(1)).removeUserFromGroup(GROUP_MEMBER_USER_1.get_id(), GROUP_ID);
    verify(spy, times(1)).removeUserFromGroup(GROUP_MEMBER_USER_2.get_id(), GROUP_ID);
  }

  /** Method: removeAllMessages */
  @Test
  void removeAllMessages_Should_NotThrowException_WhenRemoveMessagesSucceeded() throws Exception {

    when(restTemplate.postForObject(
            ArgumentMatchers.anyString(),
            any(),
            ArgumentMatchers.<Class<StandardResponseDTO>>any()))
        .thenReturn(STANDARD_RESPONSE_DTO_SUCCESS);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    assertDoesNotThrow(() -> rocketChatService.removeAllMessages(GROUP_ID));
  }

  @Test
  void
      removeAllMessages_Should_ThrowRocketChatRemoveSystemMessagesException_WhenRemoveMessagesFails()
          throws Exception {
    assertThrows(
        RocketChatRemoveSystemMessagesException.class,
        () -> {
          when(restTemplate.postForObject(
                  ArgumentMatchers.anyString(),
                  any(),
                  ArgumentMatchers.<Class<StandardResponseDTO>>any()))
              .thenReturn(STANDARD_RESPONSE_DTO_ERROR);

          when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

          rocketChatService.removeAllMessages(GROUP_ID);
        });
  }

  /** Method: getStandardMembersOfGroup */
  @Test
  void
      getStandardMembersOfGroup_Should_ThrowRocketChatGetGroupMembersException_WhenAPICallIsNotSuccessful() {

    Exception exception = new RuntimeException(MESSAGE);

    when(mockedMongoClient.getDatabase(anyString())).thenThrow(exception);

    try {
      rocketChatService.getStandardMembersOfGroup(GROUP_ID);
      fail("Expected exception: RocketChatGetGroupMembersException");
    } catch (RocketChatGetGroupMembersException | RocketChatUserNotInitializedException ex) {
      assertTrue(true, "Excepted RocketChatGetGroupMembersException thrown");
    }
  }

  @Test
  void
      getStandardMembersOfGroup_Should_ThrowRocketChatGetGroupMembersException_WhenAPIResponseIsUnSuccessful()
          throws Exception {
    try {
      rocketChatService.getStandardMembersOfGroup(GROUP_ID);
      fail("Expected exception: RocketChatGetGroupMembersException");
    } catch (RocketChatGetGroupMembersException ex) {
      assertTrue(true, "Excepted RocketChatGetGroupMembersException thrown");
    }
  }

  @Test
  void getStandardMembersOfGroup_Should_ReturnListFilteredOfGroupMemberDTO_WhenAPICallIsSuccessful()
      throws Exception {

    var doc1 = givenSubscription(RC_CREDENTIALS_SYSTEM_A.getRocketChatUserId(), "s");
    givenMongoResponseWith(doc1);
    var doc2 = givenSubscription(RC_CREDENTIALS_TECHNICAL_A.getRocketChatUserId(), "t");
    givenMongoResponseWith(doc2);
    var doc3 = givenSubscription("a", "t");
    givenMongoResponseWith(doc3);
    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);

    List<GroupMemberDTO> result = rocketChatService.getStandardMembersOfGroup(GROUP_ID);

    assertEquals(1, result.size());
    assertEquals("a", result.get(0).get_id());
  }

  /** Method: getUserInfo */
  @Test
  void getUserInfo_Should_ThrowInternalServerExceptionException_WhenAPICallFails()
      throws RocketChatUserNotInitializedException {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          Exception exception =
              new RestClientResponseException(
                  MESSAGE, HttpStatus.BAD_REQUEST.value(), ERROR, null, null, null);

          when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
          when(restTemplate.exchange(
                  ArgumentMatchers.anyString(),
                  any(),
                  any(),
                  ArgumentMatchers.<Class<UserInfoResponseDTO>>any(),
                  anyString()))
              .thenThrow(exception);

          rocketChatService.getUserInfo(RC_USER_ID);
        });
  }

  @Test
  void getUserInfo_Should_ThrowInternalServerErrorException_WhenAPICallIsNotSuccessful()
      throws RocketChatUserNotInitializedException {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
          when(restTemplate.exchange(
                  anyString(),
                  eq(HttpMethod.GET),
                  any(),
                  eq(UserInfoResponseDTO.class),
                  anyString()))
              .thenReturn(new ResponseEntity<>(USER_INFO_RESPONSE_DTO_FAILED, HttpStatus.OK));

          rocketChatService.getUserInfo(RC_USER_ID);
        });
  }

  @Test
  void getUserInfo_Should_ReturnUserInfoResponseDTOWithSameUserId_WhenAPICallIsSuccessful()
      throws Exception {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            any(),
            any(),
            ArgumentMatchers.<Class<UserInfoResponseDTO>>any(),
            anyString()))
        .thenReturn(new ResponseEntity<>(USER_INFO_RESPONSE_DTO, HttpStatus.OK));

    UserInfoResponseDTO result = rocketChatService.getUserInfo(RC_USER_ID);

    assertEquals(RC_USER_ID, result.getUser().getId());
  }

  @Test
  void updateUser_Should_performRocketChatUpdate() throws RocketChatUserNotInitializedException {
    UserUpdateRequestDTO userUpdateRequestDTO =
        new EasyRandom().nextObject(UserUpdateRequestDTO.class);
    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            eq(RC_URL_CHAT_USER_UPDATE), eq(HttpMethod.POST), any(), eq(UserInfoResponseDTO.class)))
        .thenReturn(new ResponseEntity<>(USER_INFO_RESPONSE_DTO, HttpStatus.OK));

    this.rocketChatService.updateUser(userUpdateRequestDTO);

    verify(this.restTemplate, times(1))
        .exchange(
            eq(RC_URL_CHAT_USER_UPDATE), eq(HttpMethod.POST), any(), eq(UserInfoResponseDTO.class));
  }

  @Test
  void updateUser_Should_throwInternalServerErrorException_When_rocketChatUpdateFails()
      throws RocketChatUserNotInitializedException {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          UserUpdateRequestDTO userUpdateRequestDTO =
              new EasyRandom().nextObject(UserUpdateRequestDTO.class);
          when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
          when(restTemplate.exchange(
                  eq(RC_URL_CHAT_USER_UPDATE),
                  eq(HttpMethod.POST),
                  any(),
                  eq(UserInfoResponseDTO.class)))
              .thenReturn(new ResponseEntity<>(new UserInfoResponseDTO(), HttpStatus.OK));

          this.rocketChatService.updateUser(userUpdateRequestDTO);
        });
  }

  @Test
  void updateUser_Should_throwInternalServerErrorException_When_rocketChatIsNotReachable()
      throws RocketChatUserNotInitializedException {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          UserUpdateRequestDTO userUpdateRequestDTO =
              new EasyRandom().nextObject(UserUpdateRequestDTO.class);
          when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
          when(restTemplate.exchange(
                  eq(RC_URL_CHAT_USER_UPDATE),
                  eq(HttpMethod.POST),
                  any(),
                  eq(UserInfoResponseDTO.class)))
              .thenThrow(mock(RestClientResponseException.class));

          this.rocketChatService.updateUser(userUpdateRequestDTO);
        });
  }

  @Test
  void deleteUser_Should_performRocketDeleteUser() throws Exception {
    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            eq(RC_URL_CHAT_USER_DELETE), eq(HttpMethod.POST), any(), eq(UserInfoResponseDTO.class)))
        .thenReturn(new ResponseEntity<>(USER_INFO_RESPONSE_DTO, HttpStatus.OK));

    this.rocketChatService.deleteUser("");

    verify(this.restTemplate, times(1))
        .exchange(
            eq(RC_URL_CHAT_USER_DELETE), eq(HttpMethod.POST), any(), eq(UserInfoResponseDTO.class));
  }

  @Test
  void deleteUser_Should_performRocketDeleteUserOnAlreadyDeletedResponse() throws Exception {
    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    var response =
        new UserInfoResponseDTO(
            null,
            false,
            "The required \"userId\" or \"username\" param provided does not match any users [error-invalid-user]",
            "error-invalid-user");
    when(restTemplate.exchange(
            eq(RC_URL_CHAT_USER_DELETE), eq(HttpMethod.POST), any(), eq(UserInfoResponseDTO.class)))
        .thenReturn(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));

    rocketChatService.deleteUser("");

    verify(restTemplate)
        .exchange(
            eq(RC_URL_CHAT_USER_DELETE), eq(HttpMethod.POST), any(), eq(UserInfoResponseDTO.class));
  }

  @Test
  void deleteUser_Should_throwRocketChatDeleteUserException_When_responseIsNotSuccess()
      throws Exception {
    assertThrows(
        RocketChatDeleteUserException.class,
        () -> {
          when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
          when(restTemplate.exchange(
                  eq(RC_URL_CHAT_USER_DELETE),
                  eq(HttpMethod.POST),
                  any(),
                  eq(UserInfoResponseDTO.class)))
              .thenReturn(new ResponseEntity<>(new UserInfoResponseDTO(), HttpStatus.OK));

          this.rocketChatService.deleteUser("");
        });
  }

  @Test
  void deleteGroupAsTechnicalUser_Should_performRocketDeleteUser() throws Exception {
    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.postForObject(eq(RC_URL_GROUPS_DELETE), any(), any()))
        .thenReturn(new ResponseEntity<>(new GroupDeleteResponseDTO(true), HttpStatus.OK));

    this.rocketChatService.deleteGroupAsTechnicalUser("");

    verify(this.restTemplate, times(1)).postForObject(eq(RC_URL_GROUPS_DELETE), any(), any());
  }

  @Test
  void
      deleteGroupAsTechnicalUser_Should_throwRocketChatDeleteUserException_When_responseIsNotSuccess()
          throws Exception {
    assertThrows(
        RocketChatDeleteGroupException.class,
        () -> {
          when(rcCredentialsHelper.getTechnicalUser()).thenThrow(new RuntimeException());

          this.rocketChatService.deleteGroupAsTechnicalUser("");
        });
  }

  @Test
  void deleteGroupAsSystemUser_Should_performRocketDeleteUser() throws Exception {
    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(restTemplate.postForObject(eq(RC_URL_GROUPS_DELETE), any(), any()))
        .thenReturn(new ResponseEntity<>(new GroupDeleteResponseDTO(true), HttpStatus.OK));

    this.rocketChatService.deleteGroupAsSystemUser("");

    verify(this.restTemplate, times(1)).postForObject(eq(RC_URL_GROUPS_DELETE), any(), any());
  }

  @Test
  void deleteGroupAsSystemUser_Should_throwInternalServerErrorException_When_responseIsNotSuccess()
      throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(rcCredentialsHelper.getSystemUser())
              .thenThrow(new RocketChatUserNotInitializedException(""));

          this.rocketChatService.deleteGroupAsSystemUser("");
        });
  }

  @Test
  @SuppressWarnings("unchecked")
  void setRoomReadOnly_Should_performRocketChatSetRoomReadOnly() throws Exception {
    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(restTemplate.exchange(
            eq(RC_URL_GROUPS_SET_READ_ONLY),
            eq(HttpMethod.POST),
            any(),
            eq(GroupResponseDTO.class)))
        .thenReturn(new ResponseEntity<>(new GroupResponseDTO(), HttpStatus.OK));

    this.rocketChatService.setRoomReadOnly(RC_GROUP_ID);

    ArgumentCaptor<HttpEntity<SetRoomReadOnlyBodyDTO>> captor =
        ArgumentCaptor.forClass(HttpEntity.class);
    verify(this.restTemplate, times(1))
        .exchange(
            eq(RC_URL_GROUPS_SET_READ_ONLY),
            eq(HttpMethod.POST),
            captor.capture(),
            eq(GroupResponseDTO.class));
    var body = captor.getValue().getBody();
    assertNotNull(body);
    assertThat(body.isReadOnly(), is(true));
    assertThat(body.getRoomId(), is(RC_GROUP_ID));
  }

  @Test
  void setRoomReadOnly_Should_logError_When_responseIsNotSuccess() throws Exception {
    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    GroupResponseDTO groupResponseDTO = new GroupResponseDTO();
    groupResponseDTO.setSuccess(false);
    when(restTemplate.exchange(
            eq(RC_URL_GROUPS_SET_READ_ONLY),
            eq(HttpMethod.POST),
            any(),
            eq(GroupResponseDTO.class)))
        .thenReturn(new ResponseEntity<>(groupResponseDTO, HttpStatus.OK));

    this.rocketChatService.setRoomReadOnly("");

    verify(logger).error(anyString(), anyString(), nullable(String.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void setRoomWriteable_Should_performRocketChatSetRoomReadOnly() throws Exception {
    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(restTemplate.exchange(
            eq(RC_URL_GROUPS_SET_READ_ONLY),
            eq(HttpMethod.POST),
            any(),
            eq(GroupResponseDTO.class)))
        .thenReturn(new ResponseEntity<>(new GroupResponseDTO(), HttpStatus.OK));

    this.rocketChatService.setRoomWriteable(RC_GROUP_ID);

    ArgumentCaptor<HttpEntity<SetRoomReadOnlyBodyDTO>> captor =
        ArgumentCaptor.forClass(HttpEntity.class);
    verify(this.restTemplate, times(1))
        .exchange(
            eq(RC_URL_GROUPS_SET_READ_ONLY),
            eq(HttpMethod.POST),
            captor.capture(),
            eq(GroupResponseDTO.class));
    var body = captor.getValue().getBody();
    assertNotNull(body);
    assertThat(body.isReadOnly(), is(false));
    assertThat(body.getRoomId(), is(RC_GROUP_ID));
  }

  @Test
  void setRoomWriteable_Should_logError_When_responseIsNotSuccess() throws Exception {
    when(rcCredentialsHelper.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    GroupResponseDTO groupResponseDTO = new GroupResponseDTO();
    groupResponseDTO.setSuccess(false);
    when(restTemplate.exchange(
            eq(RC_URL_GROUPS_SET_READ_ONLY),
            eq(HttpMethod.POST),
            any(),
            eq(GroupResponseDTO.class)))
        .thenReturn(new ResponseEntity<>(groupResponseDTO, HttpStatus.OK));

    this.rocketChatService.setRoomWriteable("");

    verify(logger).error(anyString(), anyString(), nullable(String.class));
  }

  @Test
  void fetchAllInactivePrivateGroupsSinceGivenDate_ShouldThrowException_WhenRocketChatCallFails()
      throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            anyString()))
        .thenThrow(httpServerErrorException);

    try {
      this.rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(LocalDateTime.now());
      fail("Expected exception: RocketChatGetGroupsListAllException");
    } catch (RocketChatGetGroupsListAllException ex) {
      assertTrue(true, "Excepted RocketChatGetGroupsListAllException thrown");
      assertEquals("Could not get all rocket chat groups", ex.getMessage());
    }
  }

  @Test
  void
      fetchAllInactivePrivateGroupsSinceGivenDate_Should_ThrowException_WhenHttpStatusFromRocketChatCallIsNotOk()
          throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            anyString()))
        .thenReturn(
            new ResponseEntity<>(GROUPS_LIST_ALL_RESPONSE_DTO_EMPTY, HttpStatus.BAD_REQUEST));

    try {
      this.rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(LocalDateTime.now());
      fail("Expected exception: RocketChatGetGroupsListAllException");
    } catch (RocketChatGetGroupsListAllException ex) {
      assertTrue(true, "Excepted RocketChatGetGroupsListAllException thrown");
      assertEquals("Could not get all rocket chat groups", ex.getMessage());
    }
  }

  @Test
  void fetchAllInactivePrivateGroupsSinceGivenDate_Should_ReturnCorrectGroupDtoList()
      throws RocketChatUserNotInitializedException, RocketChatGetGroupsListAllException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            anyString()))
        .thenReturn(new ResponseEntity<>(GROUPS_LIST_ALL_RESPONSE_DTO, HttpStatus.OK));

    List<GroupDTO> result =
        this.rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(LocalDateTime.now());

    assertThat(result.size(), is(2));
    assertThat(result.contains(GROUP_DTO), is(true));
    assertThat(result.contains(GROUP_DTO_2), is(true));
  }

  @Test
  void fetchAllInactivePrivateGroupsSinceGivenDate_Should_UseCorrectMongoQuery()
      throws RocketChatUserNotInitializedException, RocketChatGetGroupsListAllException {

    LocalDateTime dateToCheck = LocalDateTime.of(2021, 1, 1, 0, 0, 0);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            anyString()))
        .thenReturn(new ResponseEntity<>(GROUPS_LIST_ALL_RESPONSE_DTO, HttpStatus.OK));

    this.rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(dateToCheck);

    String correctMongoQuery =
        "{\"lm\": {\"$lt\": {\"$date\": \"2021-01-01T00:00:00.000Z\"}},"
            + " \"$and\": [{\"t\": \"p\"}]}";
    verify(restTemplate, times(2))
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            eq(correctMongoQuery));
  }

  @Test
  void
      fetchAllInactivePrivateGroupsSinceGivenDate_Should_CallRocketChatApiMultipleTimes_When_ResultIsPaginated()
          throws RocketChatUserNotInitializedException, RocketChatGetGroupsListAllException {

    LocalDateTime dateToCheck = LocalDateTime.of(2021, 1, 1, 0, 0, 0);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            anyString()))
        .thenReturn(new ResponseEntity<>(GROUPS_LIST_ALL_RESPONSE_DTO_PAGINATED, HttpStatus.OK));

    this.rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(dateToCheck);

    String correctMongoQuery =
        "{\"lm\": {\"$lt\": {\"$date\": \"2021-01-01T00:00:00.000Z\"}},"
            + " \"$and\": [{\"t\": \"p\"}]}";
    verify(restTemplate, times(11))
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            eq(correctMongoQuery));
  }

  @Test
  void
      fetchAllInactivePrivateGroupsSinceGivenDate_Should_CallRocketChatApiOnlyOnce_When_ResponseContainsTotalOfZeroElements()
          throws RocketChatUserNotInitializedException, RocketChatGetGroupsListAllException {

    LocalDateTime dateToCheck = LocalDateTime.of(2021, 1, 1, 0, 0, 0);

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            anyString()))
        .thenReturn(
            new ResponseEntity<>(
                GROUPS_LIST_ALL_RESPONSE_DTO_PAGINATED_WITH_TOTAL_ZERO_ELEMENTS, HttpStatus.OK));

    this.rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(dateToCheck);

    String correctMongoQuery =
        "{\"lm\": {\"$lt\": {\"$date\": \"2021-01-01T00:00:00.000Z\"}},"
            + " \"$and\": [{\"t\": \"p\"}]}";
    verify(restTemplate, times(1))
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(GroupsListAllResponseDTO.class),
            eq(correctMongoQuery));
  }

  @Test
  void getRocketChatUserIdByUsername_Should_ThrowException_WhenRocketChatCallFails()
      throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(UsersListReponseDTO.class),
            anyString(),
            anyString()))
        .thenThrow(httpServerErrorException);

    try {
      this.rocketChatService.getRocketChatUserIdByUsername(USERNAME);
      fail("Expected exception: RocketChatGetUserIdException");
    } catch (RocketChatGetUserIdException ex) {
      assertTrue(true, "Excepted RocketChatGetUserIdException thrown");
      assertEquals("Could not get users list from Rocket.Chat", ex.getMessage());
    }
  }

  @Test
  void getRocketChatUserIdByUsername_Should_ThrowException_WhenFoundNoUserByUsername()
      throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(UsersListReponseDTO.class),
            anyString(),
            anyString()))
        .thenReturn(new ResponseEntity<>(USERS_LIST_RESPONSE_DTO_EMPTY, HttpStatus.OK));

    try {
      this.rocketChatService.getRocketChatUserIdByUsername(USERNAME);
      fail("Expected exception: RocketChatGetUserIdException");
    } catch (RocketChatGetUserIdException ex) {
      assertTrue(true, "Excepted RocketChatGetUserIdException thrown");
      assertEquals("Found 0 users by username", ex.getMessage());
    }
  }

  @Test
  void getRocketChatUserIdByUsername_Should_ThrowException_WhenFound2UsersByUsername()
      throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(UsersListReponseDTO.class),
            anyString(),
            anyString()))
        .thenReturn(new ResponseEntity<>(USERS_LIST_RESPONSE_DTO_WITH_2_USERS, HttpStatus.OK));

    try {
      this.rocketChatService.getRocketChatUserIdByUsername(USERNAME);
      fail("Expected exception: RocketChatGetUserIdException");
    } catch (RocketChatGetUserIdException ex) {
      assertTrue(true, "Excepted RocketChatGetUserIdException thrown");
      assertEquals("Found 2 users by username", ex.getMessage());
    }
  }

  @Test
  void getRocketChatUserIdByUsername_Should_ThrowException_WhenHttpStatusFromRocketChatCallIsNotOk()
      throws RocketChatUserNotInitializedException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(UsersListReponseDTO.class),
            anyString(),
            anyString()))
        .thenReturn(new ResponseEntity<>(USERS_LIST_RESPONSE_DTO_EMPTY, HttpStatus.BAD_REQUEST));

    try {
      this.rocketChatService.getRocketChatUserIdByUsername(USERNAME);
      fail("Expected exception: RocketChatGetUserIdException");
    } catch (RocketChatGetUserIdException ex) {
      assertTrue(true, "Excepted RocketChatGetUserIdException thrown");
      assertEquals("Could not get users list from Rocket.Chat", ex.getMessage());
    }
  }

  @Test
  void getRocketChatUserIdByUsername_Should_ReturnRocketChatUserId()
      throws RocketChatUserNotInitializedException, RocketChatGetUserIdException {

    when(rcCredentialsHelper.getTechnicalUser()).thenReturn(RC_CREDENTIALS_TECHNICAL_A);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            eq(UsersListReponseDTO.class),
            anyString(),
            anyString()))
        .thenReturn(new ResponseEntity<>(USERS_LIST_RESPONSE_DTO, HttpStatus.OK));

    String result = this.rocketChatService.getRocketChatUserIdByUsername(USERNAME);

    assertThat(result, is(USERS_LIST_RESPONSE_DTO.getUsers()[0].getId()));
  }

  private void givenMongoResponseWith(Document doc, Document... docs) {
    if (nonNull(doc)) {
      when(mongoCursor.next()).thenReturn(doc, docs);
    }
    var booleanList = new LinkedList<Boolean>();
    var numExtraDocs = docs.length;
    while (numExtraDocs-- > 0) {
      booleanList.add(true);
    }
    booleanList.add(false);
    if (nonNull(doc)) {
      when(mongoCursor.hasNext()).thenReturn(true, booleanList.toArray(new Boolean[0]));
    } else {
      when(mongoCursor.hasNext()).thenReturn(false);
    }
    when(findIterable.iterator()).thenReturn(mongoCursor);
    when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(mockedMongoClient.getDatabase("rocketchat")).thenReturn(mongoDatabase);
    when(mongoDatabase.getCollection("rocketchat_subscription")).thenReturn(mongoCollection);
  }

  private Document givenSubscription(String chatUserId, String username)
      throws JsonProcessingException {
    var doc = new LinkedHashMap<String, Object>();
    doc.put("_id", RandomStringUtils.randomAlphanumeric(17));
    doc.put("rid", RandomStringUtils.randomAlphanumeric(17));
    doc.put("name", RandomStringUtils.randomAlphanumeric(17));

    var user = new LinkedHashMap<>();
    user.put("_id", chatUserId);
    user.put("username", username);

    doc.put("u", user);

    var json = objectMapper.writeValueAsString(doc);

    return Document.parse(json);
  }
}

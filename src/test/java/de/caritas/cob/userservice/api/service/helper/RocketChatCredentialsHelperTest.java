package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_B;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_C;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_A;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_B;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS_TECHNICAL_C;
import static de.caritas.cob.userservice.testHelper.TestConstants.SYSTEM_USER_A_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SYSTEM_USER_A_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.SYSTEM_USER_A_USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.SYSTEM_USER_B_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SYSTEM_USER_B_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.TECHNICAL_USER_A_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.TECHNICAL_USER_A_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.TECHNICAL_USER_A_USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.TECHNICAL_USER_B_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.TECHNICAL_USER_B_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.TECHNICAL_USER_B_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.rocketChat.login.DataDTO;
import de.caritas.cob.userservice.api.model.rocketChat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.logout.LogoutResponseDTO;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatCredentialsHelperTest {

  /**
   * FIELD Names
   */

  private final static String FIELD_NAME_TECHNICAL_USER_A = "techUser_A";
  private final static String FIELD_NAME_TECHNICAL_USER_B = "techUser_B";
  private final static String FIELD_NAME_TECHNICAL_USER_C = "techUser_B";

  private final static String FIELD_NAME_SYSTEM_USER_A = "systemUser_A";
  private final static String FIELD_NAME_SYSTEM_USER_B = "systemUser_B";

  private final static String FIELD_NAME_TECHNICAL_USERNAME = "technicalUsername";
  private final static String FIELD_NAME_TECHNICAL_PASSWORD = "technicalPassword";

  private final static String FIELD_NAME_SYSTEM_USERNAME = "systemUsername";
  private final static String FIELD_NAME_SYSTEM_PASSWORD = "systemPassword";

  private final static String FIELD_NAME_ROCKET_CHAT_HEADER_AUTH_TOKEN =
      "rocketChatHeaderAuthToken";
  private final static String FIELD_VALUE_ROCKET_CHAT_HEADER_AUTH_TOKEN = "X-Auth-Token";
  private final static String FIELD_NAME_ROCKET_CHAT_HEADER_USER_ID = "rocketChatHeaderUserId";
  private final static String FIELD_VALUE_ROCKET_CHAT_HEADER_USER_ID = "X-User-Id";

  private final String FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGIN = "rocketChatApiUserLogin";
  private final String FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGOUT = "rocketChatApiUserLogout";

  /**
   * DATA
   */

  private final String RC_URL_CHAT_USER_LOGIN = "http://localhost/api/v1/login";
  private final String RC_URL_CHAT_USER_LOGOUT = "http://localhost/api/v1/logout";

  MultiValueMap<String, String> MULTI_VALUE_MAP_WITH_TECHNICAL_USER_CREDENTIALS =
      new LinkedMultiValueMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
          add("username", TECHNICAL_USER_USERNAME);
          add("password", TECHNICAL_USER_PW);
        }
      };

  MultiValueMap<String, String> MULTI_VALUE_MAP_WITH_SYSTEM_USER_CREDENTIALS =
      new LinkedMultiValueMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
          add("username", SYSTEM_USER_USERNAME);
          add("password", SYSTEM_USER_PW);
        }
      };

  LoginResponseDTO LOGIN_RESPONSE_DTO_SYSTEM_USER_A =
      new LoginResponseDTO("status", new DataDTO(SYSTEM_USER_A_ID, SYSTEM_USER_A_TOKEN, null));

  LoginResponseDTO LOGIN_RESPONSE_DTO_SYSTEM_USER_B =
      new LoginResponseDTO("status", new DataDTO(SYSTEM_USER_B_ID, SYSTEM_USER_B_TOKEN, null));


  LoginResponseDTO LOGIN_RESPONSE_DTO_TECHNICAL_USER_A = new LoginResponseDTO("status",
      new DataDTO(TECHNICAL_USER_A_ID, TECHNICAL_USER_A_TOKEN, null));

  LoginResponseDTO LOGIN_RESPONSE_DTO_TECHNICAL_USER_B = new LoginResponseDTO("status",
      new DataDTO(TECHNICAL_USER_B_ID, TECHNICAL_USER_B_TOKEN, null));

  LogoutResponseDTO LOGOUT_RESPONSE_DTO_SYSTEM_USER_A = new LogoutResponseDTO("status",
      SYSTEM_USER_USERNAME,
      new de.caritas.cob.userservice.api.model.rocketChat.logout.DataDTO(SYSTEM_USER_A_USERNAME));

  LogoutResponseDTO LOGOUT_RESPONSE_DTO_TECHNICAL_USER_A = new LogoutResponseDTO("status",
      TECHNICAL_USER_USERNAME, new de.caritas.cob.userservice.api.model.rocketChat.logout.DataDTO(
          TECHNICAL_USER_A_USERNAME));

  private final static String TECHNICAL_USER_USERNAME = "techUserName";
  private final static String TECHNICAL_USER_PW = "techUserPW";

  private final static String SYSTEM_USER_USERNAME = "sysUserName";
  private final static String SYSTEM_USER_PW = "sysUserPW";

  @InjectMocks
  private RocketChatCredentialsHelper rcCredentialHelper;

  @Mock
  private RestTemplate restTemplate;

  @Before
  public void setup() throws NoSuchFieldException {
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USERNAME),
        TECHNICAL_USER_USERNAME);
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_PASSWORD),
        TECHNICAL_USER_PW);
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USERNAME),
        SYSTEM_USER_USERNAME);
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_PASSWORD), SYSTEM_USER_PW);
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGIN),
        RC_URL_CHAT_USER_LOGIN);
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGOUT),
        RC_URL_CHAT_USER_LOGOUT);
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_HEADER_AUTH_TOKEN),
        FIELD_VALUE_ROCKET_CHAT_HEADER_AUTH_TOKEN);
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_HEADER_USER_ID),
        FIELD_VALUE_ROCKET_CHAT_HEADER_USER_ID);
  }

  /**
   *
   * Method: updateCredentials
   *
   **/

  @Test
  public void updateCredentials_Should_LoginAUsers_WhenNoUsersAreLoggedIn()
      throws Exception {
    // Prepare Header for Requests
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // Prepare intercept login technical user
    HttpEntity<MultiValueMap<String, String>> requestTechnical =
        new HttpEntity<MultiValueMap<String, String>>(
            MULTI_VALUE_MAP_WITH_TECHNICAL_USER_CREDENTIALS, headers);

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        ArgumentMatchers.eq(requestTechnical), ArgumentMatchers.<Class<LoginResponseDTO>>any()))
            .thenReturn(new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_TECHNICAL_USER_A,
                HttpStatus.OK));

    // Prepare intercept login system user
    HttpEntity<MultiValueMap<String, String>> requestSys =
        new HttpEntity<MultiValueMap<String, String>>(MULTI_VALUE_MAP_WITH_SYSTEM_USER_CREDENTIALS,
            headers);

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        ArgumentMatchers.eq(requestSys), ArgumentMatchers.<Class<LoginResponseDTO>>any()))
            .thenReturn(new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_SYSTEM_USER_A,
                HttpStatus.OK));

    // Execute test
    rcCredentialHelper.updateCredentials();

    // Get and check system user
    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
    assertNotNull(systemUser);
    assertEquals(SYSTEM_USER_A_ID, systemUser.getRocketChatUserId());
    assertEquals(SYSTEM_USER_A_TOKEN, systemUser.getRocketChatToken());

    // Get and check technical user
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    assertNotNull(technicalUser);
    assertEquals(TECHNICAL_USER_A_ID, technicalUser.getRocketChatUserId());
    assertEquals(TECHNICAL_USER_A_TOKEN, technicalUser.getRocketChatToken());
  }

  @Test
  public void updateCredentials_Should_LoginBUsers_WhenAUsersAreLoggedIn()
      throws Exception {
    // Prepare Header for Requests
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // Prepare intercept login technical user
    HttpEntity<MultiValueMap<String, String>> requestTechnical =
        new HttpEntity<MultiValueMap<String, String>>(
            MULTI_VALUE_MAP_WITH_TECHNICAL_USER_CREDENTIALS, headers);

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        ArgumentMatchers.eq(requestTechnical), ArgumentMatchers.<Class<LoginResponseDTO>>any()))
            .thenReturn(new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_TECHNICAL_USER_B,
                HttpStatus.OK));

    // Prepare intercept login system user
    HttpEntity<MultiValueMap<String, String>> requestSys =
        new HttpEntity<MultiValueMap<String, String>>(MULTI_VALUE_MAP_WITH_SYSTEM_USER_CREDENTIALS,
            headers);

    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        ArgumentMatchers.eq(requestSys), ArgumentMatchers.<Class<LoginResponseDTO>>any()))
            .thenReturn(new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_SYSTEM_USER_B,
                HttpStatus.OK));

    RocketChatCredentials systemA =
        RocketChatCredentials.builder().RocketChatToken(SYSTEM_USER_A_TOKEN)
            .RocketChatUserId(SYSTEM_USER_A_ID).RocketChatUsername(SYSTEM_USER_A_USERNAME)
            .TimeStampCreated(LocalDateTime.now().minusMinutes(5)).build();
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_A), systemA);

    RocketChatCredentials technicalA =
        RocketChatCredentials.builder().RocketChatToken(TECHNICAL_USER_A_TOKEN)
            .RocketChatUserId(TECHNICAL_USER_A_ID).RocketChatUsername(TECHNICAL_USER_A_USERNAME)
            .TimeStampCreated(LocalDateTime.now().minusMinutes(5)).build();
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_A), technicalA);

    // Get and check system user - pre test needs to be User A
    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
    assertNotNull(systemUser);
    assertEquals(SYSTEM_USER_A_ID, systemUser.getRocketChatUserId());
    assertEquals(SYSTEM_USER_A_TOKEN, systemUser.getRocketChatToken());

    // Get and check technical user - pre test needs to be User A
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    assertNotNull(technicalUser);
    assertEquals(TECHNICAL_USER_A_ID, technicalUser.getRocketChatUserId());
    assertEquals(TECHNICAL_USER_A_TOKEN, technicalUser.getRocketChatToken());

    // Execute test
    rcCredentialHelper.updateCredentials();

    // Get and check system user - post test needs to be User B since he is newer
    systemUser = rcCredentialHelper.getSystemUser();
    assertNotNull(systemUser);
    assertEquals(SYSTEM_USER_B_ID, systemUser.getRocketChatUserId());
    assertEquals(SYSTEM_USER_B_TOKEN, systemUser.getRocketChatToken());

    // Get and check technical user - post test needs to be User B since he is newer
    technicalUser = rcCredentialHelper.getTechnicalUser();
    assertNotNull(technicalUser);
    assertEquals(TECHNICAL_USER_B_ID, technicalUser.getRocketChatUserId());
    assertEquals(TECHNICAL_USER_B_TOKEN, technicalUser.getRocketChatToken());
  }

  @Test
  public void updateCredentials_Should_LogoutAndReLoginBUsers_WhenAllUsersArePresent()
      throws Exception {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // create and set technical A user
    RocketChatCredentials technicalA =
        RocketChatCredentials.builder().RocketChatToken(TECHNICAL_USER_A_TOKEN)
            .RocketChatUserId(TECHNICAL_USER_A_ID).RocketChatUsername(TECHNICAL_USER_A_USERNAME)
            .TimeStampCreated(LocalDateTime.now().minusMinutes(5)).build();
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_A), technicalA);

    // create and set technical B user
    RocketChatCredentials technicalB =
        RocketChatCredentials.builder().RocketChatToken(TECHNICAL_USER_B_TOKEN)
            .RocketChatUserId(TECHNICAL_USER_B_ID).RocketChatUsername(TECHNICAL_USER_B_USERNAME)
            .TimeStampCreated(LocalDateTime.now().minusMinutes(1)).build();
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_B), technicalB);

    // create and set system A user
    RocketChatCredentials systemA =
        RocketChatCredentials.builder().RocketChatToken(SYSTEM_USER_A_TOKEN)
            .RocketChatUserId(SYSTEM_USER_A_ID).RocketChatUsername(SYSTEM_USER_A_USERNAME)
            .TimeStampCreated(LocalDateTime.now().minusMinutes(5)).build();
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_A), systemA);

    // create and set system B user
    RocketChatCredentials systemB =
        RocketChatCredentials.builder().RocketChatToken(SYSTEM_USER_B_TOKEN)
            .RocketChatUserId(SYSTEM_USER_B_ID).RocketChatUsername(SYSTEM_USER_A_USERNAME)
            .TimeStampCreated(LocalDateTime.now().minusMinutes(1)).build();

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_B), systemB);

    // prepare logout intercept for system user
    HttpHeaders headersLogoutSys = new HttpHeaders();
    headersLogoutSys.setContentType(MediaType.APPLICATION_JSON_UTF8);
    headersLogoutSys.add(FIELD_VALUE_ROCKET_CHAT_HEADER_AUTH_TOKEN, SYSTEM_USER_A_TOKEN);
    headersLogoutSys.add(FIELD_VALUE_ROCKET_CHAT_HEADER_USER_ID, SYSTEM_USER_A_ID);
    HttpEntity<Void> requestSysLogout = new HttpEntity<Void>(headersLogoutSys);
    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGOUT),
        ArgumentMatchers.eq(requestSysLogout), ArgumentMatchers.<Class<LogoutResponseDTO>>any()))
            .thenReturn(new ResponseEntity<LogoutResponseDTO>(LOGOUT_RESPONSE_DTO_SYSTEM_USER_A,
                HttpStatus.OK));

    // prepare logout intercept for technical user
    HttpHeaders headersLogoutTec = new HttpHeaders();
    headersLogoutTec.setContentType(MediaType.APPLICATION_JSON_UTF8);
    headersLogoutTec.add(FIELD_VALUE_ROCKET_CHAT_HEADER_AUTH_TOKEN, TECHNICAL_USER_A_TOKEN);
    headersLogoutTec.add(FIELD_VALUE_ROCKET_CHAT_HEADER_USER_ID, TECHNICAL_USER_A_ID);
    HttpEntity<Void> requestTechnicalLogout = new HttpEntity<Void>(headersLogoutTec);
    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGOUT),
        ArgumentMatchers.eq(requestTechnicalLogout),
        ArgumentMatchers.<Class<LogoutResponseDTO>>any()))
            .thenReturn(new ResponseEntity<LogoutResponseDTO>(LOGOUT_RESPONSE_DTO_TECHNICAL_USER_A,
                HttpStatus.OK));

    // prepare login intercept for system user
    HttpEntity<MultiValueMap<String, String>> requestSysLogin =
        new HttpEntity<MultiValueMap<String, String>>(MULTI_VALUE_MAP_WITH_SYSTEM_USER_CREDENTIALS,
            headers);
    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        ArgumentMatchers.eq(requestSysLogin), ArgumentMatchers.<Class<LoginResponseDTO>>any()))
            .thenReturn(new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_SYSTEM_USER_B,
                HttpStatus.OK));

    // prepare login intercept for technical user
    HttpEntity<MultiValueMap<String, String>> requestTechnicalLogin =
        new HttpEntity<MultiValueMap<String, String>>(
            MULTI_VALUE_MAP_WITH_TECHNICAL_USER_CREDENTIALS, headers);
    when(restTemplate.postForEntity(ArgumentMatchers.eq(RC_URL_CHAT_USER_LOGIN),
        ArgumentMatchers.eq(requestTechnicalLogin),
        ArgumentMatchers.<Class<LoginResponseDTO>>any()))
            .thenReturn(new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_TECHNICAL_USER_B,
                HttpStatus.OK));

    // Execute test
    rcCredentialHelper.updateCredentials();

    // get technical user and ensure it is a new one
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    assertNotEquals(technicalA.getTimeStampCreated(), technicalUser.getTimeStampCreated());
    assertNotEquals(technicalB.getTimeStampCreated(), technicalUser.getTimeStampCreated());

    // get system user and ensure it is a new one
    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
    assertNotEquals(systemA.getTimeStampCreated(), systemUser.getTimeStampCreated());
    assertNotEquals(systemB.getTimeStampCreated(), systemUser.getTimeStampCreated());

    // ensure logout interception was called
    verify(restTemplate, times(1)).postForEntity(RC_URL_CHAT_USER_LOGOUT, requestTechnicalLogout,
        LogoutResponseDTO.class);
    verify(restTemplate, times(1)).postForEntity(RC_URL_CHAT_USER_LOGOUT, requestSysLogout,
        LogoutResponseDTO.class);
    // ensure login interception was called
    verify(restTemplate, times(1)).postForEntity(RC_URL_CHAT_USER_LOGIN, requestSysLogin,
        LoginResponseDTO.class);
    verify(restTemplate, times(1)).postForEntity(RC_URL_CHAT_USER_LOGIN, requestTechnicalLogin,
        LoginResponseDTO.class);

  }

  /**
   *
   * Method: createPrivateGroup
   *
   **/

  @Test
  public void getTechnicalUser_Should_ThrowRocketChatUserNotInitializedException_WhenNoUserIsInitialized() {
    try {
      rcCredentialHelper.getTechnicalUser();
      fail("Expected exception: RocketChatUserNotInitializedException");
    } catch (RocketChatUserNotInitializedException ex) {
      assertTrue("Excepted RocketChatUserNotInitializedException thrown", true);
    }
  }

  @Test
  public void getTechnicalUser_Should_ReturnUserA_WhenOnlyUserAIsInitialized()
      throws Exception {

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_A),
        RC_CREDENTIALS_TECHNICAL_A);

    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    assertEquals(RC_CREDENTIALS_TECHNICAL_A, technicalUser);
  }

  @Test
  public void getTechnicalUser_Should_ReturnUserB_WhenOnlyUserBIsInitialized()
      throws Exception {

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_B),
        RC_CREDENTIALS_TECHNICAL_B);

    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    assertEquals(RC_CREDENTIALS_TECHNICAL_B, technicalUser);
  }

  @Test
  public void getTechnicalUser_Should_ReturnUserA_WhenUserAIsNewer() throws Exception {
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_A),
        RC_CREDENTIALS_TECHNICAL_A);

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_C),
        RC_CREDENTIALS_TECHNICAL_C);

    // Get User from Class (actual test)
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    // Verify it is User A since he is newer
    assertEquals(RC_CREDENTIALS_TECHNICAL_A, technicalUser);
  }

  @Test
  public void getTechnicalUser_Should_ReturnUserB_WhenUserBIsNewer() throws Exception {
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_A),
        RC_CREDENTIALS_TECHNICAL_A);

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_TECHNICAL_USER_B),
        RC_CREDENTIALS_TECHNICAL_B);

    // Get User from Class (actual test)
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    // Verify it is User A since he is newer
    assertEquals(RC_CREDENTIALS_TECHNICAL_B, technicalUser);
  }

  /**
   *
   * Method: getSystemUser
   *
   **/

  @Test
  public void getSystemUser_Should_ThrowRocketChatUserNotInitializedException_WhenNoUserIsInitialized() {
    try {
      rcCredentialHelper.getSystemUser();
      fail("Expected exception: RocketChatUserNotInitializedException");
    } catch (RocketChatUserNotInitializedException ex) {
      assertTrue("Excepted RocketChatUserNotInitializedException thrown", true);
    }
  }

  @Test
  public void getSystemUser_Should_ReturnUserA_WhenOnlyUserAIsInitialized()
      throws Exception {

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_A),
        RC_CREDENTIALS_SYSTEM_A);

    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();

    assertEquals(RC_CREDENTIALS_SYSTEM_A, systemUser);
  }

  @Test
  public void getSystemUser_Should_ReturnUserB_WhenOnlyUserBIsInitialized()
      throws Exception {

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_B),
        RC_CREDENTIALS_SYSTEM_B);

    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();

    assertEquals(RC_CREDENTIALS_SYSTEM_B, systemUser);
  }

  @Test
  public void getSystemUser_Should_ReturnUserA_WhenUserAIsNewer() throws Exception {
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_A),
        RC_CREDENTIALS_SYSTEM_A);

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_B),
        RC_CREDENTIALS_SYSTEM_C);

    // Get User from Class (actual test)
    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();

    // Verify it is User A since he is newer
    assertEquals(RC_CREDENTIALS_SYSTEM_A, systemUser);
  }

  @Test
  public void getSystemUser_Should_ReturnUserB_WhenUserBIsNewer() throws Exception {
    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_A),
        RC_CREDENTIALS_SYSTEM_A);

    FieldSetter.setField(rcCredentialHelper,
        rcCredentialHelper.getClass().getDeclaredField(FIELD_NAME_SYSTEM_USER_B),
        RC_CREDENTIALS_SYSTEM_B);

    // Get User from Class (actual test)
    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();

    // Verify it is User A since he is newer
    assertEquals(RC_CREDENTIALS_SYSTEM_B, systemUser);
  }

}

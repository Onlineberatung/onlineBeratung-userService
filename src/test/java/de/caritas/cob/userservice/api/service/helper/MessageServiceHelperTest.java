package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_WELCOME_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE_WITH_REPLACED_USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.messageService.MessageDTO;
import de.caritas.cob.userservice.api.service.LogService;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceHelperTest {

  private final String FIELD_NAME_POST_MESSAGE_API_URL = "messageServiceApiPostMessageUrl";
  private final String POST_MESSAGE_API_URL = "http://caritas.local/service/messages/new";

  @InjectMocks
  private MessageServiceHelper messageServiceHelper;
  @Mock
  private RestTemplate restTemplate;
  @Mock
  private ServiceHelper serviceHelper;
  @Mock
  private RocketChatCredentialsHelper rcCredentialHelper;
  @Mock
  private UserHelper userHelper;
  @Mock
  private LogService logService;

  @Captor
  private ArgumentCaptor<HttpEntity<MessageDTO>> messageDtoCaptor;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(messageServiceHelper,
        messageServiceHelper.getClass().getDeclaredField(FIELD_NAME_POST_MESSAGE_API_URL),
        POST_MESSAGE_API_URL);
  }

  /**
   * postMessage()
   * 
   */

  @Test
  public void postMessage_Should_ReturnAgencyServiceHelperException_When_ApiCallFails()
      throws Exception {

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any()))
            .thenThrow(new RestClientException(MESSAGE));

    try {
      messageServiceHelper.postMessage(MESSAGE, RC_CREDENTIALS, RC_GROUP_ID, null);
      fail("Expected exception: RocketChatPostMessageException");
    } catch (RocketChatPostMessageException rocketChatPostMessageException) {
      assertTrue("Excepted RocketChatPostMessageException thrown", true);
    }
  }

  @Test
  public void postMessage_Should_NotThrowAnException_When_ApiCallIsSuccessful() throws Exception {

    ResponseEntity<Void> response = new ResponseEntity<Void>(HttpStatus.CREATED);

    when(serviceHelper.getRocketChatAndCsrfHttpHeaders(RC_CREDENTIALS, RC_GROUP_ID))
        .thenReturn(new HttpHeaders());
    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any()))
            .thenReturn(response);

    messageServiceHelper.postMessage(MESSAGE, RC_CREDENTIALS, RC_GROUP_ID, null);
  }

  @Test
  public void postMessage_Should_ThrowRocketChatPostMessageException_When_ApiResponseIsNotCreated()
      throws Exception {

    ResponseEntity<Void> response = new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any()))
            .thenReturn(response);

    try {
      messageServiceHelper.postMessage(MESSAGE, RC_CREDENTIALS, RC_GROUP_ID, null);
      fail("Expected exception: RocketChatPostMessageException");
    } catch (RocketChatPostMessageException rocketChatPostMessageException) {
      assertTrue("Excepted RocketChatPostMessageException thrown", true);
    }
  }

  /**
   * postWelcomeMessage()
   * 
   * @throws RocketChatPostMessageException
   * @throws RocketChatPostWelcomeMessageException
   * 
   */

  @Test
  public void postWelcomeMessage_Should_DoNothing_When_WelcomeMessageIsDeactivated()
      throws Exception {

    messageServiceHelper.postWelcomeMessage(RC_GROUP_ID, null,
        CONSULTING_TYPE_SETTINGS_WITHOUT_WELCOME_MESSAGE, null);

    verify(restTemplate, never()).exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any());
  }

  @Test
  public void postWelcomeMessage_Should_PostWelcomeMessageWithReplacedUserName() throws Exception {

    ResponseEntity<Void> response = new ResponseEntity<Void>(HttpStatus.CREATED);

    when(userHelper.decodeUsername(USER.getUsername())).thenReturn(USER.getUsername());
    when(rcCredentialHelper.getSystemUser()).thenReturn(RC_CREDENTIALS);
    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any()))
            .thenReturn(response);

    messageServiceHelper.postWelcomeMessage(RC_GROUP_ID, USER,
        CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE, null);

    verify(restTemplate).exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        messageDtoCaptor.capture(), ArgumentMatchers.<Class<Void>>any());

    assertEquals(MESSAGE_WITH_REPLACED_USERNAME,
        messageDtoCaptor.getValue().getBody().getMessage());
  }

  @Test
  public void postWelcomeMessage_Should_ThrowRocketChatPostWelcomeMessageException_When_PostMessageFails()
      throws Exception {

    RestClientException exception = new RestClientException(MESSAGE, null);

    when(rcCredentialHelper.getSystemUser()).thenReturn(RC_CREDENTIALS);
    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any()))
            .thenThrow(exception);

    try {
      messageServiceHelper.postWelcomeMessage(RC_GROUP_ID, USER,
          CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE, null);
      fail("Expected exception: RocketChatPostWelcomeMessageException");
    } catch (RocketChatPostWelcomeMessageException rocketChatPostWelcomeMessageException) {
      assertTrue("Excepted RocketChatPostWelcomeMessageException thrown", true);
    }
  }
}

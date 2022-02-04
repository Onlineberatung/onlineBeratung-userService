package de.caritas.cob.userservice.api.service.message;


import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_U25;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_FURTHER_STEPS__AND_SAVE_SESSION_DATA_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_WELCOME_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_FURTHER_STEPS_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_UPDATE_SESSION_DATA_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetMessagesStreamException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostFurtherStepsMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.userservice.messageservice.generated.web.model.AliasOnlyMessageDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageType;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceProviderTest {

  @InjectMocks
  private MessageServiceProvider messageServiceProvider;

  @Mock
  private MessageControllerApi messageControllerApi;

  @Mock
  private RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @Mock
  private UserHelper userHelper;

  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;

  private final RestClientException restClientException = new RestClientException(ERROR);

  @Test
  public void postEnquiryMessage_Should_ThrowRocketChatPostMessageExceptionWithExceptionInformation_When_PostRcMessageFails() {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    doThrow(restClientException).when(this.messageControllerApi)
        .createMessage(anyString(), anyString(), anyString(), any());

    try {
      this.messageServiceProvider
          .postEnquiryMessage(MESSAGE, RC_CREDENTIALS, RC_GROUP_ID, exceptionInformation);
      fail("Expected exception: RocketChatPostMessageException");
    } catch (RocketChatPostMessageException exception) {
      assertTrue("Excepted RocketChatPostMessageException thrown", true);
      assertNotNull(exception.getExceptionInformation());
    }
  }

  @Test
  public void postEnquiryMessage_Should_CallCreateMessageFromMessageServiceWithCorrectParams_When_EverythingSucceeds()
      throws RocketChatPostMessageException {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    ArgumentCaptor<MessageDTO> captor = ArgumentCaptor.forClass(MessageDTO.class);

    this.messageServiceProvider
        .postEnquiryMessage(MESSAGE, RC_CREDENTIALS, RC_GROUP_ID, exceptionInformation);

    verify(messageControllerApi, times(1))
        .createMessage(eq(RC_CREDENTIALS.getRocketChatToken()),
            eq(RC_CREDENTIALS.getRocketChatUserId()), eq(RC_GROUP_ID), captor.capture());
    assertThat(captor.getValue().getMessage(), is(MESSAGE));
  }

  @Test
  public void postWelcomeMessageIfConfigured_ShouldNot_CallMessageService_When_NoWelcomeMessageConfigured()
      throws RocketChatPostWelcomeMessageException {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);

    this.messageServiceProvider
        .postWelcomeMessageIfConfigured(RC_GROUP_ID, USER,
            CONSULTING_TYPE_SETTINGS_WITHOUT_WELCOME_MESSAGE, exceptionInformation);

    verifyNoInteractions(messageControllerApi);
    verifyNoInteractions(userHelper);
  }

  @Test
  public void postWelcomeMessageIfConfigured_Should_ThrowRocketChatPostMessageExceptionWithExceptionInformation_When_GetSystemUserFails()
      throws RocketChatUserNotInitializedException {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);
    doThrow(restClientException).when(this.rocketChatCredentialsProvider)
        .getSystemUser();

    try {
      this.messageServiceProvider
          .postWelcomeMessageIfConfigured(RC_GROUP_ID, USER, CONSULTING_TYPE_SETTINGS_U25,
              exceptionInformation);
      fail("Expected exception: RocketChatPostWelcomeMessageException");
    } catch (RocketChatPostWelcomeMessageException exception) {
      assertTrue("Excepted RocketChatPostWelcomeMessageException thrown", true);
      assertNotNull(exception.getExceptionInformation());
    }
  }

  @Test
  public void postWelcomeMessageIfConfigured_Should_ThrowRocketChatPostMessageExceptionWithExceptionInformation_When_PostRcMessageFails()
      throws RocketChatUserNotInitializedException {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);
    RocketChatCredentials credentials = mock(RocketChatCredentials.class);
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    when(this.rocketChatCredentialsProvider.getSystemUser()).thenReturn(credentials);
    doThrow(restClientException).when(this.messageControllerApi)
        .createMessage(any(), any(), any(), any());

    try {
      this.messageServiceProvider
          .postWelcomeMessageIfConfigured(RC_GROUP_ID, USER, CONSULTING_TYPE_SETTINGS_U25,
              exceptionInformation);
      fail("Expected exception: RocketChatPostWelcomeMessageException");
    } catch (RocketChatPostWelcomeMessageException exception) {
      assertTrue("Excepted RocketChatPostWelcomeMessageException thrown", true);
      assertNotNull(exception.getExceptionInformation());
    }
  }

  @Test
  public void postWelcomeMessageIfConfigured_Should_CallCreateMessageFromMessageServiceWithCorrectParams_When_EverythingSucceeds()
      throws RocketChatUserNotInitializedException, RocketChatPostWelcomeMessageException {
    EasyRandom easyRandom = new EasyRandom();
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);
    RocketChatCredentials credentials = easyRandom.nextObject(RocketChatCredentials.class);
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    when(this.rocketChatCredentialsProvider.getSystemUser()).thenReturn(credentials);
    ArgumentCaptor<MessageDTO> captor = ArgumentCaptor.forClass(MessageDTO.class);

    this.messageServiceProvider.postWelcomeMessageIfConfigured(RC_GROUP_ID, USER,
        CONSULTING_TYPE_SETTINGS_U25, exceptionInformation);

    verify(messageControllerApi, times(1))
        .createMessage(anyString(), anyString(), eq(RC_GROUP_ID), captor.capture());
    assertThat(captor.getValue().getMessage(),
        is(CONSULTING_TYPE_SETTINGS_U25.getWelcomeMessage().getWelcomeMessageText()));
  }

  @Test
  public void postFurtherStepsOrSaveSessionDataMessageIfConfigured_ShouldNot_CallMessageService_When_NoFurtherStepsAndNoSaveSessionDataMessageConfigured()
      throws RocketChatPostFurtherStepsMessageException {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);

    this.messageServiceProvider
        .postFurtherStepsOrSaveSessionDataMessageIfConfigured(RC_GROUP_ID,
            CONSULTING_TYPE_SETTINGS_WITHOUT_FURTHER_STEPS__AND_SAVE_SESSION_DATA_MESSAGE,
            exceptionInformation);

    verifyNoInteractions(messageControllerApi);
  }

  @Test(expected = RocketChatPostFurtherStepsMessageException.class)
  public void postFurtherStepsOrSaveSessionDataMessageIfConfigured_Should_ThrowRocketChatPostFurtherStepsMessageExceptionWithExceptionInformation_When_PostRcMessageFails()
      throws RocketChatPostFurtherStepsMessageException {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    doThrow(restClientException).when(this.messageControllerApi)
        .saveAliasOnlyMessage(any(), any());

    this.messageServiceProvider.postFurtherStepsOrSaveSessionDataMessageIfConfigured(RC_GROUP_ID,
        CONSULTING_TYPE_SETTINGS_WITH_FURTHER_STEPS_MESSAGE, exceptionInformation);
  }

  @Test
  public void postFurtherStepsOrSaveSessionDataMessageIfConfigured_Should_SaveFurtherStepsMessage_When_Configured()
      throws RocketChatPostFurtherStepsMessageException {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    ArgumentCaptor<AliasOnlyMessageDTO> captor = ArgumentCaptor.forClass(AliasOnlyMessageDTO.class);

    this.messageServiceProvider.postFurtherStepsOrSaveSessionDataMessageIfConfigured(RC_GROUP_ID,
        CONSULTING_TYPE_SETTINGS_WITH_FURTHER_STEPS_MESSAGE, exceptionInformation);

    verify(messageControllerApi, times(1)).saveAliasOnlyMessage(any(), captor.capture());
    assertThat(captor.getValue().getMessageType(), is(MessageType.FURTHER_STEPS));
  }

  @Test
  public void postFurtherStepsOrSaveSessionDataMessageIfConfigured_Should_SaveSessionDataMessage_When_Configured()
      throws RocketChatPostFurtherStepsMessageException {
    CreateEnquiryExceptionInformation exceptionInformation = mock(
        CreateEnquiryExceptionInformation.class);
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    ArgumentCaptor<AliasOnlyMessageDTO> captor = ArgumentCaptor.forClass(AliasOnlyMessageDTO.class);

    this.messageServiceProvider.postFurtherStepsOrSaveSessionDataMessageIfConfigured(RC_GROUP_ID,
        CONSULTING_TYPE_SETTINGS_WITH_UPDATE_SESSION_DATA_MESSAGE, exceptionInformation);

    verify(messageControllerApi, times(1)).saveAliasOnlyMessage(any(), captor.capture());
    assertThat(captor.getValue().getMessageType(), is(MessageType.UPDATE_SESSION_DATA));
  }

  @Test
  public void getMessageStream_should_call_messages_api()
      throws RocketChatGetMessagesStreamException {
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);

    this.messageServiceProvider.getMessages(RC_CREDENTIALS, RC_GROUP_ID);

    verify(messageControllerApi, times(1)).getMessageStream(eq(RC_CREDENTIALS.getRocketChatToken()),
        eq(RC_CREDENTIALS.getRocketChatUserId()), eq(RC_GROUP_ID));
  }

  @Test(expected = RocketChatGetMessagesStreamException.class)
  public void getMessageStream_should_fail_with_message_stream_exception()
      throws RocketChatGetMessagesStreamException {
    HttpHeaders headers = mock(HttpHeaders.class);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    doThrow(restClientException).when(
        messageControllerApi).getMessageStream(anyString(), anyString(), anyString());

    this.messageServiceProvider.getMessages(RC_CREDENTIALS, RC_GROUP_ID);
  }
}

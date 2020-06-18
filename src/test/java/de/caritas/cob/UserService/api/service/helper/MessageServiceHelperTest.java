package de.caritas.cob.UserService.api.service.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.UserService.api.exception.MessageServiceHelperException;
import de.caritas.cob.UserService.api.service.LogService;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceHelperTest {

  private final String FIELD_NAME_POST_MESSAGE_API_URL = "messageServiceApiPostMessageUrl";
  private final String POST_MESSAGE_API_URL = "http://caritas.local/service/messages/new";
  private final String MESSAGE = "Lorem ipsum";
  private final String RC_USER_ID = "kasdka495ejir";
  private final String RC_TOKEN = "asjdiasd98w4tjrt";
  private final String RC_GROUP_ID = "jas89uwj4et";
  private final String POST_MESSAGE_AS_SYSTEM_USER_NAME = "postMessageAsSystemUser";

  @Mock
  private RestTemplate restTemplate;
  @Mock
  private ServiceHelper serviceHelper;
  @Mock
  private LogService logService;
  @InjectMocks
  private MessageServiceHelper messageServiceHelper;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(messageServiceHelper,
        messageServiceHelper.getClass().getDeclaredField(FIELD_NAME_POST_MESSAGE_API_URL),
        POST_MESSAGE_API_URL);
  }

  @Test
  public void postMessage_Should_ReturnAgencyServiceHelperException_OnError() throws Exception {

    MessageServiceHelperException exception = new MessageServiceHelperException(new Exception());

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any()))
            .thenThrow(exception);

    try {
      messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID);
      fail("Expected exception: MessageServiceHelperException");
    } catch (MessageServiceHelperException messageServiceHelperException) {
      assertTrue("Excepted MessageServiceHelperException thrown", true);
    }

  }

  @Test
  public void postMessage_Should_ReturnTrue_WhenAPIResponseIsCreated() throws Exception {

    ResponseEntity<Void> response = new ResponseEntity<Void>(HttpStatus.CREATED);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any()))
            .thenReturn(response);

    assertTrue(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID));
  }

  @Test
  public void postMessage_Should_ReturnFalseAndLogResponse_WhenAPIResponseIsNotCreated()
      throws Exception {

    ResponseEntity<Void> response = new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);

    when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
        ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Void>>any()))
            .thenReturn(response);

    assertFalse(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID));

    verify(logService, times(1)).logMessageServiceHelperException((Mockito.anyString()));
  }

}

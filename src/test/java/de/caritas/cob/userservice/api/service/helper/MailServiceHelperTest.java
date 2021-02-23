package de.caritas.cob.userservice.api.service.helper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.model.mailservice.MailsDTO;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class MailServiceHelperTest {

  private final String FIELD_NAME_MAIL_SERVICE_API_URL = "mailServiceApiSendMailUrl";
  private final String MAIL_SERVICE_URL = "http://caritas.local/service/mails/send";

  @Mock
  private Logger logger;

  @Mock
  private ServiceHelper serviceHelper;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private MailServiceHelper mailServiceHelper;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(mailServiceHelper,
        mailServiceHelper.getClass().getDeclaredField(FIELD_NAME_MAIL_SERVICE_API_URL),
        MAIL_SERVICE_URL);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void sendEmailNotification_Should_CallMessageService() {

    when(serviceHelper.getCsrfHttpHeaders()).thenReturn(getCsrfHttpHeaders());
    when(restTemplate.exchange(ArgumentMatchers.eq(MAIL_SERVICE_URL),
        ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(),
        ArgumentMatchers.eq(Void.class)))
            .thenReturn(new ResponseEntity<Void>(HttpStatus.BAD_REQUEST));

    mailServiceHelper.sendEmailNotification(new MailsDTO());

    verify(restTemplate, times(1)).exchange(ArgumentMatchers.eq(MAIL_SERVICE_URL),
        ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(),
        ArgumentMatchers.eq(Void.class));

  }

  @Test
  public void sendEmailNotification_ShouldLog_WhenResponseCodeFromMailServerIsNotOk() {

    when(serviceHelper.getCsrfHttpHeaders()).thenReturn(getCsrfHttpHeaders());
    when(restTemplate.exchange(ArgumentMatchers.eq(MAIL_SERVICE_URL),
        ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(),
        ArgumentMatchers.eq(Void.class)))
            .thenReturn(new ResponseEntity<Void>(HttpStatus.BAD_REQUEST));

    mailServiceHelper.sendEmailNotification(new MailsDTO());

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());

  }

  @Test
  public void sendEmailNotification_ShouldLogException_WhenExceptionOccursWhileCallingTheMailService() {

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "HttpServerErrorException");

    when(serviceHelper.getCsrfHttpHeaders()).thenReturn(getCsrfHttpHeaders());
    when(restTemplate.exchange(ArgumentMatchers.eq(MAIL_SERVICE_URL),
        ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.any(),
        ArgumentMatchers.eq(Void.class))).thenThrow(httpServerErrorException);

    mailServiceHelper.sendEmailNotification(new MailsDTO());

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());

  }

  private HttpHeaders getCsrfHttpHeaders() {
    String csrfToken = UUID.randomUUID().toString();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add("Cookie", "X-CSRF-TOKEN=" + csrfToken);
    httpHeaders.add("CSRF-TOKEN", csrfToken);

    return httpHeaders;
  }

}

package de.caritas.cob.userservice.api.service.helper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.config.apiclient.MailServiceApiControllerFactory;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.mailservice.generated.ApiClient;
import de.caritas.cob.userservice.mailservice.generated.web.MailsControllerApi;
import de.caritas.cob.userservice.mailservice.generated.web.model.ErrorMailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

  @Mock private Logger logger;

  @Mock private SecurityHeaderSupplier securityHeaderSupplier;

  @Mock private MailsControllerApi mailsControllerApi;

  @Mock private ApiClient apiClient;

  @Mock MailServiceApiControllerFactory mailServiceApiControllerFactory;

  @InjectMocks private MailService mailService;

  @BeforeEach
  public void setup() throws NoSuchFieldException, SecurityException {
    setInternalState(MailService.class, "log", logger);
    when(mailServiceApiControllerFactory.createControllerApi()).thenReturn(mailsControllerApi);
    when(this.mailsControllerApi.getApiClient()).thenReturn(this.apiClient);
  }

  @Test
  public void sendEmailNotification_Should_CallMailService() {
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(getCsrfHttpHeaders());

    mailService.sendEmailNotification(new MailsDTO());

    verify(mailsControllerApi, times(1)).sendMails(any());
  }

  @Test
  public void
      sendEmailNotification_ShouldLogException_WhenExceptionOccursWhileCallingTheMailService() {
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(getCsrfHttpHeaders());
    doThrow(new RuntimeException()).when(this.mailsControllerApi).sendMails(any());

    mailService.sendEmailNotification(new MailsDTO());

    verify(logger, atLeastOnce()).error(anyString(), any(Exception.class));
  }

  @Test
  public void sendErrorEmailNotification_Should_CallMailService() {
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(getCsrfHttpHeaders());

    mailService.sendErrorEmailNotification(new ErrorMailDTO());

    verify(mailsControllerApi, times(1)).sendErrorMail(any());
  }

  @Test
  public void
      sendErrorEmailNotification_ShouldLogException_WhenExceptionOccursWhileCallingTheMailService() {
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(getCsrfHttpHeaders());
    doThrow(new RuntimeException()).when(this.mailsControllerApi).sendErrorMail(any());

    mailService.sendErrorEmailNotification(new ErrorMailDTO());

    verify(logger, atLeastOnce()).error(anyString(), any(Exception.class));
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

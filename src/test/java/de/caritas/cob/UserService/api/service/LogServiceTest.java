package de.caritas.cob.UserService.api.service;

import static de.caritas.cob.UserService.testHelper.TestConstants.EXCEPTION;
import static net.therore.logback.EventMatchers.text;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import javax.ws.rs.BadRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import net.therore.logback.LogbackRule;


@RunWith(MockitoJUnitRunner.class)
public class LogServiceTest {

  private final String ERROR_MESSAGE = "Error message";
  private final String INTERNAL_SERVER_ERROR_TEXT = "Internal Server Error: ";
  private final String ROCKET_CHAT_ERROR_TEXT = "Rocket.Chat Error: ";
  private final String AGENCY_ERROR_TEXT = "AgencyServiceHelper error: ";
  private final String MAIL_SERVICE_ERROR_TEXT = "MailServiceHelper error: ";
  private final String KEYCLOAK_ERROR_TEXT = "Keycloak error: ";
  private final String BAD_REQUEST_ERROR_TEXT = "Bad Request: ";
  private final String DB_INCONSITENCY_ERROR_TEXT = "Database inconsistency: ";
  private final String UNAUTHORIZED_ERROR_TEXT = "Unauthorized: ";
  private final String FORBIDDEN_WARNING_TEXT = "Forbidden: ";
  private final String EMAIL_NOTIFICATION_ERROR_TEXT = "EmailNotificationFacade error: ";
  private final String ACCEPT_ENQUIRY_ERROR_TEXT = "AcceptEnquiryFacade error: ";
  private final String MESSAGESERVICE_HELPER_ERROR_TEXT = "MessageServiceHelper error: ";
  private final String ASSIGN_SESSION_FACADE_WARNING_TEXT = "AssignSessionFacade warning: ";
  private final String ASSIGN_SESSION_FACADE_ERROR_TEXT = "AssignSessionFacade error: ";
  private final String DECRYPTION_ERROR = "Decryption of message error: ";
  private final String TRUNCATION_ERROR = "Truncation of message error: ";
  private final String VALIDATION_ERROR = "Validation error: ";

  @InjectMocks
  private LogService logService;

  @Mock
  Exception exception;

  @Mock
  BadRequestException badRequestException;
  @Mock
  NoSuchElementException noSuchElementException;
  @Mock
  IllegalArgumentException illegalArgumentEcxeption;

  @Rule
  public LogbackRule rule = new LogbackRule();

  @Before
  public void setup() {
    logService = new LogService();

  }

  @Test
  public void logDatabaseError_Should_LogExceptionStackTrace() {

    logService.logDatabaseError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));

  }

  @Test
  public void logDatabaseInconsistency_Should_LogErrorMessage() {

    logService.logDatabaseInconsistency(ERROR_MESSAGE);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(DB_INCONSITENCY_ERROR_TEXT + ERROR_MESSAGE)));

  }

  @Test
  public void logKeycloakError_Should_LogExceptionStackTrace() {

    logService.logKeycloakError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));

  }

  @Test
  public void logKeycloakError_Should_LogExceptionStackTraceAndErrorMessage() {

    logService.logKeycloakError(ERROR_MESSAGE, exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
    verify(rule.getLog(), times(1)).contains(argThat(text(KEYCLOAK_ERROR_TEXT + ERROR_MESSAGE)));

  }

  @Test
  public void logKeycloakError_Should_LogErrorMessage() {

    logService.logKeycloakError(ERROR_MESSAGE);
    verify(rule.getLog(), times(1)).contains(argThat(text(KEYCLOAK_ERROR_TEXT)));

  }

  @Test
  public void logBadRequestException_Should_LogExceptionStackTrace() {

    logService.logBadRequestException(badRequestException);
    verify(badRequestException, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logBadRequest_Should_LogErrorMessage() {

    logService.logBadRequest(ERROR_MESSAGE);
    verify(rule.getLog(), times(1)).contains(argThat(text(BAD_REQUEST_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logUnauthorized_Should_LogErrorMessage() {

    logService.logUnauthorized(ERROR_MESSAGE);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(UNAUTHORIZED_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logForbidden_Should_LogWarningMessage() {

    logService.logForbidden(ERROR_MESSAGE);
    verify(rule.getLog(), times(1)).contains(argThat(text(FORBIDDEN_WARNING_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logInternalServerError_Should_LogErrorMessage() {

    logService.logInternalServerError(ERROR_MESSAGE);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(INTERNAL_SERVER_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logInternalServerError_Should_LogErrorMessageAndExceptionStackTrace() {

    logService.logInternalServerError(ERROR_MESSAGE, exception);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(INTERNAL_SERVER_ERROR_TEXT + ERROR_MESSAGE)));
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logRocketChatError_should_LogErrorMessage() {

    logService.logRocketChatError(ERROR_MESSAGE);
    verify(rule.getLog(), times(1)).contains(argThat(text(ROCKET_CHAT_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logRocketChatError_should_LogErrorMessageErrorAndErrorType() {

    String error = "Error";
    String errorType = "Error type";
    logService.logRocketChatError(ERROR_MESSAGE, error, errorType);
    verify(rule.getLog(), times(1)).contains(argThat(text(ROCKET_CHAT_ERROR_TEXT + ERROR_MESSAGE)));
    verify(rule.getLog(), times(1)).contains(argThat(text(error)));
    verify(rule.getLog(), times(1)).contains(argThat(text(errorType)));
  }

  @Test
  public void logRocketChatError_should_LogErrorMessageAndExceptionStackTrace() {

    logService.logRocketChatError(ERROR_MESSAGE, exception);
    verify(rule.getLog(), times(1)).contains(argThat(text(ROCKET_CHAT_ERROR_TEXT + ERROR_MESSAGE)));
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logAgencyServiceHelperException_Should_LogErrorMessageAndExceptionStackTrace() {

    logService.logAgencyServiceHelperException(ERROR_MESSAGE, exception);
    verify(rule.getLog(), times(1)).contains(argThat(text(AGENCY_ERROR_TEXT + ERROR_MESSAGE)));
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logAgencyServiceHelperException_Should_LogExceptionStackTrace() {

    logService.logAgencyServiceHelperException(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logNoSuchElementException_Should_LogExceptionStackTrace() {

    logService.logNoSuchElementException(noSuchElementException);
    verify(noSuchElementException, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logIllegalArgumentException_Should_LogExceptionStackTrace() {

    logService.logIllegalArgumentException(illegalArgumentEcxeption);
    verify(illegalArgumentEcxeption, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logMailServiceHelperException_Should_LogErrorMessageAndExceptionStackTrace() {

    logService.logMailServiceHelperException(ERROR_MESSAGE, exception);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(MAIL_SERVICE_ERROR_TEXT + ERROR_MESSAGE)));
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logMailServiceHelperException_Should_LogExceptionStackTrace() {

    logService.logMailServiceHelperException(ERROR_MESSAGE);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(MAIL_SERVICE_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logEmailNotificationFacadeError_should_LogErrorMessage() {

    logService.logEmailNotificationFacadeError(ERROR_MESSAGE);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(EMAIL_NOTIFICATION_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logAcceptEnquiryFacadeError_should_LogErrorMessage() {

    logService.logAcceptEnquiryFacadeError(ERROR_MESSAGE);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(ACCEPT_ENQUIRY_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logMessageServiceHelperException_should_LogErrorMessage() {

    logService.logMessageServiceHelperException(ERROR_MESSAGE, EXCEPTION);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(MESSAGESERVICE_HELPER_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logMessageServiceHelperException_Should_LogErrorMessageAndExceptionStackTrace() {

    logService.logMessageServiceHelperException(ERROR_MESSAGE, exception);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(MESSAGESERVICE_HELPER_ERROR_TEXT + ERROR_MESSAGE)));
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logAssignSessionFacadeWarning_should_LogErrorMessage() {

    logService.logAssignSessionFacadeWarning(ERROR_MESSAGE);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(ASSIGN_SESSION_FACADE_WARNING_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logAssignSessionFacadeError_should_LogErrorMessage() {

    logService.logAssignSessionFacadeError(ERROR_MESSAGE);
    verify(rule.getLog(), times(1))
        .contains(argThat(text(ASSIGN_SESSION_FACADE_ERROR_TEXT + ERROR_MESSAGE)));
  }

  @Test
  public void logMonitoringHelperError_Should_LogExceptionStackTrace() {

    logService.logMonitoringHelperError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logEncryptionServiceError_Should_LogExceptionStackTrace() {

    logService.logEncryptionServiceError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logEncryptionPossibleBadKeyError_Should_LogExceptionStackTrace() {

    logService.logEncryptionPossibleBadKeyError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logInfo_Should_LogMessage() {

    logService.logInfo(ERROR_MESSAGE);
    verify(rule.getLog(), times(1)).contains(argThat(text(ERROR_MESSAGE)));
  }

  @Test
  public void logDecryptionError_Should_LogExceptionStackTraceAndErrorMessage() {

    logService.logDecryptionError(ERROR_MESSAGE, exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
    verify(rule.getLog(), times(1)).contains(argThat(text(DECRYPTION_ERROR + ERROR_MESSAGE)));
  }

  @Test
  public void logTruncationError_Should_LogExceptionStackTraceAndErrorMessage() {

    logService.logTruncationError(ERROR_MESSAGE, exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
    verify(rule.getLog(), times(1)).contains(argThat(text(TRUNCATION_ERROR + ERROR_MESSAGE)));
  }

  @Test
  public void logValidationError_Should_LogMessage() {

    logService.logValidationError(ERROR_MESSAGE);
    verify(rule.getLog(), times(1)).contains(argThat(text(VALIDATION_ERROR + ERROR_MESSAGE)));
  }

}

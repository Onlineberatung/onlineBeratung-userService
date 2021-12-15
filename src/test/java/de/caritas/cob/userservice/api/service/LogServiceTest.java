package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.service.LogService.ASSIGN_SESSION_FACADE_ERROR_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.ASSIGN_SESSION_FACADE_WARNING_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.BAD_REQUEST_ERROR_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.EMAIL_NOTIFICATION_ERROR_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.EMAIL_NOTIFICATION_WARNING_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.FORBIDDEN_WARNING_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.INTERNAL_SERVER_ERROR_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.KEYCLOAK_ERROR_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.MAIL_SERVICE_ERROR_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.ROCKET_CHAT_ERROR_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.STATISTICS_EVENT_PROCESSING_ERROR;
import static de.caritas.cob.userservice.api.service.LogService.STATISTICS_EVENT_PROCESSING_WARNING;
import static de.caritas.cob.userservice.api.service.LogService.UNAUTHORIZED_WARNING_TEXT;
import static de.caritas.cob.userservice.testHelper.TestConstants.EXCEPTION;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.io.PrintWriter;
import javax.ws.rs.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;


@RunWith(MockitoJUnitRunner.class)
public class LogServiceTest {

  private final String ERROR_MESSAGE = "Error message";

  @Mock
  Exception exception;

  @Mock
  BadRequestException badRequestException;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void logDatabaseError_Should_LogExceptionStackTrace() {

    LogService.logDatabaseError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));

  }

  @Test
  public void logKeycloakError_Should_LogExceptionStackTraceAndErrorMessage() {

    LogService.logKeycloakError(ERROR_MESSAGE, exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
    verify(logger, times(1)).error(anyString(), eq(KEYCLOAK_ERROR_TEXT), eq(ERROR_MESSAGE));

  }

  @Test
  public void logKeycloakError_Should_LogErrorMessage() {

    LogService.logKeycloakError(ERROR_MESSAGE);
    verify(logger, times(1)).error(anyString(), eq(KEYCLOAK_ERROR_TEXT), eq(ERROR_MESSAGE));

  }

  @Test
  public void logKeycloakInfo_Should_LogExceptionStackTraceAndErrorMessage() {

    LogService.logKeycloakInfo(ERROR_MESSAGE, exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
    verify(logger, times(1)).info(anyString(), eq(KEYCLOAK_ERROR_TEXT), eq(ERROR_MESSAGE));

  }

  @Test
  public void logBadRequestException_Should_LogExceptionStackTrace() {

    LogService.logBadRequestException(badRequestException);
    verify(badRequestException, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logBadRequest_Should_LogErrorMessage() {

    LogService.logBadRequest(ERROR_MESSAGE);
    verify(logger, times(1)).warn(anyString(), eq(BAD_REQUEST_ERROR_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logUnauthorized_Should_LogErrorMessage() {

    LogService.logUnauthorized(ERROR_MESSAGE);
    verify(logger, times(1))
        .warn(anyString(), eq(UNAUTHORIZED_WARNING_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logForbidden_Should_LogWarningMessage() {

    LogService.logForbidden(ERROR_MESSAGE);
    verify(logger, times(1)).warn(anyString(), eq(FORBIDDEN_WARNING_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logInternalServerError_Should_LogErrorMessage() {

    LogService.logInternalServerError(ERROR_MESSAGE);
    verify(logger, times(1))
        .error(anyString(), eq(INTERNAL_SERVER_ERROR_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logInternalServerError_Should_LogErrorMessageAndExceptionStackTrace() {

    LogService.logInternalServerError(ERROR_MESSAGE, exception);
    verify(logger, times(1))
        .error(anyString(), eq(INTERNAL_SERVER_ERROR_TEXT), eq(ERROR_MESSAGE));
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logRocketChatError_should_LogErrorMessage() {

    LogService.logRocketChatError(ERROR_MESSAGE);
    verify(logger, times(1)).error(anyString(), eq(ROCKET_CHAT_ERROR_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logRocketChatError_should_LogErrorMessageErrorAndErrorType() {

    String error = "Error";
    String errorType = "Error type";
    LogService.logRocketChatError(ERROR_MESSAGE, error, errorType);
    verify(logger, times(1)).error(anyString(), eq(ROCKET_CHAT_ERROR_TEXT), eq(ERROR_MESSAGE),
        eq(error), eq(errorType));
  }

  @Test
  public void logRocketChatError_should_LogErrorMessageAndExceptionStackTrace() {

    LogService.logRocketChatError(ERROR_MESSAGE, exception);
    verify(logger, times(1)).error(anyString(), eq(ROCKET_CHAT_ERROR_TEXT), eq(ERROR_MESSAGE));
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logMailServiceHelperException_Should_LogErrorMessageAndExceptionStackTrace() {

    LogService.logMailServiceException(ERROR_MESSAGE, exception);
    verify(logger, times(1))
        .error(anyString(), eq(MAIL_SERVICE_ERROR_TEXT), eq(ERROR_MESSAGE));
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logEmailNotificationFacadeError_should_LogErrorMessage() {

    LogService.logEmailNotificationFacadeError(ERROR_MESSAGE);
    verify(logger, times(1))
        .error(anyString(), eq(EMAIL_NOTIFICATION_ERROR_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logEmailNotificationFacadeWarning_should_LogMessageAndException() {

    LogService.logEmailNotificationFacadeWarning(ERROR_MESSAGE, EXCEPTION);
    verify(logger, times(1))
        .warn(anyString(), eq(EMAIL_NOTIFICATION_WARNING_TEXT), eq(ERROR_MESSAGE));
    verify(logger, times(1))
        .warn(anyString(), eq(EMAIL_NOTIFICATION_WARNING_TEXT), eq(getStackTrace(EXCEPTION)));
  }

  @Test
  public void logEmailNotificationFacadeError_should_LogErrorMessageAndException() {

    LogService.logEmailNotificationFacadeError(ERROR_MESSAGE, EXCEPTION);
    verify(logger, times(1))
        .error(anyString(), eq(EMAIL_NOTIFICATION_ERROR_TEXT), eq(ERROR_MESSAGE));
    verify(logger, times(1))
        .error(anyString(), eq(EMAIL_NOTIFICATION_ERROR_TEXT), eq(getStackTrace(EXCEPTION)));
  }

  @Test
  public void logEmailNotificationFacadeError_should_LogErrorException() {

    LogService.logEmailNotificationFacadeError(EXCEPTION);
    verify(logger, times(1))
        .error(anyString(), eq(EMAIL_NOTIFICATION_ERROR_TEXT), eq(getStackTrace(EXCEPTION)));
  }

  @Test
  public void logAssignSessionFacadeWarning_should_LogErrorMessage() {

    LogService.logAssignSessionFacadeWarning(new Exception(ERROR_MESSAGE));
    verify(logger, times(1))
        .warn(anyString(), eq(ASSIGN_SESSION_FACADE_WARNING_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logEncryptionServiceError_Should_LogExceptionStackTrace() {

    LogService.logEncryptionServiceError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logEncryptionPossibleBadKeyError_Should_LogExceptionStackTrace() {

    LogService.logEncryptionPossibleBadKeyError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logInfo_Should_LogMessage() {

    LogService.logInfo(ERROR_MESSAGE);
    verify(logger, times(1)).info(ERROR_MESSAGE);
  }

  @Test
  public void logInfo_Should_LogExceptionStackTrace() {
    LogService.logInfo(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logCreateEnquiryMessageException_Should_LogExceptionStackTrace() {

    LogService.logCreateEnquiryMessageException(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logWarn_Should_LogExceptionStackTrace() {

    LogService.logWarn(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logAssignSessionFacadeError_Should_LogExceptionStackTraceAndErrorMessage() {

    when(exception.getMessage()).thenReturn(ERROR_MESSAGE);
    LogService.logAssignSessionFacadeError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
    verify(logger, times(1))
        .error(anyString(), eq(ASSIGN_SESSION_FACADE_ERROR_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logInternalServerError_Should_LogExceptionStackTrace() {

    LogService.logInternalServerError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logForbidden_Should_LogExceptionStackTrace() {

    LogService.logForbidden(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logUnauthorized_Should_LogExceptionStackTrace() {

    LogService.logUnauthorized(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logWarn_Should_LogExceptionStackTraceAndHttpStatusReasonPhrase() {

    LogService.logWarn(HttpStatus.BAD_REQUEST, exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
    verify(logger, times(1))
        .warn(anyString(), eq(HttpStatus.BAD_REQUEST.getReasonPhrase()), anyString());
  }

  @Test
  public void logStatisticEventError_Should_LogExceptionStackTraceAndErrorMessage() {

    LogService.logStatisticsEventError(EXCEPTION);
    verify(logger, times(1))
        .error(anyString(), eq(STATISTICS_EVENT_PROCESSING_ERROR), eq(getStackTrace(EXCEPTION)));
  }

  @Test
  public void logStatisticEventWarning_Should_LogErrorMessageAsWarning() {

    LogService.logStatisticsEventWarning(ERROR_MESSAGE);
    verify(logger, times(1))
        .warn(anyString(), eq(STATISTICS_EVENT_PROCESSING_WARNING), eq(ERROR_MESSAGE));
  }

}

package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.service.LogService.ASSIGN_SESSION_FACADE_ERROR_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.ASSIGN_SESSION_FACADE_WARNING_TEXT;
import static de.caritas.cob.userservice.api.service.LogService.FORBIDDEN_WARNING_TEXT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.io.PrintWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class LogServiceTest {

  private final String ERROR_MESSAGE = "Error message";

  @Mock Exception exception;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void logDatabaseError_Should_LogExceptionStackTrace() {

    LogService.logDatabaseError(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logForbidden_Should_LogWarningMessage() {

    LogService.logForbidden(ERROR_MESSAGE);
    verify(logger, times(1)).warn(anyString(), eq(FORBIDDEN_WARNING_TEXT), eq(ERROR_MESSAGE));
  }

  @Test
  public void logAssignSessionFacadeWarning_should_LogErrorMessage() {

    LogService.logAssignSessionFacadeWarning(new Exception(ERROR_MESSAGE));
    verify(logger, times(1))
        .warn(anyString(), eq(ASSIGN_SESSION_FACADE_WARNING_TEXT), eq(ERROR_MESSAGE));
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
  public void logRocketChatError_Should_LogExceptionMessage() {
    LogService.logRocketChatError(exception);
    verify(exception, atLeastOnce()).getMessage();
  }
}

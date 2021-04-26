package de.caritas.cob.userservice.api.service;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Service for logging
 */
public class LogService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogService.class);

  public static final String INTERNAL_SERVER_ERROR_TEXT = "Internal Server Error: ";
  public static final String ROCKET_CHAT_ERROR_TEXT = "Rocket.Chat Error: ";
  public static final String AGENCY_ERROR_TEXT = "AgencyServiceHelper error: ";
  public static final String MAIL_SERVICE_ERROR_TEXT = "MailServiceHelper error: ";
  public static final String DB_ERROR_TEXT = "Database error: ";
  public static final String KEYCLOAK_ERROR_TEXT = "Keycloak error: ";
  public static final String KEYCLOAK_EXCEPTION_TEXT = "Keycloak exception: ";
  public static final String BAD_REQUEST_ERROR_TEXT = "Bad Request: ";
  public static final String DB_INCONSISTENCY_ERROR_TEXT = "Database inconsistency: ";
  public static final String UNAUTHORIZED_WARNING_TEXT = "Unauthorized: ";
  public static final String FORBIDDEN_WARNING_TEXT = "Forbidden: ";
  public static final String ILLEGAL_ARGUMENT_ERROR_TEXT = "Illegal Argument: ";
  public static final String EMAIL_NOTIFICATION_ERROR_TEXT = "EmailNotificationFacade error: ";
  public static final String EMAIL_NOTIFICATION_WARNING_TEXT = "EmailNotificationFacade warning: ";
  public static final String ACCEPT_ENQUIRY_ERROR_TEXT = "AcceptEnquiryFacade error: ";
  public static final String ASSIGN_SESSION_FACADE_WARNING_TEXT = "AssignSessionFacade warning: ";
  public static final String ASSIGN_SESSION_FACADE_ERROR_TEXT = "AssignSessionFacade error: ";
  public static final String MONITORING_HELPER_ERROR_TEXT = "MonitoringHelper error: ";
  public static final String RC_ENCRYPTION_SERVICE_ERROR = "Encryption service error: ";
  public static final String RC_ENCRYPTION_BAD_KEY_SERVICE_ERROR =
      "Encryption service error - possible bad key error: ";
  public static final String DECRYPTION_ERROR = "Decryption of message error: ";
  public static final String TRUNCATION_ERROR = "Truncation of message error: ";
  public static final String VALIDATION_ERROR = "Validation error: ";
  public static final String CREATE_ENQUIRY_MESSAGE_ERROR = "CreateEnquiryMessageFacade error: ";
  public static final String CREATE_SESSION_FACADE_ERROR = "CreateSessionFacade error: ";

  private LogService() {}

  /**
   * Logs a database error.
   *
   * @param exception the exception
   */
  public static void logDatabaseError(Exception exception) {
    LOGGER.error("{}{}", DB_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Logs a database error.
   *
   * @param message the message
   * @param exception the exception
   */
  public static void logDatabaseError(String message, Exception exception) {
    LOGGER.error("{}{}", DB_ERROR_TEXT, message);
    LOGGER.error("{}{}", DB_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Logs a database error.
   *
   * @param message the message
   */
  public static void logDatabaseInconsistency(String message) {
    LOGGER.error("{}{}", DB_INCONSISTENCY_ERROR_TEXT, message);
  }

  /**
   * Logs a Keycloak error.
   *
   * @param exception the exception
   */
  public static void logKeycloakError(Exception exception) {
    LOGGER.error("{}{}", KEYCLOAK_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Logs a Keycloak error.
   *
   * @param message the message
   * @param exception the exception
   */
  public static void logKeycloakError(String message, Exception exception) {
    LOGGER.error("{}{}", KEYCLOAK_ERROR_TEXT, message);
    LOGGER.error("{}{}", KEYCLOAK_EXCEPTION_TEXT, getStackTrace(exception));
  }

  /**
   * Logs a Keycloak error.
   *
   * @param message error message
   */
  public static void logKeycloakError(String message) {
    LOGGER.error("{}{}", KEYCLOAK_ERROR_TEXT, message);
  }

  /**
   * Logs a Keycloak info.
   *
   * @param message the message
   * @param exception the exception
   */
  public static void logKeycloakInfo(String message, Exception exception) {
    LOGGER.info("{}{}", KEYCLOAK_ERROR_TEXT, message);
    LOGGER.info("{}{}", KEYCLOAK_EXCEPTION_TEXT, getStackTrace(exception));
  }

  /**
   * Bad Request Exception.
   *
   * @param exception the exception
   */
  public static void logBadRequestException(Exception exception) {
    LOGGER.warn("{}{}", BAD_REQUEST_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Bad Request error.
   *
   * @param message the message
   */
  public static void logBadRequest(String message) {
    LOGGER.warn("{}{}", BAD_REQUEST_ERROR_TEXT, message);
  }

  /**
   * Unauthorized warning.
   *
   * @param message the message
   */
  public static void logUnauthorized(String message) {
    LOGGER.warn("{}{}", UNAUTHORIZED_WARNING_TEXT, message);
  }

  /**
   * Unauthorized warning.
   *
   * @param exception the exception
   */
  public static void logUnauthorized(Exception exception) {
    LOGGER.warn("{}", getStackTrace(exception));
  }

  /**
   * Forbidden warning.
   *
   * @param message the message
   */
  public static void logForbidden(String message) {
    LOGGER.warn("{}{}", FORBIDDEN_WARNING_TEXT, message);
  }

  /**
   * Forbidden warning.
   *
   * @param exception the exception
   */
  public static void logForbidden(Exception exception) {
    LOGGER.warn("{}", getStackTrace(exception));
  }

  /**
   * Log internal server error.
   *
   * @param message the message
   */
  public static void logInternalServerError(String message) {
    LOGGER.error("{}{}", INTERNAL_SERVER_ERROR_TEXT, message);
  }

  /**
   * Log internal server error.
   *
   * @param message the message
   * @param exception the exception
   */
  public static void logInternalServerError(String message, Exception exception) {
    LOGGER.error("{}{}", INTERNAL_SERVER_ERROR_TEXT, message);
    LOGGER.error("{}", getStackTrace(exception));
  }

  /**
   * Log internal server error.
   *
   * @param exception the exception
   */
  public static void logInternalServerError(Exception exception) {
    LOGGER.error("UserService Api: {}, {}, {}", getStackTrace(exception), exception.getMessage(),
        nonNull(exception.getCause()) ? getStackTrace(exception.getCause()) : "No Cause");
  }

  /**
   * Rocket.Chat error.
   *
   * @param message the message
   */
  public static void logRocketChatError(String message) {
    LOGGER.error("{}{})", ROCKET_CHAT_ERROR_TEXT, message);
  }

  /**
   * Rocket.Chat error.
   *
   * @param ex the exception
   */
  public static void logRocketChatError(Exception ex) {
    LOGGER.error("{}{})", ROCKET_CHAT_ERROR_TEXT, getStackTrace(ex));
  }

  /**
   * Rocket.Chat error.
   *
   * @param message the message
   * @param error the error
   * @param errorType the errorType
   */
  public static void logRocketChatError(String message, String error, String errorType) {
    LOGGER.error("{}{} (Error: {} / ErrorType: {})", ROCKET_CHAT_ERROR_TEXT, message, error,
        errorType);
  }

  /**
   * Rocket.Chat Error with exception.
   *
   * @param exception the exception
   * @param message the message
   */
  public static void logRocketChatError(String message, Exception exception) {
    LOGGER.error("{}{}", ROCKET_CHAT_ERROR_TEXT, message);
    LOGGER.error("{}", getStackTrace(exception));
  }

  /**
   * IllegalArgumentException.
   *
   * @param exception the exception
   */
  public static void logIllegalArgumentException(IllegalArgumentException exception) {
    LOGGER.warn("{}{}", ILLEGAL_ARGUMENT_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * NoSuchElementException.
   *
   * @param exception the exception
   */
  public static void logNoSuchElementException(NoSuchElementException exception) {
    LOGGER.warn("{}{}", ILLEGAL_ARGUMENT_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Exception from AgencyServiceHelper.
   *
   * @param exception the exception
   */
  public static void logAgencyServiceHelperException(Exception exception) {
    LOGGER.error("{}{}", AGENCY_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Exception from AgencyServiceHelper.
   *
   * @param exception the exception
   * @param message the message
   */
  public static void logAgencyServiceHelperException(String message, Exception exception) {
    LOGGER.error("{}{}", AGENCY_ERROR_TEXT, message);
    LOGGER.error("{}{}", AGENCY_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Exception from MailServiceHelperException.
   *
   * @param message the message
   */
  public static void logMailServiceException(String message) {
    LOGGER.error("{}{}", MAIL_SERVICE_ERROR_TEXT, message);
  }

  /**
   * Exception from MailServiceHelperException.
   *
   * @param message the message
   * @param exception the exception
   */
  public static void logMailServiceException(String message, Exception exception) {
    LOGGER.error("{}{}", MAIL_SERVICE_ERROR_TEXT, message);
    LOGGER.error("{}{}", MAIL_SERVICE_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Error from EmailNotificationFacade.
   *
   * @param message the message
   */
  public static void logEmailNotificationFacadeError(String message) {
    LOGGER.error("{}{}", EMAIL_NOTIFICATION_ERROR_TEXT, message);
  }

  /**
   * Error from EmailNotificationFacade.
   *
   * @param message the message
   * @param exception the caused {@link Exception}
   */
  public static void logEmailNotificationFacadeError(String message, Exception exception) {
    LOGGER.error("{}{}", EMAIL_NOTIFICATION_ERROR_TEXT, message);
    LOGGER.error("{}{}", EMAIL_NOTIFICATION_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Error from EmailNotificationFacade.
   *
   * @param exception the caused {@link Exception}
   */
  public static void logEmailNotificationFacadeError(Exception exception) {
    LOGGER.error("{}{}", EMAIL_NOTIFICATION_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Warning from EmailNotificationFacade.
   *
   * @param message the message
   * @param exception the caused {@link Exception}
   */
  public static void logEmailNotificationFacadeWarning(String message, Exception exception) {
    LOGGER.warn("{}{}", EMAIL_NOTIFICATION_WARNING_TEXT, message);
    LOGGER.warn("{}{}", EMAIL_NOTIFICATION_WARNING_TEXT, getStackTrace(exception));
  }

  /**
   * Error from AcceptEnquiryFacade.
   *
   * @param message the message
   */
  public static void logAcceptEnquiryFacadeError(String message) {
    LOGGER.error("{}{}", ACCEPT_ENQUIRY_ERROR_TEXT, message);
  }

  /**
   * Warning from AssignSessionFacade.
   *
   * @param exception the exception
   */
  public static void logAssignSessionFacadeWarning(Exception exception) {
    LOGGER.warn("{}{}", ASSIGN_SESSION_FACADE_WARNING_TEXT, exception.getMessage());
    LOGGER.warn("{}", getStackTrace(exception));
  }

  /**
   * Error from AssignSessionFacade.
   *
   * @param message the message
   */
  public static void logAssignSessionFacadeError(String message) {
    LOGGER.error("{}{}", ASSIGN_SESSION_FACADE_ERROR_TEXT, message);
  }

  /**
   * Error from AssignSessionFacade.
   *
   * @param exception the exception
   */
  public static void logAssignSessionFacadeError(Exception exception) {
    LOGGER.error("{}{}", ASSIGN_SESSION_FACADE_ERROR_TEXT, exception.getMessage());
    LOGGER.error("{}", getStackTrace(exception));
  }

  /**
   * Logs monitoring exception.
   *
   * @param exception the exception
   */
  public static void logMonitoringHelperError(Exception exception) {
    LOGGER.error("{}{}", MONITORING_HELPER_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Logs an info message.
   *
   * @param msg The message
   */
  public static void logInfo(String msg) {
    LOGGER.info(msg);
  }

  /**
   * Logs a Encryption service error.
   *
   * @param exception the exception
   */
  public static void logEncryptionServiceError(Exception exception) {
    LOGGER.error(RC_ENCRYPTION_SERVICE_ERROR + "{}", getStackTrace(exception));
  }

  /**
   * Logs an encryption error.
   *
   * @param exception the exception
   */
  public static void logEncryptionPossibleBadKeyError(Exception exception) {
    LOGGER.error(RC_ENCRYPTION_BAD_KEY_SERVICE_ERROR + "{}", getStackTrace(exception));
  }

  /**
   * Error while decrypting a message.
   *
   * @param exception the exception
   * @param message the message
   */
  public static void logDecryptionError(String message, Exception exception) {
    LOGGER.error("{}{}", DECRYPTION_ERROR, message);
    LOGGER.error("{}{}", DECRYPTION_ERROR, getStackTrace(exception));
  }

  /**
   * Error while truncating a message.
   *
   * @param exception the exception
   * @param message the message
   */
  public static void logTruncationError(String message, Exception exception) {
    LOGGER.error("{}{}", TRUNCATION_ERROR, message);
    LOGGER.error("{}{}", TRUNCATION_ERROR, getStackTrace(exception));
  }

  /**
   * Error from DTO validation.
   *
   * @param message the message
   */
  public static void logValidationError(String message) {
    LOGGER.error("{}{}", VALIDATION_ERROR, message);
  }


  /**
   * Logs the exception message from creating the enquiry message
   *
   * @param exception the exception
   */
  public static void logCreateEnquiryMessageException(Exception exception) {
    LOGGER.error("{}{}", CREATE_ENQUIRY_MESSAGE_ERROR, getStackTrace(exception));
  }

  /**
   * Error while registering a new consulting type session.
   *
   * @param message Error message
   * @param exception Exception
   */
  public static void logCreateSessionFacadeError(String message, Exception exception) {
    LOGGER.error("{}{}", CREATE_SESSION_FACADE_ERROR, message);
    LOGGER.error("{}{}", CREATE_SESSION_FACADE_ERROR, getStackTrace(exception));
  }

  /**
   * Logs an warning message.
   *
   * @param exception The exception
   */
  public static void logWarn(Exception exception) {
    LOGGER.warn(getStackTrace(exception));
  }

  /**
   * Logs an warning message.
   *
   * @param message The message
   */
  public static void logWarn(String message) {
    LOGGER.warn(message);
  }

  /**
   * Logs an warning message.
   *
   * @param httpStatus http status
   * @param exception The exception
   */
  public static void logWarn(HttpStatus httpStatus, Exception exception) {
    LOGGER.warn("UserService API: {}: {}", httpStatus.getReasonPhrase(), getStackTrace(exception));
  }

  /**
   * Logs an debug message.
   *
   * @param message The message
   */
  public static void logDebug(String message) {
    LOGGER.debug(message);
  }

  /**
   * Logs an delete workflow error.
   *
   * @param e the cause exception
   */
  public static void logDeleteWorkflowError(Exception e) {
    LOGGER.error("UserService delete workflow error: {}", getStackTrace(e));
  }
}

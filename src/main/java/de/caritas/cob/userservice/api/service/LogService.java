package de.caritas.cob.userservice.api.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.httpresponses.WrongParameterException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for logging
 */
@Service
@Slf4j
public class LogService {

  public static final String INTERNAL_SERVER_ERROR_TEXT = "Internal Server Error: ";
  public static final String ROCKET_CHAT_ERROR_TEXT = "Rocket.Chat Error: ";
  public static final String AGENCY_ERROR_TEXT = "AgencyServiceHelper error: ";
  public static final String MAIL_SERVICE_ERROR_TEXT = "MailServiceHelper error: ";
  public static final String DB_ERROR_TEXT = "Database error: ";
  public static final String KEYCLOAK_ERROR_TEXT = "Keycloak error: ";
  public static final String KEYCLOAK_EXCEPTION_TEXT = "Keycloak exception: ";
  public static final String BAD_REQUEST_ERROR_TEXT = "Bad Request: ";
  public static final String DB_INCONSITENCY_ERROR_TEXT = "Database inconsistency: ";
  public static final String UNAUTHORIZED_WARNING_TEXT = "Unauthorized: ";
  public static final String FORBIDDEN_WARNING_TEXT = "Forbidden: ";
  public static final String ILLEGAL_ARGUMENT_ERROR_TEXT = "Illegal Argument: ";
  public static final String EMAIL_NOTIFICATION_ERROR_TEXT = "EmailNotificationFacade error: ";
  public static final String ACCEPT_ENQUIRY_ERROR_TEXT = "AcceptEnquiryFacade error: ";
  public static final String MESSAGESERVICE_HELPER_ERROR_TEXT = "MessageServiceHelper error: ";
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

  /**
   * Logs a database error
   */
  public void logDatabaseError(Exception exception) {
    log.error("{}{}", DB_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Logs a database error
   */
  public void logDatabaseError(String message, Exception exception) {
    log.error("{}{}", DB_ERROR_TEXT, message);
    log.error("{}{}", DB_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Logs a database error
   */
  public void logDatabaseInconsistency(String message) {
    log.error("{}{}", DB_INCONSITENCY_ERROR_TEXT, message);
  }

  /**
   * Logs a Keycloak error
   */
  public void logKeycloakError(Exception exception) {
    log.error("{}{}", KEYCLOAK_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Logs a Keycloak error
   */
  public void logKeycloakError(String message, Exception exception) {
    log.error("{}{}", KEYCLOAK_ERROR_TEXT, message);
    log.error("{}{}", KEYCLOAK_EXCEPTION_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Logs a Keycloak error
   *
   * @param message error message
   */
  public void logKeycloakError(String message) {
    log.error("{}{}", KEYCLOAK_ERROR_TEXT, message);
  }

  /**
   * javax Bad Request Exception
   */
  public void logBadRequestException(Exception exception) {
    log.warn("{}{}", BAD_REQUEST_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Bad Request/wrong parameter exception
   */
  public void logBadRequestException(WrongParameterException exception) {
    log.error("{}{}", BAD_REQUEST_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Bad Request error
   */
  public void logBadRequest(String message) {
    log.warn("{}{}", BAD_REQUEST_ERROR_TEXT, message);
  }

  /**
   * Unauthorized warning
   */
  public void logUnauthorized(String message) {
    log.warn("{}{}", UNAUTHORIZED_WARNING_TEXT, message);
  }

  /**
   * Unauthorized warning
   */
  public void logUnauthorized(Exception exception) {
    log.warn("{}", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Forbidden warning
   */
  public void logForbidden(String message) {
    log.warn("{}{}", FORBIDDEN_WARNING_TEXT, message);
  }

  /**
   * Forbidden warning
   */
  public void logForbidden(Exception exception) {
    log.warn("{}", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * javax Bad Request Exception
   */
  public void logInternalServerError(String message) {
    log.error("{}{}", INTERNAL_SERVER_ERROR_TEXT, message);
  }

  /**
   * javax Bad Request Exception
   */
  public void logInternalServerError(String message, Exception exception) {
    log.error("{}{}", INTERNAL_SERVER_ERROR_TEXT, message);
    log.error("{}", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Log internal server error
   */
  public void logInternalServerError(Exception exception) {
    log.error("{}", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Rocket.Chat error
   */
  public void logRocketChatError(String message) {
    log.error("{}{})", ROCKET_CHAT_ERROR_TEXT, message);
  }

  /**
   * Rocket.Chat error
   */
  public void logRocketChatError(String message, String error, String errorType) {
    log.error("{}{} (Error: {} / ErrorType: {})", ROCKET_CHAT_ERROR_TEXT, message, error,
        errorType);
  }

  /**
   * Rocket.Chat Error with exception
   */
  public void logRocketChatError(String message, Exception exception) {
    log.error("{}{}", ROCKET_CHAT_ERROR_TEXT, message);
    log.error("{}", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * IllegalArgumentException
   */
  public void logIllegalArgumentException(IllegalArgumentException exception) {
    log.warn("{}{}", ILLEGAL_ARGUMENT_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * NoSuchElementException
   */
  public void logNoSuchElementException(NoSuchElementException exception) {
    log.warn("{}{}", ILLEGAL_ARGUMENT_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Exception from AgencyServiceHelper
   */
  public void logAgencyServiceHelperException(Exception exception) {
    log.error("{}{}", AGENCY_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Exception from AgencyServiceHelper
   */
  public void logAgencyServiceHelperException(String message, Exception exception) {
    log.error("{}{}", AGENCY_ERROR_TEXT, message);
    log.error("{}{}", AGENCY_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Exception from MailServiceHelperException
   */
  public void logMailServiceHelperException(String message) {
    log.error("{}{}", MAIL_SERVICE_ERROR_TEXT, message);
  }

  /**
   * Exception from MailServiceHelperException
   */
  public void logMailServiceHelperException(String message, Exception exception) {
    log.error("{}{}", MAIL_SERVICE_ERROR_TEXT, message);
    log.error("{}{}", MAIL_SERVICE_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Error from EmailNotificationFacade
   */
  public void logEmailNotificationFacadeError(String message) {
    log.error("{}{}", EMAIL_NOTIFICATION_ERROR_TEXT, message);
  }

  /**
   * Error from AcceptEnquiryFacade
   */
  public void logAcceptEnquiryFacadeError(String message) {
    log.error("{}{}", ACCEPT_ENQUIRY_ERROR_TEXT, message);
  }

  /**
   * MessageService (helper) exception
   */
  public void logMessageServiceHelperException(String message, Exception exception) {
    log.error("{}{}", MESSAGESERVICE_HELPER_ERROR_TEXT, message);
    log.error("{}{}", MESSAGESERVICE_HELPER_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Warning from AssignSessionFacade
   */
  public void logAssignSessionFacadeWarning(Exception exception) {
    log.warn("{}{}", ASSIGN_SESSION_FACADE_WARNING_TEXT, exception.getMessage());
    log.warn("{}", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Error from AssignSessionFacade
   */
  public void logAssignSessionFacadeError(String message) {
    log.error("{}{}", ASSIGN_SESSION_FACADE_ERROR_TEXT, message);
  }

  /**
   * Error from AssignSessionFacade
   */
  public void logAssignSessionFacadeError(Exception exception) {
    log.error("{}{}", ASSIGN_SESSION_FACADE_ERROR_TEXT, exception.getMessage());
    log.error("{}", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Logs monitoring exception
   */
  public void logMonitoringHelperError(Exception exception) {
    log.error("{}{}", MONITORING_HELPER_ERROR_TEXT,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Logs an info message
   *
   * @param msg The message
   */
  public void logInfo(String msg) {
    log.info(msg);
  }

  /**
   * Logs a Encryption service error
   */
  public void logEncryptionServiceError(Exception exception) {
    log.error(RC_ENCRYPTION_SERVICE_ERROR + "{}",
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  public void logEncryptionPossibleBadKeyError(Exception exception) {
    log.error(RC_ENCRYPTION_BAD_KEY_SERVICE_ERROR + "{}",
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Error while decrypting a message
   */
  public void logDecryptionError(String message, Exception exception) {
    log.error("{}{}", DECRYPTION_ERROR, message);
    log.error("{}{}", DECRYPTION_ERROR,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Error while truncating a message
   */
  public void logTruncationError(String message, Exception exception) {
    log.error("{}{}", TRUNCATION_ERROR, message);
    log.error("{}{}", TRUNCATION_ERROR,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Error from DTO validation
   */
  public void logValidationError(String message) {
    log.error("{}{}", VALIDATION_ERROR, message);
  }


  /**
   * Logs the exception message from creating the enquiry message
   */
  public void logCreateEnquiryMessageException(Exception exception) {
    log.error("{}{}", CREATE_ENQUIRY_MESSAGE_ERROR,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }

  /**
   * Error while registering a new consulting type session.
   *
   * @param message Error message
   * @param exception Exception
   */
  public void logCreateSessionFacadeError(String message, Exception exception) {
    log.error("{}{}", CREATE_SESSION_FACADE_ERROR, message);
    log.error("{}{}", CREATE_SESSION_FACADE_ERROR,
        org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception));
  }
}

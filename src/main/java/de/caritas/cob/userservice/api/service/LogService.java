package de.caritas.cob.userservice.api.service;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for logging
 */
public class LogService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogService.class);

  public static final String ROCKET_CHAT_ERROR_TEXT = "Rocket.Chat Error: ";
  public static final String DB_ERROR_TEXT = "Database error: ";
  public static final String BAD_REQUEST_ERROR_TEXT = "Bad Request: ";
  public static final String UNAUTHORIZED_WARNING_TEXT = "Unauthorized: ";
  public static final String FORBIDDEN_WARNING_TEXT = "Forbidden: ";
  public static final String ASSIGN_SESSION_FACADE_WARNING_TEXT = "AssignSessionFacade warning: ";
  public static final String ASSIGN_SESSION_FACADE_ERROR_TEXT = "AssignSessionFacade error: ";
  public static final String CREATE_ENQUIRY_MESSAGE_ERROR = "CreateEnquiryMessageFacade error: ";

  private LogService() {
  }

  /**
   * Logs a database error.
   *
   * @param exception the exception
   */
  public static void logDatabaseError(Exception exception) {
    LOGGER.error("{}{}", DB_ERROR_TEXT, getStackTrace(exception));
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
   * @param exception the exception
   */
  public static void logInternalServerError(Exception exception) {
    LOGGER.error(
        "UserService Api: {}, {}, {}",
        getStackTrace(exception),
        exception.getMessage(),
        nonNull(exception.getCause()) ? getStackTrace(exception.getCause()) : "No Cause");
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
   * @param exception the exception
   */
  public static void logAssignSessionFacadeError(Exception exception) {
    LOGGER.error("{}{}", ASSIGN_SESSION_FACADE_ERROR_TEXT, exception.getMessage());
    LOGGER.error("{}", getStackTrace(exception));
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
   * Logs an info exception.
   *
   * @param exception the exception
   */
  public static void logInfo(Exception exception) {
    LOGGER.info(getStackTrace(exception));
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
   * Logs an warning message.
   *
   * @param exception The exception
   */
  public static void logWarn(Exception exception) {
    LOGGER.warn(getStackTrace(exception));
  }
}

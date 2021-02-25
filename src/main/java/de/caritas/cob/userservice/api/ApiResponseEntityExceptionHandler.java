package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.exception.NoMasterKeyException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.httpresponses.UnauthorizedException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.service.LogService;
import java.net.UnknownHostException;
import javax.validation.ConstraintViolationException;
import lombok.NoArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Customizes API error/exception handling to hide information and/or possible security
 * vulnerabilities.
 */
@NoArgsConstructor
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Custom BadRequest exception.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({BadRequestException.class})
  public ResponseEntity<Object> handleCustomBadRequest(final BadRequestException ex,
      final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Custom BadRequest exception with header reason.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({CustomValidationHttpStatusException.class})
  public ResponseEntity<Object> handleCustomBadRequest(final CustomValidationHttpStatusException ex,
      final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(ex, null, ex.getCustomHttpHeader(), ex.getHttpStatus(),
        request);
  }

  /**
   * Constraint violations.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleBadRequest(final RuntimeException ex,
      final WebRequest request) {
    LogService.logWarn(ex);

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Incoming request body could not be deserialized.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      final HttpMessageNotReadableException ex, final HttpHeaders headers, final HttpStatus status,
      final WebRequest request) {
    LogService.logWarn(status, ex);

    return handleExceptionInternal(null, null, headers, status, request);
  }

  /**
   * On object fails validation.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status,
      final WebRequest request) {
    LogService.logWarn(status, ex);

    return handleExceptionInternal(null, null, headers, status, request);
  }

  /**
   * 401 - Unauthorized.
   *
   * @param ex {@link UnauthorizedException}
   * @param request {@link WebRequest}
   * @return {@link HttpStatus#UNAUTHORIZED} without body or detailed information
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Object> handleUnauthorized(final UnauthorizedException ex,
      final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
  }

  /**
   * 409 - Conflict.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({InvalidDataAccessApiUsageException.class, DataAccessException.class})
  protected ResponseEntity<Object> handleConflict(final RuntimeException ex,
      final WebRequest request) {
    LogService.logWarn(HttpStatus.CONFLICT, ex);

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.CONFLICT, request);
  }

  /**
   * 409 - Conflict.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({ConflictException.class})
  protected ResponseEntity<Object> handleCustomConflict(final ConflictException ex,
      final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.CONFLICT, request);
  }

  /**
   * 403 - Forbidden.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({ForbiddenException.class})
  public ResponseEntity<Object> handleForbidden(final ForbiddenException ex,
      final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
  }

  /**
   * 404 - Not Found.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({NotFoundException.class})
  public ResponseEntity<Object> handleForbidden(final NotFoundException ex,
      final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  /**
   * 500 - Internal Server Error.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class,
      IllegalStateException.class, KeycloakException.class,
      UnknownHostException.class, CustomCryptoException.class, NoMasterKeyException.class})
  public ResponseEntity<Object> handleInternal(final RuntimeException ex,
      final WebRequest request) {
    LogService.logInternalServerError(ex);

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
        request);
  }

  /**
   * 500 - Custom Internal Server Error.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({InternalServerErrorException.class})
  public ResponseEntity<Object> handleInternal(final InternalServerErrorException ex,
      final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
        request);
  }

  /**
   * 204 - No Content.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({NoContentException.class})
  public ResponseEntity<Object> handleInternal(final NoContentException ex,
      final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.NO_CONTENT,
        request);
  }

}

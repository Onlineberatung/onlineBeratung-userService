package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.exception.httpresponses.customheader.CustomHttpHeader;
import de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/** Custom validation exception for http status with reason. */
@Getter
public class CustomValidationHttpStatusException extends CustomHttpStatusException {

  private final HttpHeaders customHttpHeaders;
  private final HttpStatus httpStatus;

  /**
   * Creates a {@link CustomValidationHttpStatusException} with default {@link HttpStatus} bad
   * request.
   *
   * @param httpStatusExceptionReason the reason for the exception
   */
  public CustomValidationHttpStatusException(HttpStatusExceptionReason httpStatusExceptionReason) {
    super();
    this.customHttpHeaders = new CustomHttpHeader(httpStatusExceptionReason).buildHeader();
    this.httpStatus = HttpStatus.BAD_REQUEST;
  }

  /**
   * Creates a {@link CustomValidationHttpStatusException} with custom {@link HttpStatus} to be
   * returned.
   *
   * @param reason the reason for the exception
   * @param httpStatus the {@link HttpStatus} to be returned
   */
  public CustomValidationHttpStatusException(
      HttpStatusExceptionReason reason, HttpStatus httpStatus) {
    super();
    this.customHttpHeaders = new CustomHttpHeader(reason).buildHeader();
    this.httpStatus = httpStatus;
  }
}

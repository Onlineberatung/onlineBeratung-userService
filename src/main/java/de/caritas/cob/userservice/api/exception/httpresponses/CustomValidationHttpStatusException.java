package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.exception.httpresponses.customheader.CustomHttpHeader;
import de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason;
import org.springframework.http.HttpHeaders;

/**
 * Custom validation exception for http status with reason.
 */
public class CustomValidationHttpStatusException extends CustomHttpStatusException {

  private final HttpHeaders customHttpHeader;

  public CustomValidationHttpStatusException(HttpStatusExceptionReason httpStatusExceptionReason) {
    super();
    this.customHttpHeader = new CustomHttpHeader(httpStatusExceptionReason)
        .buildHeader();
  }

  /**
   * Get the {@link @HttpHeaders} for the thrown exception.
   *
   * @return a value of {@link @HttpHeaders}
   */
  public HttpHeaders getCustomHttpHeader() {
    return this.customHttpHeader;
  }

}

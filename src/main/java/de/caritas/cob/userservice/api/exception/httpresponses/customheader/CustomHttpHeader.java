package de.caritas.cob.userservice.api.exception.httpresponses.customheader;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

/*
* Custom http header with X-Reason header.
 */
@RequiredArgsConstructor
@Getter
public class CustomHttpHeader {

  private final @NonNull HttpStatusExceptionReason httpStatusExceptionReason;

  /**
   * Build the header object.
   *
   * @return an instance of {@link HttpHeaders}
   */
  public HttpHeaders buildHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Reason", this.httpStatusExceptionReason.name());
    return headers;
  }
}

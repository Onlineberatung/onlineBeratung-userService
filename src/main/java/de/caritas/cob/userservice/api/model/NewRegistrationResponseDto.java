package de.caritas.cob.userservice.api.model;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.caritas.cob.userservice.api.repository.session.Session;
import lombok.Builder;
import lombok.Getter;

/**
 * Builder for {@link CreateSessionResponse} information containing the {@link HttpStatus} response
 * code and {@link Session#getId()} of the newly created session.
 *
 */
@Getter
@Builder
public class NewRegistrationResponseDto {

  private Long sessionId;
  @JsonIgnore
  private HttpStatus status;
}

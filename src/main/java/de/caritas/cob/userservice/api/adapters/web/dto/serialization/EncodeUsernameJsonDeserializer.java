package de.caritas.cob.userservice.api.adapters.web.dto.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EncodeUsernameJsonDeserializer extends JsonDeserializer<String> {

  @Value("${user.username.invalid.length}")
  private String errorUsernameInvalidLength;

  private final UserHelper userHelper;

  @Override
  public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    String username = new UsernameTranscoder().encodeUsername(jsonParser.getValueAsString());

    // Check if username is of valid length
    if (!userHelper.isUsernameValid(username)) {
      throw new BadRequestException(errorUsernameInvalidLength);
    }

    return username;
  }
}

package de.caritas.cob.userservice.api.model.jsondeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncodeUsernameJsonDeserializer extends JsonDeserializer<String> {

  @Value("${user.username.invalid.length}")
  private String ERROR_USERNAME_INVALID_LENGTH;

  private UserHelper userHelper = new UserHelper();

  @Override
  public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    String username = userHelper.encodeUsername(jsonParser.getValueAsString());

    // Check if username is of valid length
    if (!userHelper.isUsernameValid(username)) {
      throw new BadRequestException(ERROR_USERNAME_INVALID_LENGTH);
    }

    return username;
  }

}

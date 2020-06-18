package de.caritas.cob.UserService.api.model.jsonSerializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.caritas.cob.UserService.api.helper.UserHelper;

public class DecodeUsernameJsonSerializer extends JsonSerializer<String> {

  private UserHelper userHelper = new UserHelper();

  @Override
  public void serialize(String username, JsonGenerator jsonGenerator,
      SerializerProvider serializers) throws IOException {
    jsonGenerator.writeObject(userHelper.decodeUsername(username));
  }

}

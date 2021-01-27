package de.caritas.cob.userservice.api.model.jsonserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.caritas.cob.userservice.api.helper.UserHelper;
import java.io.IOException;

public class DecodeUsernameJsonSerializer extends JsonSerializer<String> {

  private final UserHelper userHelper = new UserHelper();

  @Override
  public void serialize(String username, JsonGenerator jsonGenerator,
      SerializerProvider serializers) throws IOException {
    jsonGenerator.writeObject(userHelper.decodeUsername(username));
  }

}

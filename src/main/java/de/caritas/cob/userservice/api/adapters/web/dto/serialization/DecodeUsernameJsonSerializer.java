package de.caritas.cob.userservice.api.adapters.web.dto.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import java.io.IOException;

public class DecodeUsernameJsonSerializer extends JsonSerializer<String> {

  @Override
  public void serialize(
      String username, JsonGenerator jsonGenerator, SerializerProvider serializers)
      throws IOException {
    jsonGenerator.writeObject(new UsernameTranscoder().decodeUsername(username));
  }
}

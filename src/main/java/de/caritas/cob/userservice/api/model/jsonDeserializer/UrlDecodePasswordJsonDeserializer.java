package de.caritas.cob.userservice.api.model.jsonDeserializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.caritas.cob.userservice.api.helper.Helper;

public class UrlDecodePasswordJsonDeserializer extends JsonDeserializer<String> {

  private Helper helper = new Helper();

  @Override
  public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JsonProcessingException {
    String password = jsonParser.getValueAsString();
    return helper.urlDecodeString(password);
  }

}

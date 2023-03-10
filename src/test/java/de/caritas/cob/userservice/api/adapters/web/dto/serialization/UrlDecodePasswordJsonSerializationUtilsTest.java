package de.caritas.cob.userservice.api.adapters.web.dto.serialization;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.PASSWORD;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.PASSWORD_URL_ENCODED;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UrlDecodePasswordJsonSerializationUtilsTest {

  private ObjectMapper objectMapper;
  private UrlDecodePasswordJsonDeserializer urlDecodePasswordJsonDeserializer;

  @Before
  public void setup() {
    objectMapper = new ObjectMapper();
    urlDecodePasswordJsonDeserializer = new UrlDecodePasswordJsonDeserializer();
  }

  @Test
  public void deserialize_Schould_EncodePassword() throws JsonParseException, IOException {
    String json = "{\"password:\":\"" + PASSWORD_URL_ENCODED + "\"}";
    String result = deserializePassword(json);
    assertEquals(PASSWORD, result);
  }

  private String deserializePassword(String json) throws JsonParseException, IOException {
    InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    JsonParser jsonParser = objectMapper.getFactory().createParser(stream);
    jsonParser.nextToken();
    jsonParser.nextToken();
    jsonParser.nextToken();
    DeserializationContext deserializationContext = objectMapper.getDeserializationContext();
    return urlDecodePasswordJsonDeserializer.deserialize(jsonParser, deserializationContext);
  }
}

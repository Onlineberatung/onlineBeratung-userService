package de.caritas.cob.UserService.api.model.jsonDeserializer;

import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_TOO_LONG;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_TOO_SHORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class EncodeUsernameJsonDeserializerTest {

  private ObjectMapper objectMapper;
  private EncodeUsernameJsonDeserializer encodeUsernameJsonDeserializer;

  @Before
  public void setup() {
    objectMapper = new ObjectMapper();
    encodeUsernameJsonDeserializer = new EncodeUsernameJsonDeserializer();
  }

  @Test
  public void deserialize_Schould_EncodeDecodedUsername() throws JsonParseException, IOException {
    String json = "{\"username:\":\"" + USERNAME_DECODED + "\"}";
    String result = deserializeUsername(json);
    assertEquals(USERNAME_ENCODED, result);
  }

  @Test
  public void deserialize_SchouldNot_ReencodeEncodedUsername()
      throws JsonParseException, IOException {
    String json = "{\"username:\":\"" + USERNAME_ENCODED + "\"}";
    String result = deserializeUsername(json);
    assertEquals(USERNAME_ENCODED, result);
  }

  @Test
  public void deserialize_Should_ThrowBadRequestException_WhenUsernameIsTooShort()
      throws JsonParseException, IOException {

    try {
      String json = "{\"username:\":\"" + USERNAME_TOO_SHORT + "\"}";
      deserializeUsername(json);

      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  @Test
  public void deserialize_Should_ThrowBadRequestException_WhenUsernameIsTooLong()
      throws JsonParseException, IOException {

    try {
      String json = "{\"username:\":\"" + USERNAME_TOO_LONG + "\"}";
      deserializeUsername(json);

      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  private String deserializeUsername(String json) throws JsonParseException, IOException {
    InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    JsonParser jsonParser = objectMapper.getFactory().createParser(stream);
    jsonParser.nextToken();
    jsonParser.nextToken();
    jsonParser.nextToken();
    DeserializationContext deserializationContext = objectMapper.getDeserializationContext();
    return encodeUsernameJsonDeserializer.deserialize(jsonParser, deserializationContext);
  }

}

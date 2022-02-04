package de.caritas.cob.userservice.api.model.jsondeserializer;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_TOO_LONG;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_TOO_SHORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

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
  public void deserialize_Should_EncodeDecodedUsername() throws IOException {
    String json = "{\"username:\":\"" + USERNAME_DECODED + "\"}";
    String result = deserializeUsername(json);
    assertEquals(USERNAME_ENCODED, result);
  }

  @Test
  public void deserialize_ShouldNot_ReencodeEncodedUsername()
      throws IOException {
    String json = "{\"username:\":\"" + USERNAME_ENCODED + "\"}";
    String result = deserializeUsername(json);
    assertEquals(USERNAME_ENCODED, result);
  }

  @Test
  public void deserialize_Should_ThrowBadRequestException_WhenUsernameIsTooShort()
      throws IOException {

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
      throws IOException {

    try {
      String json = "{\"username:\":\"" + USERNAME_TOO_LONG + "\"}";
      deserializeUsername(json);

      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  private String deserializeUsername(String json) throws IOException {
    InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    JsonParser jsonParser = objectMapper.getFactory().createParser(stream);
    jsonParser.nextToken();
    jsonParser.nextToken();
    jsonParser.nextToken();
    DeserializationContext deserializationContext = objectMapper.getDeserializationContext();
    return encodeUsernameJsonDeserializer.deserialize(jsonParser, deserializationContext);
  }

}

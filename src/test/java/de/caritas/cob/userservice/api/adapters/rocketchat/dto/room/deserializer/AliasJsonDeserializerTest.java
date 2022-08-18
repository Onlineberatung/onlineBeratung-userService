package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.deserializer;

import static de.caritas.cob.userservice.api.adapters.web.dto.VideoCallMessageDTO.EventTypeEnum.IGNORED_CALL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ForwardMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.VideoCallMessageDTO;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ClassUtils;

@RunWith(MockitoJUnitRunner.class)
public class AliasJsonDeserializerTest {

  private static final String DECODED_USERNAME = "username";
  private static final String ENCODE_USERNAME =
      new UsernameTranscoder().encodeUsername(DECODED_USERNAME);
  private static final String MESSAGE_FORWARD_ALIAS_JSON_WITH_ENCODED_USERNAME =
      "{\"alias\":\"%7B%22timestamp%22%3A%221568128850636%22%2C%22username%22%3A%22"
          + ENCODE_USERNAME
          + "%22%2C%22rcUserId%22%3A%22p5NdZSxc2Kh7GfXdB%22%7D\"";
  private static final String MESSAGE_FORWARD_ALIAS_JSON_WITH_DECODED_USERNAME =
      "{\"alias\":\"%7B%22timestamp%22%3A%221568128850636%22%2C%22username%22%3A%22"
          + DECODED_USERNAME
          + "%22%2C%22rcUserId%22%3A%22p5NdZSxc2Kh7GfXdB%22%7D\"";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AliasJsonDeserializer aliasJsonDeserializer = new AliasJsonDeserializer();

  @Test
  public void aliasJsonDeserializer_Should_haveNoArgsConstructor() {
    assertTrue(ClassUtils.hasConstructor(AliasJsonDeserializer.class));
  }

  @Test
  public void deserialize_Should_convertAliasWithEncodedUsernameToForwardMessageDTO()
      throws IOException {
    ForwardMessageDTO result =
        deserializeOldAliasJson(MESSAGE_FORWARD_ALIAS_JSON_WITH_ENCODED_USERNAME)
            .getForwardMessageDTO();
    assertThat(result.getUsername(), is(DECODED_USERNAME));
  }

  @Test
  public void deserialize_Should_convertAliasWithDecodedUsernameToForwardMessageDTO()
      throws IOException {
    ForwardMessageDTO result =
        deserializeOldAliasJson(MESSAGE_FORWARD_ALIAS_JSON_WITH_DECODED_USERNAME)
            .getForwardMessageDTO();
    assertThat(result.getUsername(), is(DECODED_USERNAME));
  }

  @Test
  public void deserialize_Should_ReturnNull_IfAliasIsEmpty() throws IOException {
    AliasMessageDTO result = deserializeOldAliasJson("");
    assertNull(result);
  }

  @Test
  public void deserialize_Should_returnAliasDTOWithDecodedUsername_When_usernameIsEncoded()
      throws Exception {
    String aliasMessageDTO =
        asJsonString(
            new AliasMessageDTO()
                .videoCallMessageDTO(
                    new VideoCallMessageDTO()
                        .eventType(IGNORED_CALL)
                        .initiatorUserName(ENCODE_USERNAME)
                        .initiatorRcUserId("rcUserId")));

    AliasMessageDTO result = deserializeNewAliasJson(aliasMessageDTO);

    assertThat(result.getForwardMessageDTO(), nullValue());
    assertThat(result.getVideoCallMessageDTO(), notNullValue());
    assertThat(result.getVideoCallMessageDTO().getEventType(), is(IGNORED_CALL));
    assertThat(result.getVideoCallMessageDTO().getInitiatorUserName(), is(DECODED_USERNAME));
  }

  @Test
  public void deserialize_Should_returnAliasDTOWithDecodedUsername_When_usernameIsDecoded()
      throws Exception {
    String aliasMessageDTO =
        asJsonString(
            new AliasMessageDTO()
                .videoCallMessageDTO(
                    new VideoCallMessageDTO()
                        .eventType(IGNORED_CALL)
                        .initiatorUserName(DECODED_USERNAME)
                        .initiatorRcUserId("rcUserId")));

    AliasMessageDTO result = deserializeNewAliasJson(aliasMessageDTO);

    assertThat(result.getForwardMessageDTO(), nullValue());
    assertThat(result.getVideoCallMessageDTO(), notNullValue());
    assertThat(result.getVideoCallMessageDTO().getEventType(), is(IGNORED_CALL));
    assertThat(result.getVideoCallMessageDTO().getInitiatorUserName(), is(DECODED_USERNAME));
  }

  @Test
  public void deserialize_Should_returnAliasDTOWithDecodedUsernames_When_usernamesAreEncoded()
      throws Exception {
    String aliasMessageDTO =
        asJsonString(
            new AliasMessageDTO()
                .forwardMessageDTO(
                    new ForwardMessageDTO()
                        .message("message")
                        .rcUserId("rcUserId")
                        .timestamp("timestamp")
                        .username(ENCODE_USERNAME))
                .videoCallMessageDTO(
                    new VideoCallMessageDTO()
                        .eventType(IGNORED_CALL)
                        .initiatorUserName(ENCODE_USERNAME)
                        .initiatorRcUserId("rcUserId")));

    AliasMessageDTO result = deserializeNewAliasJson(aliasMessageDTO);

    assertThat(result.getForwardMessageDTO(), notNullValue());
    assertThat(result.getForwardMessageDTO().getMessage(), is("message"));
    assertThat(result.getForwardMessageDTO().getRcUserId(), is("rcUserId"));
    assertThat(result.getForwardMessageDTO().getTimestamp(), is("timestamp"));
    assertThat(result.getForwardMessageDTO().getUsername(), is(DECODED_USERNAME));
    assertThat(result.getVideoCallMessageDTO(), notNullValue());
    assertThat(result.getVideoCallMessageDTO().getEventType(), is(IGNORED_CALL));
    assertThat(result.getVideoCallMessageDTO().getInitiatorUserName(), is(DECODED_USERNAME));
  }

  private AliasMessageDTO deserializeOldAliasJson(String json) throws IOException {
    InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    JsonParser jsonParser = objectMapper.getFactory().createParser(stream);
    jsonParser.nextToken();
    jsonParser.nextToken();
    jsonParser.nextToken();
    DeserializationContext deserializationContext = objectMapper.getDeserializationContext();
    return aliasJsonDeserializer.deserialize(jsonParser, deserializationContext);
  }

  private AliasMessageDTO deserializeNewAliasJson(String json) throws IOException {
    JsonParser jsonParser = mock(JsonParser.class);
    when(jsonParser.getValueAsString()).thenReturn(json);
    return aliasJsonDeserializer.deserialize(jsonParser, null);
  }

  private String asJsonString(AliasMessageDTO aliasMessageDTO) throws JsonProcessingException {
    return objectMapper.writeValueAsString(aliasMessageDTO);
  }
}

package de.caritas.cob.userservice.api.adapters.rocketchat.dto.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceDTO.PresenceStatus;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

class PresenceDTOTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final EasyRandom easyRandom = new EasyRandom();

  private PresenceDTO underTest;

  @Test
  void presenceDTOShouldMarshallCorrectly() throws JsonProcessingException {
    underTest = easyRandom.nextObject(PresenceDTO.class);

    var marshalledPresenceDto = objectMapper.writeValueAsString(underTest);

    var lowerCasePresence = underTest.getPresence().name().toLowerCase();
    assertTrue(marshalledPresenceDto.contains("\"presence\":\"" + lowerCasePresence + "\""));
    assertTrue(marshalledPresenceDto.contains("\"success\":" + underTest.getSuccess() + ""));
  }

  @Test
  void presenceDTOShouldDeMarshallStatusBusy() throws JsonProcessingException {
    var presenceDTOString = "{ \"presence\": \"busy\" }";

    underTest = objectMapper.readValue(presenceDTOString, PresenceDTO.class);

    assertEquals(PresenceStatus.BUSY, underTest.getPresence());
  }

  @Test
  void presenceDTOShouldDeMarshallStatusOnline() throws JsonProcessingException {
    var presenceDTOString = "{ \"presence\": \"online\" }";

    underTest = objectMapper.readValue(presenceDTOString, PresenceDTO.class);

    assertEquals(PresenceStatus.ONLINE, underTest.getPresence());
  }

  @Test
  void presenceDTOShouldDeMarshallStatusOffline() throws JsonProcessingException {
    var presenceDTOString = "{ \"presence\": \"offline\" }";

    underTest = objectMapper.readValue(presenceDTOString, PresenceDTO.class);

    assertEquals(PresenceStatus.OFFLINE, underTest.getPresence());
  }

  @Test
  void presenceDTOShouldDeMarshallStatusAway() throws JsonProcessingException {
    var presenceDTOString = "{ \"presence\": \"away\" }";

    underTest = objectMapper.readValue(presenceDTOString, PresenceDTO.class);

    assertEquals(PresenceStatus.AWAY, underTest.getPresence());
  }
}

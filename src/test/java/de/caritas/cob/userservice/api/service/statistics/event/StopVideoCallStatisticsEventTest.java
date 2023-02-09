package de.caritas.cob.userservice.api.service.statistics.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StopVideoCallStatisticsEventTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private StopVideoCallStatisticsEvent underTest;
  private UUID userId;
  private UUID videoCallId;

  @BeforeEach
  void setup() {
    userId = null;
    videoCallId = null;
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void constructorShouldThrowExceptionIfUserIdNull() {
    givenAVideoCallId();

    var thrown =
        assertThrows(
            NullPointerException.class, () -> new StopVideoCallStatisticsEvent(null, videoCallId));

    assertTrue(thrown.getMessage().contentEquals("userId is marked non-null but is null"));
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void constructorShouldThrowExceptionIfVideoCallIdNull() {
    givenAUserId();
    var userIdStr = userId.toString();
    assert userIdStr != null;

    var thrown =
        assertThrows(
            NullPointerException.class, () -> new StopVideoCallStatisticsEvent(userIdStr, null));

    assertTrue(thrown.getMessage().contentEquals("videoCallId is marked non-null but is null"));
  }

  @Test
  void getEventTypeShouldReturnStopVideoCall() {
    givenAUserId();
    givenAVideoCallId();

    underTest = new StopVideoCallStatisticsEvent(userId.toString(), videoCallId);

    assertEquals(EventType.STOP_VIDEO_CALL, underTest.getEventType());
  }

  @Test
  void getPayloadShouldReturnValidJsonPayload() throws JsonProcessingException {
    givenAUserId();
    givenAVideoCallId();

    underTest = new StopVideoCallStatisticsEvent(userId.toString(), videoCallId);

    var payload = underTest.getPayload().orElseThrow();

    assertNotNull(payload);
    var payloadMap = objectMapper.readValue(payload, Map.class);
    assertEquals(videoCallId.toString(), payloadMap.get("videoCallUuid"));
    assertEquals(EventType.STOP_VIDEO_CALL.toString(), payloadMap.get("eventType"));
    assertEquals(userId.toString(), payloadMap.get("userId"));
    assertEquals(UserRole.CONSULTANT.toString(), payloadMap.get("userRole"));
    assertTimestampIsNearlyNow(payloadMap);
  }

  private void givenAUserId() {
    userId = UUID.randomUUID();
  }

  private void givenAVideoCallId() {
    videoCallId = UUID.randomUUID();
  }

  @SuppressWarnings("rawtypes")
  private void assertTimestampIsNearlyNow(Map payloadMap) {
    var payloadTimestamp = (String) payloadMap.get("timestamp");
    assertNotNull(payloadTimestamp);

    var payloadInstant = Instant.parse(payloadTimestamp);
    assertTrue(Duration.between(payloadInstant, Instant.now()).abs().getSeconds() < 2);
  }
}

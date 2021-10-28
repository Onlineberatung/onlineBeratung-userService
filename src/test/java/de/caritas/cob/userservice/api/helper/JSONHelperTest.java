package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.offsetdatetime.CustomOffsetDateTime;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.AssignSessionStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;

public class JSONHelperTest {

  @Test
  public void serialize_Should_returnOptionalWithSerializedObject() {

    OffsetDateTime offsetDateTime = CustomOffsetDateTime.nowInUtc();

    AssignSessionStatisticsEventMessage assignSessionStatisticsEventMessage =
        new AssignSessionStatisticsEventMessage()
            .eventType(EventType.ASSIGN_SESSION)
            .sessionId(SESSION_ID)
            .userId(CONSULTANT_ID)
            .userRole(UserRole.CONSULTANT)
            .timestamp(offsetDateTime);

    Optional<String> result =
        JSONHelper.serializeWithOffsetDateTimeAsString(assignSessionStatisticsEventMessage,
            LogService::logInternalServerError);

    assertThat(result.isPresent(), is(true));

    String expectedJson =
        "{"
            + "  \"userId\":\""
            + CONSULTANT_ID
            + "\","
            + "  \"userRole\":\""
            + UserRole.CONSULTANT
            + "\","
            + "  \"sessionId\":"
            + SESSION_ID
            + ","
            + "  \"timestamp\":\""
            + offsetDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
            + "\","
            + "  \"eventType\":\""
            + EventType.ASSIGN_SESSION
            + "\""
            + "}";

    assertThat(result.get(), jsonEquals(expectedJson));

  }

  @Test
  public void serialize_Should_returnOptionalEmpty_When_jsonStringCanNotBeConverted()
      throws JsonProcessingException {

    ObjectMapper om = Mockito.spy(new ObjectMapper());
    Mockito.when(om.writeValueAsString(Object.class)).thenThrow(new JsonProcessingException("") {});

    Optional<String> result =
        JSONHelper.serializeWithOffsetDateTimeAsString(new Object(),
            LogService::logInternalServerError);

    assertThat(result.isPresent(), is(false));
  }

}

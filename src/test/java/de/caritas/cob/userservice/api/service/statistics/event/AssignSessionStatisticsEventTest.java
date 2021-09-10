package de.caritas.cob.userservice.api.service.statistics.event;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import java.util.Objects;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AssignSessionStatisticsEventTest {


  private final String TIMESTAMP_FIELD_NAME = "TIMESTAMP";
  private AssignSessionStatisticsEvent assignSessionStatisticsEvent;
  private String staticTimestamp;

  @Before
  public void setup() throws NoSuchFieldException, IllegalAccessException {
    assignSessionStatisticsEvent = new AssignSessionStatisticsEvent(CONSULTANT_ID, SESSION_ID);
    staticTimestamp = Objects.requireNonNull(ReflectionTestUtils
            .getField(assignSessionStatisticsEvent,
                AssignSessionStatisticsEvent.class,
                TIMESTAMP_FIELD_NAME))
        .toString();
  }

  @Test
  public void getEventType_Should_ReturnEventTypeCreateMessage() {

    assertThat(assignSessionStatisticsEvent.getEventType(),
        is(EventType.ASSIGN_SESSION));
  }

  @Test
  public void getPayload_Should_ReturnValidJsonPayload() {

    String expectedJson = "{"
        + "  \"consultantId\":\"" + CONSULTANT_ID + "\","
        + "  \"sessionId\":" + SESSION_ID + ","
        + "  \"timestamp\":\"" + staticTimestamp + "\","
        + "  \"eventType\":\"" + EventType.ASSIGN_SESSION + "\""
        + "}";

    Optional<String> result =  assignSessionStatisticsEvent.getPayload();

    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), jsonEquals(expectedJson));
  }

}

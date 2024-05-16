package de.caritas.cob.userservice.api.service.statistics;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.api.testConfig.RabbitMqTestConfig;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.profiles.active=testing")
@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class StatisticsServiceIT {

  private static final long MAX_TIMEOUT_MILLIS = 5000;

  @Autowired StatisticsService statisticsService;
  @Autowired AmqpTemplate amqpTemplate;

  @Test
  public void fireEvent_Should_Send_ExpectedAssignSessionStatisticsEventMessageToQueue()
      throws IOException {

    AssignSessionStatisticsEvent assignSessionStatisticsEvent =
        new AssignSessionStatisticsEvent(CONSULTANT_ID, UserRole.CONSULTANT, SESSION_ID);

    statisticsService.fireEvent(assignSessionStatisticsEvent);
    Message message =
        amqpTemplate.receive(RabbitMqTestConfig.QUEUE_NAME_ASSIGN_SESSION, MAX_TIMEOUT_MILLIS);
    assert message != null;

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
            + OffsetDateTime.now(ZoneOffset.UTC)
            + "\","
            + "  \"eventType\":\""
            + EventType.ASSIGN_SESSION
            + "\""
            + "}";

    assertThat(
        extractBodyFromAmpQMessage(message),
        jsonEquals(expectedJson)
            .whenIgnoringPaths("timestamp", "requestReferer", "requestUri", "requestUserId"));
  }

  private String extractBodyFromAmpQMessage(Message message) throws IOException {
    return IOUtils.toString(message.getBody(), UTF_8);
  }
}

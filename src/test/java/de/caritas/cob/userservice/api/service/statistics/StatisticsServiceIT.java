package de.caritas.cob.userservice.api.service.statistics;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.helper.Serializer;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.AssignSessionStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.testConfig.RabbitMqTestConfig;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class StatisticsServiceIT {

  private static final long MAX_TIMEOUT_MILLIS = 5000;

  @Autowired
  StatisticsService statisticsService;
  @Autowired
  AmqpTemplate amqpTemplate;

  @Test
  public void fireEvent_Should_Send_ExpectedAssignSessionStatisticsEventMessageToQueue()
      throws IOException {

    AssignSessionStatisticsEvent assignSessionStatisticsEvent =
        new AssignSessionStatisticsEvent(CONSULTANT_ID, SESSION_ID);
    AssignSessionStatisticsEventMessage assignSessionStatisticsEventMessage =
        new AssignSessionStatisticsEventMessage()
            .eventType(EventType.ASSIGN_SESSION)
            .consultantId(CONSULTANT_ID)
            .sessionId(SESSION_ID)
            .timestamp(assignSessionStatisticsEvent.getTimestamp());

    statisticsService.fireEvent(assignSessionStatisticsEvent);
    Message message =
        amqpTemplate.receive(RabbitMqTestConfig.QUEUE_NAME_ASSIGN_SESSION, MAX_TIMEOUT_MILLIS);
    assert message != null;
    assertThat(
        extractBodyFromAmpQMessage(message),
        jsonEquals(serializeEventMessage(assignSessionStatisticsEventMessage)));
  }

  private String serializeEventMessage(
      AssignSessionStatisticsEventMessage assignSessionStatisticsEventMessage) {
    return Serializer.serialize(
            assignSessionStatisticsEventMessage, LogService::logStatisticsEventError)
        .orElseThrow();
  }

  private String extractBodyFromAmpQMessage(Message message) throws IOException {
    return IOUtils.toString(message.getBody(), UTF_8);
  }
}

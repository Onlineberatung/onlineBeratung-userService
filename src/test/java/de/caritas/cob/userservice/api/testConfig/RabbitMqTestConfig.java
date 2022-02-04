package de.caritas.cob.userservice.api.testConfig;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMqTestConfig {

  public static final String STATISTICS_EXCHANGE_NAME = "statistics.topic";
  private static final String QUEUE_PREFIX = "statistics.";
  public static final String QUEUE_NAME_ASSIGN_SESSION = QUEUE_PREFIX + EventType.ASSIGN_SESSION;

  @Bean ConnectionFactory connectionFactory() {
    return new CachingConnectionFactory(new MockConnectionFactory());
  }

  @Bean
  public Declarables topicBindings() {
    Queue assignSessionStatisticEventQueue = new Queue(QUEUE_NAME_ASSIGN_SESSION, true);

    TopicExchange topicExchange = new TopicExchange(STATISTICS_EXCHANGE_NAME, true, false);

    return new Declarables(
        assignSessionStatisticEventQueue,
        topicExchange,
        BindingBuilder
            .bind(assignSessionStatisticEventQueue)
            .to(topicExchange).with(
                de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType.ASSIGN_SESSION));
  }
}

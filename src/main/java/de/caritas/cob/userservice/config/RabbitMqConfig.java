package de.caritas.cob.userservice.config;

import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for statistics.
 */
@Configuration
public class RabbitMqConfig {

  @Value("${spring.rabbitmq.host}")
  private String rabbitHost;

  public static final String STATISTICS_EXCHANGE_NAME = "statistics.topic";
  private static final String QUEUE_PREFIX = "statistics.";
  public static final String QUEUE_NAME_ASSIGN_SESSION = QUEUE_PREFIX + EventType.ASSIGN_SESSION;

  @Bean
  public Declarables topicBindings() {
    Queue assignSessionStatisticEventQueue = new Queue(QUEUE_NAME_ASSIGN_SESSION, true);

    TopicExchange topicExchange = new TopicExchange(STATISTICS_EXCHANGE_NAME, true, false);

    return new Declarables(
        assignSessionStatisticEventQueue,
        topicExchange,
        BindingBuilder
            .bind(assignSessionStatisticEventQueue)
            .to(topicExchange).with(EventType.ASSIGN_SESSION));
  }
}

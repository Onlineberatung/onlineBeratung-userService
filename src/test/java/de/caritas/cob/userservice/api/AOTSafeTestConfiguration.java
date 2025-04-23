package de.caritas.cob.userservice.api;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AOTSafeTestConfiguration {

  @Bean
  public Clock clock() {
    var today = LocalDateTime.of(2022, 2, 15, 13, 37).toInstant(ZoneOffset.UTC);
    return Clock.fixed(today, ZoneId.of("UTC"));
  }

  @Bean
  public MongoClient mongoClient() {
    return MongoClients.create("mongodb://localhost:27017");
  }
}

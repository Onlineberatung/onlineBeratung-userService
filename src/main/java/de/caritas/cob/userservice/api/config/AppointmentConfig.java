package de.caritas.cob.userservice.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "appointments")
public class AppointmentConfig {

  private int lifespanInHours;
  private Boolean deleteJobEnabled;

}

package de.caritas.cob.userservice.api.config;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "appointments")
public class AppointmentConfig {

  @NotNull @Positive private Integer lifespanInHours;
  @NotNull private Boolean deleteJobEnabled;
  @NotBlank private String deleteJobCron;
}

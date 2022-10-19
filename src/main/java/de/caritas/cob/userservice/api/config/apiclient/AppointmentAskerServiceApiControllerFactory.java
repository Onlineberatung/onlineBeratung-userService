package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.appointmentservice.generated.ApiClient;
import de.caritas.cob.userservice.appointmentservice.generated.web.AskerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AppointmentAskerServiceApiControllerFactory {

  @Value("${appointment.service.api.url}")
  private String appointmentServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public AskerApi createControllerApi() {
    var apiClient = new ApiClient(restTemplate).setBasePath(this.appointmentServiceApiUrl);
    return new AskerApi(apiClient);
  }
}

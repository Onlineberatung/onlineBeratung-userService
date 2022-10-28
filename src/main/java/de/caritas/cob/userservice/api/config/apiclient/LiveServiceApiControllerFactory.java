package de.caritas.cob.userservice.api.config.apiclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.liveservice.generated.ApiClient;
import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
import java.net.http.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LiveServiceApiControllerFactory {

  @Value("${live.service.api.url}")
  private String liveServiceApiUrl;

  private final ObjectMapper objectMapper;

  public LiveControllerApi createControllerApi() {
    var apiClient = new ApiClient(HttpClient.newBuilder(), objectMapper, liveServiceApiUrl);

    return new LiveControllerApi(apiClient);
  }
}

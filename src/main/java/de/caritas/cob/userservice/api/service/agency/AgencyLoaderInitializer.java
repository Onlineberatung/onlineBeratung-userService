package de.caritas.cob.userservice.api.service.agency;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Initializer class to provide static access to agency service.
 */
@Component
@RequiredArgsConstructor
public class AgencyLoaderInitializer {

  private final @NonNull AgencyService agencyService;

  /**
   * Initializes the {@link AgencyLoader}.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initializeAgencyLoader() {
    AgencyLoader.setAgencyService(this.agencyService);
  }

}

package de.caritas.cob.userservice.api.service.consultingtype;

import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReleaseToggleService {

  private final @NonNull ApplicationSettingsService applicationSettingsService;

  public boolean isToggleEnabled(ReleaseToggle toggle) {
    Map<String, Object> releaseToggles =
        applicationSettingsService.getApplicationSettings().getReleaseToggles();

    String toggleKeyName = toggle.getValue();
    if (releaseToggles != null && releaseToggles.containsKey(toggleKeyName)) {
      return nullAsFalse(Boolean.parseBoolean((String) releaseToggles.get(toggleKeyName)));
    } else {
      log.debug("Release toggle not found: {}", toggle);
      return false;
    }
  }

  private boolean nullAsFalse(Boolean topicsInRegistrationEnabled) {
    return Boolean.TRUE.equals(topicsInRegistrationEnabled);
  }
}

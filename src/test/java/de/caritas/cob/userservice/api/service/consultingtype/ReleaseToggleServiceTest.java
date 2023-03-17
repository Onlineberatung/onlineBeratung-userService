package de.caritas.cob.userservice.api.service.consultingtype;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import de.caritas.cob.userservice.applicationsettingsservice.generated.web.model.ApplicationSettingsDTO;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReleaseToggleServiceTest {

  @InjectMocks ReleaseToggleService releaseToggleService;

  @Mock ApplicationSettingsService applicationSettingsService;

  @Test
  void isToggleEnabled_Should_ReturnTrueIfApplicationSettingsContainThisToggle() {
    // given
    Map<String, Object> releaseToggles = Maps.newHashMap();
    releaseToggles.put(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS.getValue(), "true");
    when(applicationSettingsService.getApplicationSettings())
        .thenReturn(new ApplicationSettingsDTO().releaseToggles(releaseToggles));
    // when
    assertTrue(releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS));
  }

  @Test
  void isToggleEnabled_Should_ReturnFalseIfApplicationSettingsDoesNotContainThisToggle() {
    // given
    when(applicationSettingsService.getApplicationSettings())
        .thenReturn(new ApplicationSettingsDTO().releaseToggles(Maps.newHashMap()));
    // when
    assertFalse(releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS));
  }
}

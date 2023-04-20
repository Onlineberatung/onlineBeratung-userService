package de.caritas.cob.userservice.api.facade.userdata;

import static org.assertj.core.api.Assertions.assertThat;

import de.caritas.cob.userservice.api.adapters.web.dto.EmailNotificationsDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.model.NotificationsAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailNotificationMapperTest {

  @Mock NotificationsAware notificationsAware;

  @InjectMocks EmailNotificationMapper emailNotificationMapper;

  @Test
  void toEmailNotificationsDTO_Should_ConvertSettingsToDefaultSettingsIfNull() {
    Mockito.when(notificationsAware.isNotificationsEnabled()).thenReturn(true);
    Mockito.when(notificationsAware.getNotificationsSettings()).thenReturn(null);

    EmailNotificationsDTO emailNotificationsDTO =
        emailNotificationMapper.toEmailNotificationsDTO(notificationsAware);

    assertThat(emailNotificationsDTO.getEmailNotificationsEnabled()).isTrue();
    assertThat(emailNotificationsDTO.getSettings()).isEqualTo(new NotificationsSettingsDTO());
  }

  @Test
  void toEmailNotificationsDTO_Should_ConvertSettings() {
    Mockito.when(notificationsAware.isNotificationsEnabled()).thenReturn(true);
    Mockito.when(notificationsAware.getNotificationsSettings())
        .thenReturn(
            "{'initialEnquiryNotificationEnabled': 'true','newChatMessageNotificationEnabled': 'true', 'reassignmentNotificationEnabled': 'true','appointmentNotificationEnabled': 'true'}");

    EmailNotificationsDTO emailNotificationsDTO =
        emailNotificationMapper.toEmailNotificationsDTO(notificationsAware);

    assertThat(emailNotificationsDTO.getEmailNotificationsEnabled()).isTrue();
    assertThat(emailNotificationsDTO.getSettings().getInitialEnquiryNotificationEnabled()).isTrue();
    assertThat(emailNotificationsDTO.getSettings().getNewChatMessageNotificationEnabled()).isTrue();
    assertThat(emailNotificationsDTO.getSettings().getAppointmentNotificationEnabled()).isTrue();
    assertThat(emailNotificationsDTO.getSettings().getReassignmentNotificationEnabled()).isTrue();
  }
}

package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.api.helper.json.JsonSerializationUtils.deserializeFromJsonString;

import de.caritas.cob.userservice.api.adapters.web.dto.EmailNotificationsDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.model.NotificationsAware;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationMapper {

  EmailNotificationsDTO toEmailNotificationsDTO(NotificationsAware notificationsAware) {
    EmailNotificationsDTO emailNotificationsDTO = new EmailNotificationsDTO();
    emailNotificationsDTO.emailNotificationsEnabled(notificationsAware.isNotificationsEnabled());
    if (StringUtils.isBlank(notificationsAware.getNotificationsSettings())) {
      emailNotificationsDTO.settings(new NotificationsSettingsDTO());
    } else {
      emailNotificationsDTO.settings(
          mapNotificationsFromJson(notificationsAware.getNotificationsSettings()));
    }
    return emailNotificationsDTO;
  }

  public static NotificationsSettingsDTO mapNotificationsFromJson(String jsonString) {
    return deserializeFromJsonString(jsonString, NotificationsSettingsDTO.class);
  }
}

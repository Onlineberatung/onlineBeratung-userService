package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.api.helper.json.JsonDeserializer.deserializeFromJsonString;

import de.caritas.cob.userservice.api.adapters.web.dto.EmailNotificationsDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.model.NotificationsAware;

public class EmailNotificationMapper {

  EmailNotificationsDTO toEmailNotificationsDTO(NotificationsAware notificationsAware) {
    EmailNotificationsDTO emailNotificationsDTO = new EmailNotificationsDTO();
    emailNotificationsDTO.emailNotificationsEnabled(notificationsAware.isNotificationEnabled());
    if (notificationsAware.getNotificationSettings() == null) {
      emailNotificationsDTO.settings(new NotificationsSettingsDTO());
    } else {
      emailNotificationsDTO.settings(
          convertNotificationsFromJson(notificationsAware.getNotificationSettings()));
    }
    return emailNotificationsDTO;
  }

  public static NotificationsSettingsDTO convertNotificationsFromJson(String jsonString) {
    return deserializeFromJsonString(jsonString, NotificationsSettingsDTO.class);
  }
}

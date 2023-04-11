package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.helper.json.JsonSerializationUtils;
import de.caritas.cob.userservice.api.model.NotificationSettings;
import de.caritas.cob.userservice.api.model.NotificationsAware;

public class EmailNotificationUtils {

  private EmailNotificationUtils() {}

  public static NotificationSettings deserializeNotificationSettingsOrDefaultIfNull(
      NotificationsAware notificationsAware) {
    return notificationsAware.getNotificationsSettings() == null
        ? new NotificationSettings()
        : JsonSerializationUtils.deserializeFromJsonString(
            notificationsAware.getNotificationsSettings(), NotificationSettings.class);
  }

  public static NotificationsSettingsDTO deserializeNotificationSettingsDTOOrDefaultIfNull(
      NotificationsAware notificationsAware) {
    return notificationsAware.getNotificationsSettings() == null
        ? new NotificationsSettingsDTO()
        : JsonSerializationUtils.deserializeFromJsonString(
            notificationsAware.getNotificationsSettings(), NotificationsSettingsDTO.class);
  }
}

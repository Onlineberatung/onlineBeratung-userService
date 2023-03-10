package de.caritas.cob.userservice.api.model;

/** Marker interface for entities that need to support notifications feature. */
public interface NotificationsAware {

  boolean isNotificationsEnabled();

  void setNotificationsEnabled(boolean notification);

  String getNotificationsSettings();

  void setNotificationsSettings(String settings);
}

package de.caritas.cob.userservice.api.model;

/** Marker interface for entities that need to support notifications feature. */
public interface NotificationsAware {

  boolean isNotificationEnabled();

  String getNotificationSettings();
}

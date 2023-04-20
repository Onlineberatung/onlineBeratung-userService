package de.caritas.cob.userservice.api.service.consultingtype;

import lombok.Getter;

@Getter
public enum ReleaseToggle {
  NEW_EMAIL_NOTIFICATIONS("enableNewNotifications");

  private final String value;

  ReleaseToggle(String value) {
    this.value = value;
  }
}

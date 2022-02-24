package de.caritas.cob.userservice.api.helper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SessionDataKeyRegistration {
  ADDICTIVE_DRUGS("addictiveDrugs"),
  RELATION("relation"),
  AGE("age"),
  GENDER("gender"),
  STATE("state");

  private final String key;

  public String getValue() {
    return this.key;
  }

  public static boolean containsKey(String key) {
    for (var sessionDataKeyRegistration : SessionDataKeyRegistration.values()) {
      if (key.equals(sessionDataKeyRegistration.getValue())) {
        return true;
      }
    }
    return false;
  }
}

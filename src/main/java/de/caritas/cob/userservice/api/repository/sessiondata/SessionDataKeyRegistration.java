package de.caritas.cob.userservice.api.repository.sessiondata;

/**
 * Keys for registration values in session data
 */
public enum SessionDataKeyRegistration {

  ADDICTIVE_DRUGS("addictiveDrugs"), RELATION("relation"), AGE("age"), GENDER("gender"), STATE(
      "state");

  private final String key;

  SessionDataKeyRegistration(String key) {
    this.key = key;
  }

  /**
   * Get the key
   *
   * @return key
   */
  public String getValue() {
    return this.key;
  }

  /**
   * Returns true, if the enum contains the specific key
   *
   * @param key key
   * @return true, if the enum contains the specific key
   */
  public static boolean containsKey(String key) {
    for (SessionDataKeyRegistration sessionDataKeyRegistration : SessionDataKeyRegistration
        .values()) {
      if (key.equals(sessionDataKeyRegistration.getValue())) {
        return true;
      }
    }
    return false;
  }

}

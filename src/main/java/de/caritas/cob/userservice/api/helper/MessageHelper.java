package de.caritas.cob.userservice.api.helper;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;

public class MessageHelper {

  /**
   * Replaces the given username in the given message
   * 
   * @param message
   * @param username
   * @return the message with the replaced placeholder for the username
   */
  public static String replaceUsernameInMessage(String message, String username) {
    Map<String, Object> placeholderMap = new HashMap<String, Object>();
    placeholderMap.put("username", username);
    return replacePlaceholderInMessage(message, placeholderMap);
  }

  /**
   * Replaces all placeholders in a given message.
   * 
   * @param message
   * @param placeholderMap
   * @return the message with the replaced placeholders
   */
  public static String replacePlaceholderInMessage(String message,
      Map<String, Object> placeholderMap) {
    return new StringSubstitutor(placeholderMap, "${", "}").replace(message);
  }

}

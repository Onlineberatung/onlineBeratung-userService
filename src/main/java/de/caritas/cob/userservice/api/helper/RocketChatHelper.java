package de.caritas.cob.userservice.api.helper;

import org.springframework.stereotype.Component;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.session.Session;

@Component
public class RocketChatHelper {

  private static final String FEEDBACK_GROUP_SUFFIX = "feedback";
  private static final String GROUP_CHAT_SUFFIX = "group_chat";

  /**
   * Generates a unique name for the private Rocket.Chat group consisting of the session id and the
   * current time stamp.
   * 
   * @param session
   * @return the group name
   */
  public String generateGroupName(Session session) {
    return generateName(session.getId(), null);
  }

  /**
   * Generates a unique name for the private Rocket.Chat group consisting of the session id, the
   * feedback identifier and the current time stamp.
   * 
   * @param session
   * @return the group name
   */
  public String generateFeedbackGroupName(Session session) {
    return generateName(session.getId(), FEEDBACK_GROUP_SUFFIX);
  }

  /**
   * Generates a unique name for the private Rocket.Chat group consisting of the chat id and the
   * current time stamp.
   * 
   * @param chat
   * @return the group name
   */
  public String generateGroupChatName(Chat chat) {
    return generateName(chat.getId(), GROUP_CHAT_SUFFIX);
  }

  /**
   * Generates a unique name for a private Rocket.Chat group with sessionId and suffix
   * 
   * @param sessionId
   * @param suffix
   * @return the group name
   */
  private String generateName(Long sessionId, String suffix) {
    return String.valueOf(
        sessionId + (suffix != null ? "_" + suffix + "_" : "_") + System.currentTimeMillis());
  }
}

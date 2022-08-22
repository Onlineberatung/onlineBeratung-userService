package de.caritas.cob.userservice.api.testHelper;

public class FieldConstants {

  /** User */
  public static final String FIELD_NAME_EMAIL_DUMMY_SUFFIX = "emailDummySuffix";

  public static final String FIELD_VALUE_EMAIL_DUMMY_SUFFIX = "@onlineberatung.internet";
  public static final String FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID = "rocketChatSystemUserId";
  public static final String FIELD_VALUE_ROCKET_CHAT_SYSTEM_USER_ID = "sdfk323";

  /** UserHelper */
  public static final String FIELD_NAME_HOST_BASE_URL = "hostBaseUrl";

  /** StopChatFacade */
  public static final Long FIELD_VALUE_WEEKLY_PLUS = 1L;

  /** RocketChat */
  public static final String FIELD_NAME_ROCKET_CHAT_TECH_AUTH_TOKEN = "technUserAuthToken";

  public static final String FIELD_NAME_ROCKET_CHAT_TECH_USER_ID = "technUserId";
  public static final String RC_URL_GROUPS_DELETE = "http://localhost/api/v1/groups.delete";
  public static final String RC_URL_GROUPS_SET_READ_ONLY =
      "http://localhost/api/v1/groups.setReadOnly";
  public static final String RC_URL_CHAT_USER_LOGOUT = "http://localhost/api/v1/logout";
  public static final String RC_URL_CHAT_USER_DELETE = "http://localhost/api/v1/users.delete";
  public static final String RC_URL_CHAT_USER_UPDATE = "http://localhost/api/v1/users.update";
}

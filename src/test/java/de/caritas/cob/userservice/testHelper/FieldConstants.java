package de.caritas.cob.userservice.testHelper;

public class FieldConstants {

  /**
   * User
   */
  public static final String FIELD_NAME_EMAIL_DUMMY_SUFFIX = "emailDummySuffix";
  public static final String FIELD_VALUE_EMAIL_DUMMY_SUFFIX = "@onlineberatung.internet";
  public static final String FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID = "rocketChatSystemUserId";
  public static final String FIELD_VALUE_ROCKET_CHAT_SYSTEM_USER_ID = "sdfk323";

  /**
   * UserHelper
   */
  public static final String FIELD_NAME_HOST_BASE_URL = "hostBaseUrl";

  /**
   * StopChatFacade
   */
  public static final String FIELD_NAME_WEEKLY_PLUS = "weeklyPlus";
  public static final Long FIELD_VALUE_WEEKLY_PLUS = 1L;

  /**
   * RocketChat
   */
  public static final String FIELD_NAME_ROCKET_CHAT_HEADER_AUTH_TOKEN = "rocketChatHeaderAuthToken";
  public static final String FIELD_NAME_ROCKET_CHAT_HEADER_USER_ID = "rocketChatHeaderUserId";
  public static final String FIELD_NAME_ROCKET_CHAT_API_GROUP_CREATE_URL =
      "rocketChatApiGroupCreateUrl";
  public static final String FIELD_NAME_ROCKET_CHAT_API_GROUP_DELETE_URL =
      "rocketChatApiGroupDeleteUrl";
  public static final String FIELD_NAME_ROCKET_CHAT_API_SUBSCRIPTIONS_GET_URL =
      "rocketChatApiSubscriptionsGet";
  public static final String FIELD_NAME_ROCKET_CHAT_API_ROOMS_GET_URL = "rocketChatApiRoomsGet";
  public static final String FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGIN = "rocketChatApiUserLogin";
  public static final String FIELD_NAME_ROCKET_CHAT_API_POST_USER_LOGOUT =
      "rocketChatApiUserLogout";
  public static final String FIELD_NAME_ROCKET_CHAT_API_POST_ADD_USER_URL =
      "rocketChatApiGroupAddUserUrl";
  public static final String FIELD_NAME_ROCKET_CHAT_REMOVE_USER_FROM_GROUP_URL =
      "rocketChatApiGroupRemoveUserUrl";
  public static final String FIELD_NAME_ROCKET_CHAT_API_USER_DELETE_URL = "rocketChatApiUserDelete";
  public static final String FIELD_NAME_ROCKET_CHAT_API_CLEAN_ROOM_HISTORY =
      "rocketChatApiCleanRoomHistory";
  public static final String FIELD_NAME_ROCKET_CHAT_TECH_AUTH_TOKEN = "technUserAuthToken";
  public static final String FIELD_NAME_ROCKET_CHAT_TECH_USER_ID = "technUserId";
  public static final String FIELD_NAME_ROCKET_CHAT_SYSTEM_AUTH_TOKEN = "systemUserAuthToken";
  public static final String FIELD_NAME_ROCKET_CHAT_SERVICE_SYSTEM_USER_ID = "systemUserId";
  public static final String FIELD_NAME_ROCKET_CHAT_API_USER_INFO = "rocketChatApiUserInfo";

  public static final String RC_URL_GROUPS_CREATE = "http://localhost/api/v1/groups.create";
  public static final String RC_URL_GROUPS_DELETE = "http://localhost/api/v1/groups.delete";
  public static final String RC_URL_GROUPS_REMOVE_USER = "http://localhost/api/v1/groups.kick";
  public static final String RC_URL_CHAT_USER_LOGIN = "http://localhost/api/v1/login";
  public static final String RC_URL_CHAT_USER_LOGOUT = "http://localhost/api/v1/logout";
  public static final String RC_URL_CHAT_ADD_USER = "http://localhost/api/v1/groups.invite";
  public static final String RC_URL_CHAT_USER_DELETE = "http://localhost/api/v1/users.delete";
  public static final String RC_URL_CLEAN_ROOM_HISTORY =
      "http://localhost/api/v1/rooms.cleanHistory";
  public static final String RC_URL_SUBSCRIPTIONS_GET = "http://localhost/api/v1/subscriptions.get";
  public static final String RC_URL_ROOMS_GET = "http://localhost/api/v1/rooms.get";
  public static final String RC_URL_USERS_INFO_GET = "http://localhost/api/v1/users.info";
}

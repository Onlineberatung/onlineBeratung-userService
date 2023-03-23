package de.caritas.cob.userservice.api.testHelper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.INVALID_CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;

public class PathConstants {

  public static final String PATH_GET_SESSIONS_FOR_AUTHENTICATED_USER = "/users/sessions/askers";
  public static final String PATH_CREATE_ENQUIRY_MESSAGE =
      "/users/sessions/" + SESSION_ID + "/enquiry/new";
  public static final String PATH_REGISTER_USER = "/users/askers/new";
  public static final String PATH_ACTIVATE_2FA = "/users/2fa/app";
  public static final String PATH_ACCEPT_ENQUIRY = "/users/sessions/new/";
  public static final String PATH_PUT_CONSULTANT_ABSENT = "/users/consultants/absences";
  public static final String PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT =
      "/users/sessions/consultants?status=1&offset=0&count=10&filter=all";
  public static final String PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_INVALID_FILTER =
      "/users/sessions/consultants?status=1&offset=0&count=10&filter=sdfsdfsdfsddfdfds";
  public static final String PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_OFFSET =
      "/users/sessions/consultants?status=1&count=10&filter=all";
  public static final String PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_NEGATIVE_OFFSET =
      "/users/sessions/consultants?status=1&offset=-5&count=10&filter=all";
  public static final String PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_COUNT =
      "/users/sessions/consultants?status=1&offset=0&filter=all";
  public static final String PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_NEGATIVE_COUNT =
      "/users/sessions/consultants?status=1&offset=0&count=-10&filter=all";
  public static final String PATH_GET_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_STATUS =
      "/users/sessions/consultants";
  public static final String PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT =
      "/users/sessions/teams?offset=0&count=1&filter=all";
  public static final String PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_OFFSET =
      "/users/sessions/teams?count=1&filter=all";
  public static final String
      PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_NEGATIVE_OFFSET =
          "/users/sessions/teams?offset=-10&count=1&filter=all";
  public static final String PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITHOUT_COUNT =
      "/users/sessions/teams?offset=0&filter=all";
  public static final String
      PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_NEGATIVE_COUNT =
          "/users/sessions/teams?offset=0&count=-1&filter=all";
  public static final String
      PATH_GET_TEAM_SESSIONS_FOR_AUTHENTICATED_CONSULTANT_WITH_INVALID_FILTER =
          "/users/sessions/teams?offset=0&count=1&filter=sdfsegsgsdfdsf";
  public static final String PATH_SEND_NEW_MESSAGE_NOTIFICATION = "/users/mails/messages/new";
  public static final String PATH_USER_DATA = "/users/data";
  public static final String PATH_GET_CONSULTANTS_FOR_AGENCY_WITHOUT_PARAM = "/users/consultants";
  public static final String PATH_GET_CONSULTANTS_FOR_AGENCY = "/users/consultants?agencyId=10";
  public static final String PATH_PUT_ASSIGN_SESSION =
      "/users/sessions/" + SESSION_ID + "/consultant/" + CONSULTANT_ID;
  public static final String PATH_PUT_ASSIGN_SESSION_INVALID_PARAMS =
      "/users/sessions/43das/consultant/0";
  public static final String PATH_PUT_UPDATE_PASSWORD = "/users/password/change";
  public static final String PATH_UPDATE_KEY = "/users/messages/key?key=";
  public static final String PATH_GET_USER_DATA = "/users/data";
  public static final String PATH_GET_OPEN_SESSIONS_FOR_AUTHENTICATED_CONSULTANT =
      "/users/sessions/open";
  public static final String PATH_GET_ENQUIRIES_FOR_AGENCY = "/users/sessions/consultants/new";
  public static final String PATH_POST_REGISTER_USER = "/users/askers/new";
  public static final String PATH_POST_REGISTER_NEW_CONSULTING_TYPE =
      "/users/askers/consultingType/new";
  public static final String PATH_POST_NEW_MESSAGE_NOTIFICATION = "/users/mails/messages/new";
  public static final String PATH_POST_IMPORT_CONSULTANTS = "/users/consultants/import";
  public static final String PATH_POST_IMPORT_ASKERS = "/users/askers/import";
  public static final String PATH_GET_CONSULTANTS = "/users/consultants";
  public static final String PATH_POST_CHAT_NEW = "/users/chat/new";
  public static final String PATH_POST_CHAT_NEW_V2 = "/users/chat/v2/new";
  public static final String PATH_PUT_CHAT_START = "/users/chat/" + CHAT_ID + "/start";
  public static final String PATH_PUT_CHAT_START_WITH_INVALID_PATH_PARAMS =
      "/users/chat/" + INVALID_CHAT_ID + "/start";
  public static final String PATH_PUT_ASSIGN_CHAT = "/users/chat/" + RC_GROUP_ID + "/assign";
  public static final String PATH_PUT_JOIN_CHAT = "/users/chat/" + CHAT_ID + "/join";
  public static final String PATH_PUT_JOIN_CHAT_WITH_INVALID_PATH_PARAMS =
      "/users/chat/" + INVALID_CHAT_ID + "/join";
  public static final String PATH_PUT_LEAVE_CHAT = "/users/chat/" + CHAT_ID + "/leave";
  public static final String PATH_GET_CHAT = "/users/chat/" + CHAT_ID;
  public static final String PATH_GET_CHAT_WITH_INVALID_PATH_PARAMS =
      "/users/chat/" + INVALID_CHAT_ID;
  public static final String PATH_GET_CHAT_MEMBERS = "/users/chat/" + CHAT_ID + "/members";
  public static final String PATH_GET_CHAT_MEMBERS_WITH_INVALID_PATH_PARAMS =
      "/users/chat/" + INVALID_CHAT_ID + "/members";
  public static final String PATH_PUT_CHAT_STOP = "/users/chat/" + CHAT_ID + "/stop";
  public static final String PATH_PUT_CHAT_STOP_INVALID = "/users/chat/invalid/stop";
  public static final String PATH_PUT_UPDATE_CHAT = "/users/chat/" + CHAT_ID + "/update";
  public static final String PATH_PUT_UPDATE_CHAT_INVALID_PATH_PARAMS =
      "/users/chat/" + INVALID_CHAT_ID + "/update";
  public static final String PATH_POST_IMPORT_ASKERS_WITHOUT_SESSION =
      "/users/askersWithoutSession/import";
  public static final String PATH_GET_SESSION_FOR_CONSULTANT = "/users/consultants/sessions/1";
  public static final String PATH_PUT_UPDATE_EMAIL = "/users/email";
  public static final String PATH_DELETE_FLAG_USER_DELETED = "/users/account";
  public static final String PATH_PUT_UPDATE_MOBILE_TOKEN = "/users/mobiletoken";
  public static final String PATH_PUT_UPDATE_SESSION_DATA = "/users/sessions/123/data";
  public static final String PATH_PUT_UPDATE_SESSION_DATA_INVALID_PATH_VAR =
      "/users/sessions" + "/1x2y3/data";
  public static final String PATH_ARCHIVE_SESSION = "/users/sessions/123/archive";
  public static final String PATH_ARCHIVE_SESSION_INVALID_PATH_VAR = "/users/sessions/xyz/archive";
  public static final String PATH_DEARCHIVE_SESSION = "/users/sessions/123/dearchive";
  public static final String PATH_DEARCHIVE_SESSION_INVALID_PATH_VAR =
      "/users/sessions/xyz/dearchive";
  public static final String PATH_PUT_ADD_MOBILE_TOKEN = "/users/mobile/app/token";
}

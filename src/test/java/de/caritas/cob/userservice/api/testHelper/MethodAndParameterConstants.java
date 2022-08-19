package de.caritas.cob.userservice.api.testHelper;

public class MethodAndParameterConstants {

  /** RocketChatService */
  public static final String ADD_USER_TO_GROUP_METHOD_NAME = "addUserToGroup";

  public static final Class<?>[] ADD_USER_TO_GROUP_METHOD_PARAMS =
      new Class[] {String.class, String.class};
  public static final String REMOVE_USER_FROM_GROUP_METHOD_NAME = "removeUserFromGroup";
  public static final Class<?>[] REMOVE_USER_FROM_GROUP_METHOD_PARAMS =
      new Class[] {String.class, String.class};
  public static final String GET_MEMBERS_OF_GROUP_METHOD_NAME = "getMembersOfGroup";
  public static final Class<?>[] GET_MEMBERS_OF_GROUP_METHOD_PARAMS = new Class[] {String.class};
  public static final String GET_GROUP_COUNTERS_METHOD_NAME = "getGroupCounters";
  public static final Class<?>[] GET_GROUP_COUNTERS_METHOD_PARAMS =
      new Class[] {String.class, String.class};
  public static final String DELETE_USER_METHOD_NAME = "deleteUser";
  public static final Class<?>[] DELETE_USER_METHOD_PARAMS = new Class[] {String.class};
  public static final String LOGOUT_TECHNICAL_USER_METHOD_NAME = "logoutTechnicalUser";
  public static final String DELETE_GROUP_AS_SYSUSER_METHOD_NAME = "deleteGroupAsSystemUser";
  public static final Class<?>[] DELETE_GROUP_AS_SYSUSER_METHOD_PARAMS = new Class[] {String.class};
  public static final String REMOVE_TECHNICAL_USER_FROM_GROUP_METHOD_NAME =
      "removeTechnicalUserFromGroup";
  public static final Class<?>[] REMOVE_TECHNICAL_USER_FROM_GROUP_METHOD_PARAMS =
      new Class[] {String.class};
  public static final String GET_STANDARD_MEMBERS_OF_GROUP_METHOD_NAME =
      "getStandardMembersOfGroup";
  public static final Class<?>[] GET_STANDARD_MEMBERS_OF_GROUP_METHOD_PARAMS =
      new Class[] {String.class};
}

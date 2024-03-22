package de.caritas.cob.userservice.api.admin.service.consultant;

public enum TransactionalStep {
  CREATE_ACCOUNT_IN_KEYCLOAK,
  CREATE_CONSULTANT_IN_MARIADB,

  CREATE_ACCOUNT_IN_ROCKETCHAT,

  CREATE_ACCOUNT_IN_CALCOM_OR_APPOINTMENTSERVICE,

  SAVE_CONSULTANT_IN_MARIADB,
  ROLLBACK_CONSULTANT_IN_MARIADB,
  UPDATE_ROCKET_CHAT_USER_DISPLAY_NAME,

  ROLLBACK_UPDATE_ROCKET_CHAT_USER_DISPLAY_NAME,

  PATCH_APPOINTMENT_SERVICE_CONSULTANT,
  UPDATE_USER_PASSWORD_IN_KEYCLOAK,
  UPDATE_USER_ROLES_IN_KEYCLOAK;
}
package de.caritas.cob.userservice.api.admin.service.consultant;

public enum TransactionalStep {
  CREATE_ACCOUNT_IN_KEYCLOAK,
  CREATE_CONSULTANT_IN_MARIADB,

  CREATE_ACCOUNT_IN_ROCKETCHAT,

  CREATE_ACCOUNT_IN_CALCOM_OR_APPOINTMENTSERVICE
}

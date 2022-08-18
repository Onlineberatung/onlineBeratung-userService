package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;

import java.time.LocalDateTime;

/** Definition for required input data used to create a new consultant. */
interface ConsultantCreationInput {

  Long getIdOld();

  String getUserName();

  String getEncodedUsername();

  String getFirstName();

  String getLastName();

  String getEmail();

  boolean isAbsent();

  String getAbsenceMessage();

  boolean isTeamConsultant();

  boolean isLanguageFormal();

  default LocalDateTime getCreateDate() {
    return nowInUtc();
  }

  default LocalDateTime getUpdateDate() {
    return nowInUtc();
  }

  Long getTenantId();
}

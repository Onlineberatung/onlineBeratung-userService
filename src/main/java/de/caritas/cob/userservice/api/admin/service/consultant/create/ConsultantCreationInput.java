package de.caritas.cob.userservice.api.admin.service.consultant.create;

import java.time.LocalDateTime;

/**
 * Definition for required input data used to create a new consultant.
 */
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

  LocalDateTime getCreateDate();

  LocalDateTime getUpdateDate();

}

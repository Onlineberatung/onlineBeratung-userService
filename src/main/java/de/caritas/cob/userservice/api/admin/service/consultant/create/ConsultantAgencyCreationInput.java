package de.caritas.cob.userservice.api.admin.service.consultant.create;

import java.time.LocalDateTime;

/**
 * Definition for required input data used to create a new consultantAgency.
 */
public interface ConsultantAgencyCreationInput {

  String getConsultantId();

  String getRole();

  Long getAgencyId();

  LocalDateTime getCreateDate();

  LocalDateTime getUpdateDate();

}

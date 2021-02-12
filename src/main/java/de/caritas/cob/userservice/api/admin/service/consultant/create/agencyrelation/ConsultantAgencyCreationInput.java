package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Definition for required input data used to create a new consultantAgency.
 */
public interface ConsultantAgencyCreationInput {

  String getConsultantId();

  Set<String> getRoles();

  Long getAgencyId();

  default LocalDateTime getCreateDate() {
    return nowInUtc();
  }

  default LocalDateTime getUpdateDate() {
    return nowInUtc();
  }

}

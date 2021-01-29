package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * Definition for required input data used to create a new consultantAgency.
 */
public interface ConsultantAgencyCreationInput {

  String getConsultantId();

  Set<String> getRoles();

  Long getAgencyId();

  default LocalDateTime getCreateDate() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

  default LocalDateTime getUpdateDate() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

}

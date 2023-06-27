package de.caritas.cob.userservice.api.adapters.web.dto;

import java.util.Collection;

public interface UserRegistrationDTO {

  Long getAgencyId();

  String getPostcode();

  String getConsultingType();

  boolean isNewUserAccount();

  String getConsultantId();

  Long getMainTopicId();

  String getUserGender();

  Integer getUserAge();

  Collection<Long> getTopicIds();

  String getCounsellingRelation();

  String getReferer();
}

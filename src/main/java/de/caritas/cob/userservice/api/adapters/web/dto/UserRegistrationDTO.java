package de.caritas.cob.userservice.api.adapters.web.dto;

public interface UserRegistrationDTO {

  Long getAgencyId();

  String getPostcode();

  String getConsultingType();

  boolean isNewUserAccount();

  String getConsultantId();
}

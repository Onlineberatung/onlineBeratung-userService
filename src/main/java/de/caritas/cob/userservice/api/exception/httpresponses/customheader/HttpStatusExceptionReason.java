package de.caritas.cob.userservice.api.exception.httpresponses.customheader;

public enum HttpStatusExceptionReason {
  USERNAME_NOT_AVAILABLE,
  USERNAME_NOT_VALID,
  EMAIL_NOT_AVAILABLE,
  EMAIL_NOT_VALID,
  MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER,
  CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST,
  CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_IS_STILL_ACTIVE,
  CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_HAS_OPEN_ENQUIRIES,
  CONSULTANT_HAS_ACTIVE_SESSIONS,
  DEMOGRAPHICS_ATTRIBUTE_MISSING, NUMBER_OF_LICENSES_EXCEEDED
}

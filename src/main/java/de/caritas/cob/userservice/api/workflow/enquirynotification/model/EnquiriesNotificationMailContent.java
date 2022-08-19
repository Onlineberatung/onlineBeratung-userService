package de.caritas.cob.userservice.api.workflow.enquirynotification.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnquiriesNotificationMailContent {

  private Long amountOfOpenEnquiries;
  private Long agencyId;
  private String agencyName;
}

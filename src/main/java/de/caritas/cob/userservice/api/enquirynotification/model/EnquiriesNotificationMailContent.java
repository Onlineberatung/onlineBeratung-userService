package de.caritas.cob.userservice.api.enquirynotification.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnquiriesNotificationMailContent {

  private Long amountOfOpenEnquiries;
  private Long agencyId;

  @Override
  public String toString() {
    return String
        .format("Ihre Beratungsstelle hat aktuell %s offene Erstanfragen.", amountOfOpenEnquiries);
  }

}

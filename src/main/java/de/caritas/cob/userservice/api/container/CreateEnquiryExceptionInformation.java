package de.caritas.cob.userservice.api.container;

import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.model.Session;
import lombok.Builder;
import lombok.Data;

/**
 * Builder for {@link CreateEnquiryMessageFacade} exception rollback parameter values
 */
@Data
@Builder
public class CreateEnquiryExceptionInformation {

  private Session session;
  private String rcGroupId;
  private String rcFeedbackGroupId;
}

package de.caritas.cob.userservice.api.container;

import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.repository.session.Session;
import lombok.Builder;
import lombok.Getter;

/**
 * Builder for {@link CreateEnquiryMessageFacade} exception rollback parameter values
 *
 */
@Getter
@Builder
public class CreateEnquiryExceptionInformation {

  private Session session;
  private String rcGroupId;
  private String rcFeedbackGroupId;
}

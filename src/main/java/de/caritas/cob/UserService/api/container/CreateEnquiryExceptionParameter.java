package de.caritas.cob.UserService.api.container;

import de.caritas.cob.UserService.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.UserService.api.repository.session.Session;
import lombok.Builder;
import lombok.Getter;

/**
 * Builder for {@link CreateEnquiryMessageFacade} exception rollback parameter values
 *
 */
@Getter
@Builder
public class CreateEnquiryExceptionParameter {

  private Session session;
  private String rcGroupId;
  private String rcFeedbackGroupId;
}

package de.caritas.cob.userservice.api.conversation.facade;

import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacade;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserAccountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade to encapsulate necessary steps to accept an anonymous enquiry. */
@Service
@RequiredArgsConstructor
public class AcceptAnonymousEnquiryFacade {

  private final @NonNull AssignEnquiryFacade assignEnquiryFacade;
  private final @NonNull LiveEventNotificationService liveEventNotificationService;
  private final @NonNull SessionService sessionService;
  private final @NonNull UserAccountService userAccountProvider;

  /**
   * Accepts the anonymous enquiry with the given session id and assigns the session to the current
   * authenticated consultant.
   *
   * @param sessionId the id of the anonymous session
   */
  public void acceptAnonymousEnquiry(Long sessionId) {
    var session =
        sessionService
            .getSession(sessionId)
            .orElseThrow(
                () -> new NotFoundException("Session with id %s does not exist", sessionId));

    var consultant = this.userAccountProvider.retrieveValidatedConsultant();
    this.assignEnquiryFacade.assignAnonymousEnquiry(session, consultant);
    this.liveEventNotificationService.sendAcceptAnonymousEnquiryEventToUser(
        session.getUser().getUserId());
  }
}

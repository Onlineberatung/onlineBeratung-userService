package de.caritas.cob.userservice.api.conversation.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacade;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserAccountService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcceptAnonymousEnquiryFacadeTest {

  @InjectMocks private AcceptAnonymousEnquiryFacade acceptAnonymousEnquiryFacade;

  @Mock private AssignEnquiryFacade assignEnquiryFacade;

  @Mock private LiveEventNotificationService liveEventNotificationService;

  @Mock private SessionService sessionService;

  @Mock private UserAccountService userAccountService;

  @Test
  void acceptAnonymousEnquiry_Should_useServicesCorrectly_When_sessionExists() {
    Session session = new EasyRandom().nextObject(Session.class);
    when(this.sessionService.getSession(session.getId())).thenReturn(Optional.of(session));

    this.acceptAnonymousEnquiryFacade.acceptAnonymousEnquiry(session.getId());

    verify(this.userAccountService, times(1)).retrieveValidatedConsultant();
    verify(this.assignEnquiryFacade, times(1)).assignAnonymousEnquiry(eq(session), any());
    verify(this.liveEventNotificationService, times(1))
        .sendAcceptAnonymousEnquiryEventToUser(session.getUser().getUserId());
  }

  @Test
  void acceptAnonymousEnquiry_Should_throwNotFoundException_When_sessionDoesNotExist() {
    assertThrows(
        NotFoundException.class,
        () -> {
          this.acceptAnonymousEnquiryFacade.acceptAnonymousEnquiry(1L);
        });
  }
}

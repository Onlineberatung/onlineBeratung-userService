package de.caritas.cob.userservice.api.service.conversation.anonymous;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_DTO_SUCHT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.conversation.model.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.conversation.service.AnonymousConversationCreatorService;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AnonymousConversationCreatorServiceTest {

  @InjectMocks private AnonymousConversationCreatorService anonymousConversationCreatorService;
  @Mock private UserService userService;
  @Mock SessionService sessionService;
  @Mock RollbackFacade rollbackFacade;
  @Mock CreateEnquiryMessageFacade createEnquiryMessageFacade;
  @Mock AgencyService agencyService;
  @Mock ConsultantAgencyService consultantAgencyService;
  @Mock LiveEventNotificationService liveEventNotificationService;

  EasyRandom easyRandom = new EasyRandom();

  @Test
  void
      createAnonymousConversation_Should_ThrowInternalServerErrorException_When_ProvidedUserDoesNotExist() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(userService.getUser(anyString())).thenReturn(Optional.empty());
          AnonymousUserCredentials credentials =
              easyRandom.nextObject(AnonymousUserCredentials.class);

          anonymousConversationCreatorService.createAnonymousConversation(
              USER_DTO_SUCHT, credentials);

          verifyNoInteractions(sessionService);
          verifyNoInteractions(rollbackFacade);
          verifyNoInteractions(createEnquiryMessageFacade);
          verifyNoInteractions(agencyService);
          verifyNoInteractions(consultantAgencyService);
          verifyNoInteractions(liveEventNotificationService);
        });
  }

  @Test
  void
      createAnonymousConversation_Should_ThrowInternalServerErrorExceptionAndPerformRollback_When_InitializingSessionFails() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(userService.getUser(anyString())).thenReturn(Optional.of(USER));
          when(sessionService.initializeSession(
                  any(User.class),
                  any(UserDTO.class),
                  anyBoolean(),
                  any(RegistrationType.class),
                  any(SessionStatus.class)))
              .thenThrow(new IllegalArgumentException());
          AnonymousUserCredentials credentials =
              easyRandom.nextObject(AnonymousUserCredentials.class);

          anonymousConversationCreatorService.createAnonymousConversation(
              USER_DTO_SUCHT, credentials);

          verifyNoInteractions(rollbackFacade);
          verifyNoInteractions(createEnquiryMessageFacade);
          verifyNoInteractions(agencyService);
          verifyNoInteractions(consultantAgencyService);
          verifyNoInteractions(liveEventNotificationService);
        });
  }

  @Test
  void
      createAnonymousConversation_Should_ThrowInternalServerErrorExceptionAndPerformRollback_When_CreateRcRoomFails()
          throws CreateEnquiryException {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(userService.getUser(anyString())).thenReturn(Optional.of(USER));
          when(sessionService.initializeSession(
                  any(User.class),
                  any(UserDTO.class),
                  anyBoolean(),
                  any(RegistrationType.class),
                  any(SessionStatus.class)))
              .thenReturn(SESSION);
          CreateEnquiryException exception = easyRandom.nextObject(CreateEnquiryException.class);
          when(createEnquiryMessageFacade.createRocketChatRoomAndAddUsers(any(), any(), any()))
              .thenThrow(exception);
          AnonymousUserCredentials credentials =
              easyRandom.nextObject(AnonymousUserCredentials.class);

          anonymousConversationCreatorService.createAnonymousConversation(
              USER_DTO_SUCHT, credentials);

          verifyNoInteractions(agencyService);
          verifyNoInteractions(consultantAgencyService);
          verifyNoInteractions(liveEventNotificationService);
        });
  }

  @Test
  void createAnonymousConversation_Should_ReturnSessionAndTriggerLiveEvent()
      throws CreateEnquiryException {
    when(userService.getUser(anyString())).thenReturn(Optional.of(USER));
    when(sessionService.initializeSession(
            any(User.class),
            any(UserDTO.class),
            anyBoolean(),
            any(RegistrationType.class),
            any(SessionStatus.class)))
        .thenReturn(SESSION);
    when(createEnquiryMessageFacade.createRocketChatRoomAndAddUsers(any(), any(), any()))
        .thenReturn(ROCKETCHAT_ID);
    AnonymousUserCredentials credentials = easyRandom.nextObject(AnonymousUserCredentials.class);

    Session session =
        anonymousConversationCreatorService.createAnonymousConversation(
            USER_DTO_SUCHT, credentials);

    assertThat(session, instanceOf(Session.class));
    verify(liveEventNotificationService, times(1))
        .sendLiveNewAnonymousEnquiryEventToUsers(any(), any());
  }
}

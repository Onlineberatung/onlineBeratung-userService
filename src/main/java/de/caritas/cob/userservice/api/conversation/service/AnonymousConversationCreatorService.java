package de.caritas.cob.userservice.api.conversation.service;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.conversation.model.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service to create anonymous user conversations (sessions). */
@Service
@RequiredArgsConstructor
public class AnonymousConversationCreatorService {

  private final @NonNull UserService userService;
  private final @NonNull SessionService sessionService;
  private final @NonNull RollbackFacade rollbackFacade;
  private final @NonNull CreateEnquiryMessageFacade createEnquiryMessageFacade;
  private final @NonNull AgencyService agencyService;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull LiveEventNotificationService liveEventNotificationService;

  /**
   * Creates a new anonymous conversation session with the corresponding Rocket.Chat room.
   *
   * @param userDTO {@link UserDTO}
   * @param credentials {@link AnonymousUserCredentials}
   * @return {@link Session}
   */
  public Session createAnonymousConversation(
      UserDTO userDTO, AnonymousUserCredentials credentials) {

    var user = obtainAnonymousUser(credentials);
    List<ConsultantAgency> consultantAgencies;
    Session session;

    try {
      session =
          sessionService.initializeSession(
              user, userDTO, false, RegistrationType.ANONYMOUS, SessionStatus.NEW);
      consultantAgencies = obtainConsultants(session.getConsultingTypeId());
      String rcGroupId =
          createEnquiryMessageFacade.createRocketChatRoomAndAddUsers(
              session, consultantAgencies, credentials.getRocketChatCredentials());
      session.setGroupId(rcGroupId);
      sessionService.saveSession(session);

    } catch (Exception ex) {
      rollBackAnonymousConversation(userDTO, user);
      throw new InternalServerErrorException(
          String.format(
              "Could not create session for user %s. %s", user.getUsername(), ex.getMessage()));
    }

    sendNewAnonymousEnquiryLiveEvent(consultantAgencies, session);

    return session;
  }

  private User obtainAnonymousUser(AnonymousUserCredentials credentials) {
    return userService
        .getUser(credentials.getUserId())
        .orElseThrow(
            () ->
                new InternalServerErrorException(
                    String.format(
                        "Could not get user %s to create a new anonymous conversation.",
                        credentials.getUserId())));
  }

  private List<ConsultantAgency> obtainConsultants(int consultingTypeId) {
    List<Long> agencyList =
        agencyService.getAgenciesByConsultingType(consultingTypeId).stream()
            .map(AgencyDTO::getId)
            .collect(Collectors.toList());

    return consultantAgencyService.getConsultantsOfAgencies(agencyList);
  }

  private void rollBackAnonymousConversation(UserDTO userDTO, User user) {
    rollbackFacade.rollBackUserAccount(
        RollbackUserAccountInformation.builder()
            .userId(user.getUserId())
            .user(user)
            .rollBackUserAccount(Boolean.parseBoolean(userDTO.getTermsAccepted()))
            .build());
  }

  private void sendNewAnonymousEnquiryLiveEvent(
      List<ConsultantAgency> consultantAgencies, Session session) {
    liveEventNotificationService.sendLiveNewAnonymousEnquiryEventToUsers(
        consultantAgencies.stream()
            .map(agency -> agency.getConsultant().getId())
            .collect(Collectors.toList()),
        session.getId());
  }
}

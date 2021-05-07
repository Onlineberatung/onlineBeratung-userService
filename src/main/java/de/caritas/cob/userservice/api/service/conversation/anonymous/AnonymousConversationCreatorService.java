package de.caritas.cob.userservice.api.service.conversation.anonymous;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.RegistrationType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** TODO */
@Service
@RequiredArgsConstructor
public class AnonymousConversationCreatorService {

  private final @NonNull UserService userService;
  private final @NonNull SessionService sessionService;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull RollbackFacade rollbackFacade;
  private final @NonNull CreateEnquiryMessageFacade createEnquiryMessageFacade;

  public Session createAnonymousConversation(String userId, UserDTO userDTO,
      RocketChatCredentials rocketChatCredentials, ConsultingType consultingType) {

    var user = userService.getUser(userId)
        .orElseThrow(() -> new InternalServerErrorException(
            "User not found for creating a new anonymous conversation."));
    var consultingTypeSettings =
        consultingTypeManager.getConsultingTypeSettings(consultingType);

    try {
      Session session =
          sessionService.initializeSession(user, userDTO, false, consultingTypeSettings,
              RegistrationType.ANONYMOUS);

      String rcGroupId = createEnquiryMessageFacade.createAndRetrieveRcGroupIdForAnonymousEnquiry(
          session, rocketChatCredentials);
      session.setGroupId(rcGroupId);
      sessionService.saveSession(session);

      return session;
    } catch (Exception ex) {
      rollbackFacade.rollBackUserAccount(
          RollbackUserAccountInformation.builder()
              .userId(user.getUserId())
              .user(user)
              .rollBackUserAccount(Boolean.parseBoolean(userDTO.getTermsAccepted()))
              .build());

      throw new InternalServerErrorException(
          String.format(
              "Could not create session for user %s. %s", user.getUsername(), ex.getMessage()));
    }
  }
}

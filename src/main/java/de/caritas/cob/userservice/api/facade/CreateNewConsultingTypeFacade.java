package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.registration.NewRegistrationDto;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.registration.UserRegistrationDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateNewConsultingTypeFacade {

  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull CreateUserChatRelationFacade createUserChatRelationFacade;
  private final @NonNull CreateSessionFacade createSessionFacade;

  /**
   * Initializes the new consulting type settings and creates a session or a chat-agency relation
   * depending on its type.
   *
   * @param userRegistrationDTO   {@link UserRegistrationDTO}
   * @param user                  {@link User}
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return session ID of created session (if not consulting type {@link ConsultingType#KREUZBUND}
   */
  public Long initializeNewConsultingType(UserRegistrationDTO userRegistrationDTO, User user,
      RocketChatCredentials rocketChatCredentials) {
    ConsultingType consultingType =
        ConsultingType.values()[Integer.parseInt(userRegistrationDTO.getConsultingType())];
    ConsultingTypeSettings consultingTypeSettings;
    try {
      consultingTypeSettings = consultingTypeManager.getConsultantTypeSettings(consultingType);
    } catch (MissingConsultingTypeException e) {
      throw new BadRequestException(e.getMessage(), LogService::logInternalServerError);
    }

    return createSessionOrChat(userRegistrationDTO, user,
        consultingTypeSettings, rocketChatCredentials);
  }

  /**
   * Initializes the new consulting type settings and creates a session or a chat-agency relation
   * depending on its type.
   *
   * @param userRegistrationDTO    {@link UserRegistrationDTO}
   * @param user                   {@link User}
   * @param consultingTypeSettings {@link ConsultingTypeSettings}
   */
  public void initializeNewConsultingType(UserRegistrationDTO userRegistrationDTO, User user,
      ConsultingTypeSettings consultingTypeSettings) {

    createSessionOrChat(userRegistrationDTO, user, consultingTypeSettings, null);
  }

  private UserDTO fromUserRegistrationDTO(UserRegistrationDTO userRegistrationDTO) {
    if (userRegistrationDTO instanceof NewRegistrationDto) {
      UserDTO userDTO = new UserDTO();
      userDTO.setAgencyId(userRegistrationDTO.getAgencyId());
      userDTO.setPostcode(userRegistrationDTO.getPostcode());
      userDTO.setConsultingType(userRegistrationDTO.getConsultingType());
      return userDTO;
    }

    return (UserDTO) userRegistrationDTO;
  }

  private Long createSessionOrChat(UserRegistrationDTO user, User dbUser,
      ConsultingTypeSettings consultingTypeSettings, RocketChatCredentials rocketChatCredentials) {

    Long sessionId = null;

    if (consultingTypeSettings.getConsultingType().equals(ConsultingType.KREUZBUND)) {
      createUserChatRelationFacade
          .initializeUserChatAgencyRelation(fromUserRegistrationDTO(user), dbUser,
              rocketChatCredentials);

    } else {
      sessionId = createSessionFacade
          .createUserSession(fromUserRegistrationDTO(user), dbUser, consultingTypeSettings);
    }

    return sessionId;
  }

}
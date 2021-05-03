package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.registration.NewRegistrationDto;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.registration.UserRegistrationDTO;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps to initialize a new consulting type.
 */
@Service
@RequiredArgsConstructor
public class CreateNewConsultingTypeFacade {

  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull CreateUserChatRelationFacade createUserChatRelationFacade;
  private final @NonNull CreateSessionFacade createSessionFacade;

  /**
   * Initializes the new consulting type settings and creates a session or a chat-agency relation
   * depending on its type. This method should be used for new consulting type registrations.
   *
   * @param userRegistrationDTO   {@link UserRegistrationDTO}
   * @param user                  {@link User}
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return session ID of created session (if not consulting id refers to a group only consulting type)
   */
  public Long initializeNewConsultingType(UserRegistrationDTO userRegistrationDTO, User user,
      RocketChatCredentials rocketChatCredentials) {
    try {
      ConsultingTypeSettings consultingTypeSettings = consultingTypeManager
          .getConsultingTypeSettings(userRegistrationDTO.getConsultingType());

      return createSessionOrChat(userRegistrationDTO, user,
          consultingTypeSettings, rocketChatCredentials);
    } catch (MissingConsultingTypeException | IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), LogService::logInternalServerError);
    }
  }

  /**
   * Initializes the new consulting type settings and creates a session or a chat-agency relation
   * depending on its type. This method should be used for new user account registrations.
   *
   * @param userRegistrationDTO    {@link UserRegistrationDTO}
   * @param user                   {@link User}
   * @param consultingTypeSettings {@link ConsultingTypeSettings}
   */
  public void initializeNewConsultingType(UserRegistrationDTO userRegistrationDTO, User user,
      ConsultingTypeSettings consultingTypeSettings) {

    createSessionOrChat(userRegistrationDTO, user, consultingTypeSettings, null);
  }

  private Long createSessionOrChat(UserRegistrationDTO userRegistrationDTO, User user,
      ConsultingTypeSettings consultingTypeSettings, RocketChatCredentials rocketChatCredentials) {

    Long sessionId = null;

    if (consultingTypeSettings.isGroupChat()) {
      createUserChatRelationFacade
          .initializeUserChatAgencyRelation(fromUserRegistrationDTO(userRegistrationDTO), user,
              rocketChatCredentials);

    } else {
      sessionId = createSessionFacade
          .createUserSession(fromUserRegistrationDTO(userRegistrationDTO), user,
              consultingTypeSettings);
    }

    return sessionId;
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

}

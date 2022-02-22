package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.adapters.web.dto.NewRegistrationResponseDto;
import de.caritas.cob.userservice.api.adapters.web.dto.NewRegistrationDto;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserRegistrationDTO;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
   * @return session ID of created session (if not consulting id refers to a group only consulting
   * type)
   */
  public NewRegistrationResponseDto initializeNewConsultingType(
      UserRegistrationDTO userRegistrationDTO, User user,
      RocketChatCredentials rocketChatCredentials) {
    try {
      var extendedConsultingTypeResponseDTO = consultingTypeManager
          .getConsultingTypeSettings(userRegistrationDTO.getConsultingType());

      return createSessionOrChat(userRegistrationDTO, user,
          extendedConsultingTypeResponseDTO, rocketChatCredentials);
    } catch (MissingConsultingTypeException | IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }
  }

  /**
   * Initializes the new consulting type settings and creates a session or a chat-agency relation
   * depending on its type. This method should be used for new user account registrations.
   *
   * @param userRegistrationDTO               {@link UserRegistrationDTO}
   * @param user                              {@link User}
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   */
  public void initializeNewConsultingType(UserRegistrationDTO userRegistrationDTO, User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO) {

    createSessionOrChat(userRegistrationDTO, user, extendedConsultingTypeResponseDTO, null);
  }

  private NewRegistrationResponseDto createSessionOrChat(UserRegistrationDTO userRegistrationDTO,
      User user, ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO,
      RocketChatCredentials rocketChatCredentials) {

    if (isNotBlank(userRegistrationDTO.getConsultantId())) {
      return createSessionFacade.createDirectUserSession(userRegistrationDTO.getConsultantId(),
          fromUserRegistrationDTO(userRegistrationDTO), user, extendedConsultingTypeResponseDTO);
    }

    Long sessionId = null;

    var groupChat = extendedConsultingTypeResponseDTO.getGroupChat();
    if (nonNull(groupChat) && isTrue(groupChat.getIsGroupChat())) {
      createUserChatRelationFacade
          .initializeUserChatAgencyRelation(fromUserRegistrationDTO(userRegistrationDTO), user,
              rocketChatCredentials);
    } else {
      sessionId = createSessionFacade
          .createUserSession(fromUserRegistrationDTO(userRegistrationDTO), user,
              extendedConsultingTypeResponseDTO);
    }

    return new NewRegistrationResponseDto()
        .sessionId(sessionId)
        .status(HttpStatus.CREATED);
  }

  private UserDTO fromUserRegistrationDTO(UserRegistrationDTO userRegistrationDTO) {
    if (userRegistrationDTO instanceof NewRegistrationDto) {
      var userDTO = new UserDTO();
      userDTO.setAgencyId(userRegistrationDTO.getAgencyId());
      userDTO.setPostcode(userRegistrationDTO.getPostcode());
      userDTO.setConsultingType(userRegistrationDTO.getConsultingType());
      return userDTO;
    }

    return (UserDTO) userRegistrationDTO;
  }

}

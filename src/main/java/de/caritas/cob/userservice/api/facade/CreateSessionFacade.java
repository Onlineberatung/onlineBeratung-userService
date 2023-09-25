package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.SessionDataProvider.fromUserDTO;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NewRegistrationResponseDto;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.helper.AgencyVerifier;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserAccountService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Facade to encapsulate the steps to initialize a new session. */
@Service
@RequiredArgsConstructor
public class CreateSessionFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull AgencyVerifier agencyVerifier;
  private final @NonNull SessionDataService sessionDataService;
  private final @NonNull RollbackFacade rollbackFacade;
  private final @NonNull UserAccountService userAccountProvider;

  /**
   * Creates a new session for the provided user.
   *
   * @param userDTO {@link UserDTO}
   * @param user {@link User}
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   */
  public Long createUserSession(
      UserDTO userDTO,
      User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO) {

    checkIfAlreadyRegisteredToConsultingType(user, extendedConsultingTypeResponseDTO.getId());
    var agencyDTO = obtainVerifiedAgency(userDTO, extendedConsultingTypeResponseDTO);
    var session = initializeSession(userDTO, user, agencyDTO);

    return session != null ? session.getId() : null;
  }

  /**
   * Creates a new session for the provided user and assignes it to given consultant.
   *
   * @param userDTO {@link UserDTO}
   * @param user {@link User}
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   */
  public NewRegistrationResponseDto createDirectUserSession(
      String consultantId,
      UserDTO userDTO,
      User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO) {
    var consultant = userAccountProvider.retrieveValidatedConsultantById(consultantId);

    var existingSession =
        sessionService.findSessionByConsultantAndUserAndConsultingType(
            consultant, user, extendedConsultingTypeResponseDTO.getId());
    if (existingSession.isPresent()) {
      var session = existingSession.get();
      return new NewRegistrationResponseDto()
          .sessionId(session.getId())
          .rcGroupId(session.getGroupId())
          .status(HttpStatus.CONFLICT);
    }

    return initializeNewDirectSession(userDTO, user, extendedConsultingTypeResponseDTO, consultant);
  }

  private NewRegistrationResponseDto initializeNewDirectSession(
      UserDTO userDTO,
      User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO,
      Consultant consultant) {
    var agencyDTO = obtainVerifiedAgency(userDTO, extendedConsultingTypeResponseDTO);
    var session =
        sessionService.initializeDirectSession(
            consultant, user, userDTO, agencyDTO.getTeamAgency());
    sessionDataService.saveSessionData(session, fromUserDTO(userDTO));
    session.setConsultant(consultant);
    sessionService.saveSession(session);

    return new NewRegistrationResponseDto().sessionId(session.getId()).status(HttpStatus.CREATED);
  }

  private Session initializeSession(UserDTO userDTO, User user, AgencyDTO agencyDTO) {
    var sessionData = fromUserDTO(userDTO);
    var isTeaming = isTrue(agencyDTO.getTeamAgency());
    boolean initialized = false;

    try {
      var session = sessionService.initializeSession(user, userDTO, isTeaming);
      initialized = nonNull(session) && nonNull(session.getId());
      sessionDataService.saveSessionData(session, sessionData);

      return session;
    } catch (Exception ex) {
      rollbackFacade.rollBackUserAccount(
          RollbackUserAccountInformation.builder()
              .userId(user.getUserId())
              .user(user)
              .rollBackUserAccount(Boolean.parseBoolean(userDTO.getTermsAccepted()))
              .build());

      var stepWord = initialized ? "save session data" : "initialize session";
      var message = String.format("Could not %s for user %s.", stepWord, user.getUsername());
      throw new InternalServerErrorException(message, ex);
    }
  }

  private AgencyDTO obtainVerifiedAgency(
      UserDTO userDTO, ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO) {
    var agencyDTO =
        agencyVerifier.getVerifiedAgency(
            userDTO.getAgencyId(), requireNonNull(extendedConsultingTypeResponseDTO.getId()));

    if (isNull(agencyDTO)) {
      throw new BadRequestException(
          String.format(
              "Agency %s is not assigned to given consulting type %d",
              userDTO.getAgencyId(), extendedConsultingTypeResponseDTO.getId()));
    }

    return agencyDTO;
  }

  private void checkIfAlreadyRegisteredToConsultingType(User user, int consultingTypeId) {
    List<Session> sessions =
        sessionService.getSessionsForUserByConsultingTypeId(user, consultingTypeId);

    if (CollectionUtils.isNotEmpty(sessions)) {
      throw new ConflictException(
          String.format(
              "User %s is already registered to consulting type %d",
              user.getUserId(), consultingTypeId));
    }
  }
}

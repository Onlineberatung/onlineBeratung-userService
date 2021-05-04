package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.SessionDataProvider.fromUserDTO;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.helper.AgencyVerifier;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps to initialize a new session.
 */
@Service
@RequiredArgsConstructor
public class CreateSessionFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull AgencyVerifier agencyVerifier;
  private final @NonNull MonitoringService monitoringService;
  private final @NonNull SessionDataService sessionDataService;
  private final @NonNull RollbackFacade rollbackFacade;

  /**
   * Creates a new session for the provided user.
   *
   * @param userDTO                           {@link UserDTO}
   * @param user                              {@link User}
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   */
  public Long createUserSession(UserDTO userDTO, User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO) {

    checkIfAlreadyRegisteredToConsultingType(user, extendedConsultingTypeResponseDTO.getId());
    AgencyDTO agencyDTO = obtainVerifiedAgency(userDTO, extendedConsultingTypeResponseDTO.getId());
    Session session = initializeSession(userDTO, user, extendedConsultingTypeResponseDTO,
        agencyDTO);
    initializeMonitoring(userDTO, user, extendedConsultingTypeResponseDTO, session);

    return session.getId();
  }

  private void initializeMonitoring(UserDTO userDTO, User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO, Session session) {
    try {
      monitoringService.createMonitoringIfConfigured(session, extendedConsultingTypeResponseDTO);
    } catch (CreateMonitoringException exception) {
      rollbackFacade.rollBackUserAccount(RollbackUserAccountInformation.builder()
          .userId(user.getUserId())
          .user(user)
          .rollBackUserAccount(Boolean.parseBoolean(userDTO.getTermsAccepted()))
          .build());

      throw new InternalServerErrorException(String.format(
          "Could not create monitoring for session with id %s. %s",
          session.getId(), exception.getMessage()));
    }
  }

  private Session initializeSession(UserDTO userDTO, User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO, AgencyDTO agencyDTO) {

    try {
      Session session = sessionService
          .initializeSession(user, userDTO, isTrue(agencyDTO.getTeamAgency()),
              extendedConsultingTypeResponseDTO);
      sessionDataService.saveSessionData(session, fromUserDTO(userDTO));

      return session;
    } catch (Exception ex) {
      rollbackFacade.rollBackUserAccount(RollbackUserAccountInformation.builder()
          .userId(user.getUserId())
          .user(user)
          .rollBackUserAccount(Boolean.parseBoolean(userDTO.getTermsAccepted()))
          .build());

      throw new InternalServerErrorException(
          String.format("Could not create session for user %s. %s", user.getUsername(),
              ex.getMessage()));
    }
  }

  private AgencyDTO obtainVerifiedAgency(UserDTO userDTO, int consultingType) {
    AgencyDTO agencyDTO =
        agencyVerifier.getVerifiedAgency(userDTO.getAgencyId(), consultingType);

    if (isNull(agencyDTO)) {
      throw new BadRequestException(
          String.format("Agency %s is not assigned to given consulting type %d",
              userDTO.getAgencyId(), consultingType));
    }

    return agencyDTO;
  }

  private void checkIfAlreadyRegisteredToConsultingType(User user, int consultingTypeId) {
    List<Session> sessions =
        sessionService.getSessionsForUserByConsultingTypeId(user, consultingTypeId);

    if (CollectionUtils.isNotEmpty(sessions)) {
      throw new ConflictException(
          String.format("User %s is already registered to consulting type %d", user.getUserId(),
              consultingTypeId));
    }
  }
}

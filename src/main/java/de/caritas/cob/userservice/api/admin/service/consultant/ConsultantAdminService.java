package de.caritas.cob.userservice.api.admin.service.consultant;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.INITIAL;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_ARCHIVE;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.NEW;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.create.ConsultantCreatorService;
import de.caritas.cob.userservice.api.admin.service.consultant.delete.ConsultantPreDeletionService;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.DistributedTransactionException;
import de.caritas.cob.userservice.api.exception.httpresponses.DistributedTransactionInfo;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/** Service class for admin operations on {@link Consultant} objects. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultantAdminService {

  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull ConsultantCreatorService consultantCreatorService;
  private final @NonNull ConsultantUpdateService consultantUpdateService;
  private final @NonNull ConsultantPreDeletionService consultantPreDeletionService;
  private final @NonNull AppointmentService appointmentService;

  private final @NonNull SessionRepository sessionRepository;

  private final @NonNull AuthenticatedUser authenticatedUser;

  /**
   * Finds a {@link Consultant} by the given consultant id and throws a {@link NoContentException}
   * if no consultant for given id exists.
   *
   * @param consultantId the consultant id to search for
   * @return a generated {@link ConsultantResponseDTO}
   */
  public ConsultantAdminResponseDTO findConsultantById(String consultantId) {
    Consultant consultant =
        this.consultantRepository
            .findByIdAndDeleteDateIsNull(consultantId)
            .orElseThrow(
                () ->
                    new NoContentException(
                        String.format("Consultant with id %s not found", consultantId)));
    return ConsultantResponseDTOBuilder.getInstance(consultant).buildResponseDTO();
  }

  /**
   * Creates a new {@link Consultant} based on the {@link CreateConsultantDTO} input.
   *
   * @param createConsultantDTO the input data used for {@link Consultant} creation
   * @return the generated and persisted {@link Consultant} representation as {@link
   *     ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO createNewConsultant(CreateConsultantDTO createConsultantDTO)
      throws DistributedTransactionException {
    Consultant newConsultant =
        this.consultantCreatorService.createNewConsultant(createConsultantDTO);
    List<TransactionalStep> completedSteps =
        Lists.newArrayList(
            TransactionalStep.CREATE_ACCOUNT_IN_KEYCLOAK,
            TransactionalStep.CREATE_ACCOUNT_IN_ROCKETCHAT,
            TransactionalStep.CREATE_CONSULTANT_IN_MARIADB);

    ConsultantAdminResponseDTO consultantAdminResponseDTO =
        ConsultantResponseDTOBuilder.getInstance(newConsultant).buildResponseDTO();

    try {
      this.appointmentService.createConsultant(consultantAdminResponseDTO);
    } catch (RestClientException e) {
      log.error(
          "User with id {}, who has roles {}, has created a consultant with id {} but the appointment service returned an error: {}",
          authenticatedUser.getUserId(),
          authenticatedUser.getRoles(),
          newConsultant.getId(),
          e.getMessage());
      this.consultantCreatorService.rollbackCreateNewConsultant(newConsultant);
      throw new DistributedTransactionException(
          e,
          new DistributedTransactionInfo(
              "createNewConsultant",
              completedSteps,
              TransactionalStep.CREATE_ACCOUNT_IN_CALCOM_OR_APPOINTMENTSERVICE));
    }

    return consultantAdminResponseDTO;
  }

  /**
   * Updates a new {@link Consultant} based on the {@link UpdateConsultantDTO} input.
   *
   * @param consultantId the id of consultant to be updated
   * @param updateConsultantDTO the input data used for {@link Consultant} update
   * @return the generated and persisted {@link Consultant} representation as {@link
   *     ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO updateConsultant(
      String consultantId, UpdateAdminConsultantDTO updateConsultantDTO) {
    Consultant updatedConsultant =
        this.consultantUpdateService.updateConsultant(consultantId, updateConsultantDTO);

    ConsultantAdminResponseDTO consultantAdminResponseDTO =
        ConsultantResponseDTOBuilder.getInstance(updatedConsultant).buildResponseDTO();

    this.appointmentService.updateConsultant(consultantAdminResponseDTO);
    return consultantAdminResponseDTO;
  }

  /**
   * Marks the {@link Consultant} as deleted.
   *
   * @param consultantId the consultant id
   * @param forceDeleteSessions
   */
  public void markConsultantForDeletion(String consultantId, Boolean forceDeleteSessions) {
    var consultant =
        this.consultantRepository
            .findByIdAndDeleteDateIsNull(consultantId)
            .orElseThrow(
                () -> new NotFoundException("Consultant with id %s does not exist", consultantId));

    this.consultantPreDeletionService.performPreDeletionSteps(consultant, forceDeleteSessions);

    if (Boolean.TRUE.equals(forceDeleteSessions)) {
      deleteAndUnassignSessions(consultant);
      log.info(
          "User with id {}, who has roles {}, has deleted in-progress and archived sessions of consultant with id {}",
          authenticatedUser.getUserId(),
          authenticatedUser.getRoles(),
          consultantId);
    }

    consultant.setDeleteDate(nowInUtc());
    consultant.setStatus(ConsultantStatus.IN_DELETION);
    this.consultantRepository.save(consultant);
  }

  private void deleteAndUnassignSessions(Consultant consultant) {
    deleteSessionsInProgressOrArchived(consultant);
    unassignNewOrInitialSessions(consultant);
  }

  private void unassignNewOrInitialSessions(Consultant consultant) {
    sessionRepository
        .findByConsultantAndStatusIn(consultant, Lists.newArrayList(NEW, INITIAL))
        .forEach(
            session -> {
              session.setConsultant(null);
              sessionRepository.save(session);
            });
  }

  private void deleteSessionsInProgressOrArchived(Consultant consultant) {
    sessionRepository
        .findByConsultantAndStatusIn(consultant, Lists.newArrayList(IN_PROGRESS, IN_ARCHIVE))
        .forEach(sessionRepository::delete);
  }
}

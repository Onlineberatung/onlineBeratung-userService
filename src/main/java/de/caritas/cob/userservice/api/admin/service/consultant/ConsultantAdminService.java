package de.caritas.cob.userservice.api.admin.service.consultant;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.INITIAL;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_ARCHIVE;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.NEW;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.AccountManager;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.create.CreateConsultantSaga;
import de.caritas.cob.userservice.api.admin.service.consultant.delete.ConsultantPreDeletionService;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.DistributedTransactionException;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service class for admin operations on {@link Consultant} objects. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultantAdminService {

  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull CreateConsultantSaga createConsultantSaga;
  private final @NonNull ConsultantUpdateService consultantUpdateService;
  private final @NonNull ConsultantPreDeletionService consultantPreDeletionService;

  private final @NonNull SessionRepository sessionRepository;

  private final @NonNull AuthenticatedUser authenticatedUser;

  private final @NonNull AccountManager accountManager;

  private final @NonNull AppointmentService appointmentService;

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
    var response = ConsultantResponseDTOBuilder.getInstance(consultant).buildResponseDTO();
    enrichWithDisplayName(consultantId, response);
    return response;
  }

  private void enrichWithDisplayName(String consultantId, ConsultantAdminResponseDTO response) {
    accountManager
        .findConsultant(consultantId)
        .ifPresent(map -> response.getEmbedded().setDisplayName(getDisplayNameFromUserMap(map)));
  }

  private static String getDisplayNameFromUserMap(Map<String, Object> map) {
    return map.containsKey("displayName") ? (String) map.get("displayName") : null;
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
    return createConsultantSaga.createNewConsultant(createConsultantDTO);
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

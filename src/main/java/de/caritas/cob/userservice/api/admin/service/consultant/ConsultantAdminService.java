package de.caritas.cob.userservice.api.admin.service.consultant;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.create.ConsultantCreatorService;
import de.caritas.cob.userservice.api.admin.service.consultant.delete.ConsultantPreDeletionService;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service class for admin operations on {@link Consultant} objects. */
@Service
@RequiredArgsConstructor
public class ConsultantAdminService {

  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull ConsultantCreatorService consultantCreatorService;
  private final @NonNull ConsultantUpdateService consultantUpdateService;
  private final @NonNull ConsultantPreDeletionService consultantPreDeletionService;
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
    return ConsultantResponseDTOBuilder.getInstance(consultant).buildResponseDTO();
  }

  /**
   * Creates a new {@link Consultant} based on the {@link CreateConsultantDTO} input.
   *
   * @param createConsultantDTO the input data used for {@link Consultant} creation
   * @return the generated and persisted {@link Consultant} representation as {@link
   *     ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO createNewConsultant(CreateConsultantDTO createConsultantDTO) {
    Consultant newConsultant =
        this.consultantCreatorService.createNewConsultant(createConsultantDTO);

    ConsultantAdminResponseDTO consultantAdminResponseDTO =
        ConsultantResponseDTOBuilder.getInstance(newConsultant).buildResponseDTO();

    this.appointmentService.createConsultant(consultantAdminResponseDTO);

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
   */
  public void markConsultantForDeletion(String consultantId) {
    var consultant =
        this.consultantRepository
            .findByIdAndDeleteDateIsNull(consultantId)
            .orElseThrow(
                () -> new NotFoundException("Consultant with id %s does not exist", consultantId));

    this.consultantPreDeletionService.performPreDeletionSteps(consultant);

    this.appointmentService.deleteConsultant(consultant.getId());

    consultant.setDeleteDate(nowInUtc());
    consultant.setStatus(ConsultantStatus.IN_DELETION);
    this.consultantRepository.save(consultant);
  }
}

package de.caritas.cob.userservice.api.admin.service.consultant;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.admin.service.consultant.create.ConsultantCreatorService;
import de.caritas.cob.userservice.api.admin.service.consultant.delete.ConsultantPreDeletionService;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class for admin operations on {@link Consultant} objects.
 */
@Service
@RequiredArgsConstructor
public class ConsultantAdminService {

  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull ConsultantCreatorService consultantCreatorService;
  private final @NonNull ConsultantUpdateService consultantUpdateService;
  private final @NonNull ConsultantPreDeletionService consultantPreDeletionService;

  /**
   * Finds a {@link Consultant} by the given consultant id and throws a {@link NoContentException}
   * if no consultant for given id exists.
   *
   * @param consultantId the consultant id to search for
   * @return a generated {@link ConsultantResponseDTO}
   */
  public ConsultantAdminResponseDTO findConsultantById(String consultantId) {
    Consultant consultant = this.consultantRepository.findByIdAndDeleteDateIsNull(consultantId)
        .orElseThrow(() -> new NoContentException(
            String.format("Consultant with id %s not found", consultantId)));
    return ConsultantResponseDTOBuilder.getInstance(consultant)
        .buildResponseDTO();
  }

  /**
   * Creates a new {@link Consultant} based on the {@link CreateConsultantDTO} input.
   *
   * @param createConsultantDTO the input data used for {@link Consultant} creation
   * @return the generated and persisted {@link Consultant} representation as {@link
   * ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO createNewConsultant(CreateConsultantDTO createConsultantDTO) {
    Consultant newConsultant =
        this.consultantCreatorService.createNewConsultant(createConsultantDTO);

    return ConsultantResponseDTOBuilder.getInstance(newConsultant)
        .buildResponseDTO();
  }

  /**
   * Updates a new {@link Consultant} based on the {@link UpdateConsultantDTO} input.
   *
   * @param consultantId        the id of consultant to be updated
   * @param updateConsultantDTO the input data used for {@link Consultant} update
   * @return the generated and persisted {@link Consultant} representation as {@link
   * ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO updateConsultant(String consultantId,
      UpdateAdminConsultantDTO updateConsultantDTO) {
    Consultant updatedConsultant = this.consultantUpdateService.updateConsultant(consultantId,
        updateConsultantDTO);

    return ConsultantResponseDTOBuilder.getInstance(updatedConsultant)
        .buildResponseDTO();
  }

  /**
   * Marks the {@link Consultant} as deleted.
   *
   * @param consultantId the consultant id
   */
  public void markConsultantForDeletion(String consultantId) {
    Consultant consultant = this.consultantRepository.findByIdAndDeleteDateIsNull(consultantId)
        .orElseThrow(() -> new NotFoundException(
            String.format("Consultant with id %s does not exist", consultantId)));

    this.consultantPreDeletionService.performPreDeletionSteps(consultant);

    consultant.setDeleteDate(nowInUtc());
    this.consultantRepository.save(consultant);
  }
}

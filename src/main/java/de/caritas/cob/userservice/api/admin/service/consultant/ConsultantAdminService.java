package de.caritas.cob.userservice.api.admin.service.consultant;

import de.caritas.cob.userservice.api.admin.service.consultant.create.ConsultantCreatorService;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.model.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
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

  /**
   * Finds a {@link Consultant} by the given consultant id and throws a {@link NoContentException}
   * if no consultant for given id exists.
   *
   * @param consultantId the consultant id to search for
   * @return a generated {@link ConsultantResponseDTO}
   */
  public ConsultantAdminResponseDTO findConsultantById(String consultantId) {
    Consultant consultant = this.consultantRepository.findById(consultantId)
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

}

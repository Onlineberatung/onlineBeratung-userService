package de.caritas.cob.userservice.api.facade.userdata;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.model.AbsenceDTO;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultantDataFacade {

  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull ConsultantUpdateService consultantUpdateService;
  private final @NonNull ConsultantService consultantService;

  /**
   * Updates a {@link Consultant} with the absence data from a (@Link AbsenceDTO).
   *
   * @param absence {@link AbsenceDTO}
   */
  public Consultant updateConsultantAbsent(Consultant consultant, AbsenceDTO absence) {
    consultant.setAbsent(isTrue(absence.getAbsent()));

    if (isNotBlank(absence.getMessage())) {
      consultant.setAbsenceMessage(Helper.removeHTMLFromText(absence.getMessage()));
    } else {
      consultant.setAbsenceMessage(null);
    }
    return this.consultantService.saveConsultant(consultant);
  }

  /**
   * Updates the authenticated consultant with the provided {@link UpdateConsultantDTO} data.
   *
   * @param updateConsultantDTO the {@link UpdateConsultantDTO}
   */
  public void updateConsultantData(UpdateConsultantDTO updateConsultantDTO) {
    String consultantId = this.authenticatedUser.getUserId();
    Consultant consultant = this.consultantService.getConsultant(consultantId)
        .orElseThrow(() -> new NotFoundException(String.format("Consultant with id %s not found",
            consultantId)));

    UpdateAdminConsultantDTO updateAdminConsultantDTO = new UpdateAdminConsultantDTO()
        .email(updateConsultantDTO.getEmail())
        .firstname(updateConsultantDTO.getFirstname())
        .lastname(updateConsultantDTO.getLastname())
        .formalLanguage(consultant.isLanguageFormal())
        .absent(consultant.isAbsent())
        .absenceMessage(consultant.getAbsenceMessage());

    this.consultantUpdateService.updateConsultant(consultantId, updateAdminConsultantDTO);
  }
}

package de.caritas.cob.userservice.api.facade.userdata;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.model.AbsenceDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultantDataFacade {

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
}

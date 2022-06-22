package de.caritas.cob.userservice.api.admin.service.consultant.validation;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Adapter class to provide a {@link AbsenceInputValidation} by given {@link CreateConsultantDTO}.
 */
@RequiredArgsConstructor
public class CreateConsultantDTOAbsenceInputAdapter implements AbsenceInputValidation {

  private final @NonNull CreateConsultantDTO createConsultantDTO;

  /**
   * Returnes if absent is set to true.
   *
   * @return true if absent is not false and null
   */
  @Override
  public boolean isAbsent() {
    return isTrue(createConsultantDTO.getAbsent());
  }

  /**
   * Returnes the absence message.
   *
   * @return the absence message
   */
  @Override
  public String absenceMessage() {
    return this.createConsultantDTO.getAbsenceMessage();
  }
}

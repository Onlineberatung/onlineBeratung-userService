package de.caritas.cob.userservice.api.admin.service.consultant.create;

import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateConsultantAgencyDTOInputAdapter implements ConsultantAgencyCreationInput {

  private final @NonNull String consultantId;
  private final @NonNull CreateConsultantAgencyDTO createConsultantAgencyDTO;

  @Override
  public String getConsultantId() {
    return this.consultantId;
  }

  @Override
  public String getRole() {
    return this.createConsultantAgencyDTO.getRole();
  }

  @Override
  public Long getAgencyId() {
    return this.createConsultantAgencyDTO.getAgencyId();
  }

  @Override
  public LocalDateTime getCreateDate() {
    return LocalDateTime.now();
  }

  @Override
  public LocalDateTime getUpdateDate() {
    return LocalDateTime.now();
  }
}

package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImportRecordAgencyCreationInputAdapter implements ConsultantAgencyCreationInput {

  private final @NonNull String consultantID;
  private final @NonNull Long agencyId;
  private final @NonNull Set<String> roleSetNames;

  @Override
  public String getConsultantId() {
    return this.consultantID;
  }

  @Override
  public Set<String> getRoleSetNames() {
    return this.roleSetNames;
  }

  @Override
  public Long getAgencyId() {
    return this.agencyId;
  }
}

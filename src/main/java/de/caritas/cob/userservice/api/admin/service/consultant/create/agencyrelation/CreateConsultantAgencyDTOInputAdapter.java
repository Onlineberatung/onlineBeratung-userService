package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static org.hibernate.search.util.impl.CollectionHelper.asSet;

import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import java.util.Set;
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
  public Set<String> getRoleSetNames() {
    return asSet(this.createConsultantAgencyDTO.getRoleSetKey());
  }

  @Override
  public Long getAgencyId() {
    return this.createConsultantAgencyDTO.getAgencyId();
  }
}

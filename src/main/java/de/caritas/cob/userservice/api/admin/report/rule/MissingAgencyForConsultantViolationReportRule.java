package de.caritas.cob.userservice.api.admin.report.rule;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import de.caritas.cob.userservice.api.model.AdditionalInformationDTO;
import de.caritas.cob.userservice.api.model.ViolationDTO;
import de.caritas.cob.userservice.api.model.ViolationDTO.ViolationTypeEnum;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissingAgencyForConsultantViolationReportRule implements ViolationReportRule {

  private final @NonNull ConsultantRepository consultantRepository;

  @Override
  public List<ViolationDTO> generateViolations() {
    return StreamSupport.stream(this.consultantRepository.findAll().spliterator(), true)
        .filter(consultant -> isEmpty(consultant.getConsultantAgencies()))
        .map(this::fromConsultant)
        .collect(Collectors.toList());
  }

  private ViolationDTO fromConsultant(Consultant consultant) {
    return new ViolationDTO()
        .violationType(ViolationTypeEnum.CONSULTANT)
        .identifier(consultant.getId())
        .reason("Missing agency assignment for consultant")
        .addAdditionalInformationItem(additionalInformation("Username", consultant.getUsername()))
        .addAdditionalInformationItem(additionalInformation("Email", consultant.getEmail()));
  }

  private AdditionalInformationDTO additionalInformation(String key, String value) {
    return new AdditionalInformationDTO()
        .name(key)
        .value(value);
  }

}

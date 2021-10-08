package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.api.model.AgencyAdminFullResponseDTO;
import de.caritas.cob.userservice.api.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder to create a {@link ConsultantAgencyAdminResultDTO}.
 */
public class ConsultantAgencyAdminResultDTOBuilder implements HalLinkBuilder {

  private List<de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO> agencyList;

  private ConsultantAgencyAdminResultDTOBuilder() {
  }

  /**
   * Creates an {@link ConsultantAgencyAdminResultDTOBuilder} instance.
   *
   * @return a instance of {@link ConsultantAgencyAdminResultDTOBuilder}
   */
  public static ConsultantAgencyAdminResultDTOBuilder getInstance() {
    return new ConsultantAgencyAdminResultDTOBuilder();
  }

  /**
   * Sets the result param.
   *
   * @param agencyList the repository result for building links and embedded objects
   * @return the current {@link ConsultantAgencyAdminResultDTOBuilder}
   */
  public ConsultantAgencyAdminResultDTOBuilder withResult(
      List<de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO> agencyList) {
    this.agencyList = agencyList;
    return this;
  }

  /**
   * Creates the {@link ConsultantAgencyAdminResultDTO}.
   *
   * @return the generated {@link ConsultantAgencyAdminResultDTO}
   */
  public List<AgencyAdminFullResponseDTO> build() {
    return nonNullConsultantAgencies().stream()
        .map(this::fromAgency)
        .collect(Collectors.toList());
  }

  private List<de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO> nonNullConsultantAgencies() {
    return nonNull(this.agencyList) ? this.agencyList : emptyList();
  }

  private AgencyAdminFullResponseDTO fromAgency(
      de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO agency) {
    return new AgencyAdminFullResponseDTO()
        .embedded(new AgencyAdminResponseDTO()
            .id(agency.getId())
            .name(agency.getName())
            .dioceseId(agency.getDioceseId())
            .teamAgency(agency.getTeamAgency())
            .offline(agency.getOffline())
            .postcode(agency.getPostcode())
            .description(agency.getDescription())
            .external(agency.getExternal())
            .consultingType(agency.getConsultingType())
            .city(agency.getCity())
            .url(agency.getUrl())
            .createDate(agency.getCreateDate())
            .updateDate(agency.getUpdateDate())
            .deleteDate(agency.getDeleteDate())
        );
  }

}

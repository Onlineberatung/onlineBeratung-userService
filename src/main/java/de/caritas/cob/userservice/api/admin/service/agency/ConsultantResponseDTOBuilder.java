package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminFullResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyLinks;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;
import java.util.List;
import java.util.stream.Collectors;

/** Builder to create a list of{@link AgencyAdminFullResponseDTO}. */
public class ConsultantResponseDTOBuilder implements HalLinkBuilder {

  private List<
          de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO>
      agencyList;
  private String consultantId;

  private ConsultantResponseDTOBuilder() {}

  /**
   * Creates an {@link ConsultantResponseDTOBuilder} instance.
   *
   * @return a instance of {@link ConsultantResponseDTOBuilder}
   */
  public static ConsultantResponseDTOBuilder getInstance() {
    return new ConsultantResponseDTOBuilder();
  }

  /**
   * Sets the result param.
   *
   * @param agencyList the repository result for building links and embedded objects
   * @return the current {@link ConsultantResponseDTOBuilder}
   */
  public ConsultantResponseDTOBuilder withResult(
      List<de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO>
          agencyList) {
    this.agencyList = agencyList;
    return this;
  }

  /**
   * Sets the id of the consultant.
   *
   * @param consultantId the id of the consultant needed for the hal self link
   * @return the current {@link ConsultantResponseDTOBuilder}
   */
  public ConsultantResponseDTOBuilder withConsultantId(String consultantId) {
    this.consultantId = consultantId;
    return this;
  }

  /**
   * Creates the list of {@link AgencyAdminFullResponseDTO} and wrappes it into a hal conform {@link
   * ConsultantAgencyResponseDTO}.
   *
   * @return the generated {@link ConsultantAgencyResponseDTO}
   */
  public ConsultantAgencyResponseDTO build() {
    var agencies =
        nonNullConsultantAgencies().stream().map(this::fromAgency).collect(Collectors.toList());
    return new ConsultantAgencyResponseDTO()
        .total(agencies.size())
        .embedded(agencies)
        .links(buildConsultantAgencyLinks());
  }

  private AgencyLinks buildConsultantAgencyLinks() {
    return new AgencyLinks()
        .self(
            buildHalLink(
                methodOn(UseradminApi.class).getConsultantAgencies(this.consultantId),
                MethodEnum.GET));
  }

  private List<
          de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO>
      nonNullConsultantAgencies() {
    return nonNull(this.agencyList) ? this.agencyList : emptyList();
  }

  private AgencyAdminFullResponseDTO fromAgency(
      de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO
          agency) {
    return new AgencyAdminFullResponseDTO()
        .embedded(
            new AgencyAdminResponseDTO()
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
                .deleteDate(agency.getDeleteDate()));
  }
}

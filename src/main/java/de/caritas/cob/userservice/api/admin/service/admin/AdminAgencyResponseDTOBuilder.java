package de.caritas.cob.userservice.api.admin.service.admin;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminAgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminFullResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyLinks;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAgencyResponseDTOBuilder implements HalLinkBuilder {
  private String adminId;
  private List<
          de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO>
      agencyList;

  public static AdminAgencyResponseDTOBuilder getInstance() {
    return new AdminAgencyResponseDTOBuilder();
  }

  public AdminAgencyResponseDTOBuilder withAdminId(final String adminId) {
    this.adminId = adminId;
    return this;
  }

  public AdminAgencyResponseDTOBuilder withAgencies(
      final List<
              de.caritas.cob.userservice.agencyadminserivce.generated.web.model
                  .AgencyAdminResponseDTO>
          agencyList) {
    this.agencyList = agencyList;
    return this;
  }

  public AdminAgencyResponseDTO build() {
    var agencies =
        emptyIfNull(agencyList).stream().map(this::fromAgency).collect(Collectors.toList());
    return new AdminAgencyResponseDTO()
        .total(agencies.size())
        .embedded(agencies)
        .links(buildAdminAgencyLinks());
  }

  private AgencyLinks buildAdminAgencyLinks() {
    return new AgencyLinks()
        .self(
            buildHalLink(
                methodOn(UseradminApi.class).getAdminAgencies(this.adminId),
                HalLink.MethodEnum.GET));
  }

  private AgencyAdminFullResponseDTO fromAgency(
      final de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO
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

package de.caritas.cob.userservice.api.admin.service.agency;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAgencyLinks;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;
import java.util.List;

/**
 * Builder class to generate a {@link AgencyConsultantResponseDTO} containing available hal links
 * and result of {@link ConsultantAdminResponseDTO} elements.
 */
public class AgencyConsultantResponseDTOBuilder implements HalLinkBuilder {

  private final List<ConsultantAdminResponseDTO> consultants;
  private String agencyId;

  private AgencyConsultantResponseDTOBuilder(
      List<ConsultantAdminResponseDTO> consultantAdminResponseDTOS) {
    this.consultants = consultantAdminResponseDTOS;
  }

  /**
   * Creates the {@link AgencyConsultantResponseDTOBuilder} instance.
   *
   * @param consultants the source consultants
   * @return a instance of {@link AgencyConsultantResponseDTOBuilder}
   */
  public static AgencyConsultantResponseDTOBuilder getInstance(
      List<ConsultantAdminResponseDTO> consultants) {
    return new AgencyConsultantResponseDTOBuilder(consultants);
  }

  /**
   * Sets the agency id needed for generation of the hal link.
   *
   * @param agencyId the agency id
   * @return the current {@link AgencyConsultantResponseDTOBuilder}
   */
  public AgencyConsultantResponseDTOBuilder withAgencyId(String agencyId) {
    this.agencyId = agencyId;
    return this;
  }

  /**
   * Generates the {@link AgencyConsultantResponseDTO} containing the {@link
   * ConsultantAdminResponseDTO} resources and hal self link.
   *
   * @return the generated {@link ConsultantAdminResponseDTO}
   */
  public AgencyConsultantResponseDTO build() {
    return new AgencyConsultantResponseDTO()
        .total(consultants.size())
        .embedded(consultants)
        .links(buildConsultantAgencyLinks());
  }

  private ConsultantAgencyLinks buildConsultantAgencyLinks() {
    return new ConsultantAgencyLinks()
        .self(
            buildHalLink(
                methodOn(UseradminApi.class).getAgencyConsultants(this.agencyId), MethodEnum.GET));
  }
}

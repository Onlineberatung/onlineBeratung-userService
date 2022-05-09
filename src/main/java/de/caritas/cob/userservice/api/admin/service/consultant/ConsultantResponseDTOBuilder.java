package de.caritas.cob.userservice.api.admin.service.consultant;

import static java.util.Objects.requireNonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.api.admin.mapper.ConsultantAdminMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantLinks;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;

/**
 * Builder class to generate a {@link ConsultantAdminResponseDTO} containing available hal links and
 * result of {@link ConsultantDTO} element.
 */
public class ConsultantResponseDTOBuilder implements HalLinkBuilder {

  private final Consultant consultant;

  private ConsultantResponseDTOBuilder(Consultant consultant) {
    this.consultant = requireNonNull(consultant);
  }

  /**
   * Creates the {@link ConsultantResponseDTOBuilder} instance.
   *
   * @param consultant the source consultant
   * @return a instance of {@link ConsultantResponseDTOBuilder}
   */
  public static ConsultantResponseDTOBuilder getInstance(Consultant consultant) {
    return new ConsultantResponseDTOBuilder(consultant);
  }

  /**
   * Generates the {@link ConsultantAdminResponseDTO} containing the {@link ConsultantDTO} resource
   * and navigation hal links.
   *
   * @return the generated {@link ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO buildResponseDTO() {
    var consultantDTO = new ConsultantAdminMapper(this.consultant).mapData();
    var consultantLinks = new ConsultantLinks()
        .self(buildSelfLink())
        .update(buildUpdateLink())
        .delete(buildDeleteLink())
        .agencies(buildAgenciesLink())
        .addAgency(buildAddAgencyLink());

    return new ConsultantAdminResponseDTO()
        .embedded(consultantDTO)
        .links(consultantLinks);
  }

  private HalLink buildSelfLink() {
    return buildHalLink(methodOn(UseradminApi.class)
        .getConsultant(this.consultant.getId()), MethodEnum.GET);
  }

  private HalLink buildUpdateLink() {
    return buildHalLink(methodOn(UseradminApi.class)
        .updateConsultant(this.consultant.getId(), null), MethodEnum.PUT);
  }

  private HalLink buildDeleteLink() {
    return buildHalLink(methodOn(UseradminApi.class)
        .markConsultantForDeletion(this.consultant.getId()), MethodEnum.DELETE);
  }

  private HalLink buildAgenciesLink() {
    return buildHalLink(methodOn(UseradminApi.class)
        .getConsultantAgencies(this.consultant.getId()), MethodEnum.GET);
  }

  private HalLink buildAddAgencyLink() {
    return buildHalLink(methodOn(UseradminApi.class)
        .createConsultantAgency(this.consultant.getId(), null), MethodEnum.POST);
  }

}

package de.caritas.cob.userservice.api.admin.service.consultant;

import static java.util.Objects.requireNonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.api.admin.mapper.ConsultantAdminMapper;
import de.caritas.cob.userservice.api.model.ConsultantDTO;
import de.caritas.cob.userservice.api.model.GetConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.GetLinks;
import de.caritas.cob.userservice.api.model.HalLink;
import de.caritas.cob.userservice.api.model.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.generated.api.admin.controller.UseradminApi;

/**
 * Builder class to generate a {@link GetConsultantResponseDTO} containing available hal links
 * and result of {@link ConsultantDTO} element.
 */
public class GetConsultantDTOBuilder implements HalLinkBuilder {

  private final Consultant consultant;

  private GetConsultantDTOBuilder(Consultant consultant) {
    this.consultant = requireNonNull(consultant);
  }

  /**
   * Creates the {@link GetConsultantDTOBuilder} instance.
   *
   * @param consultant the source consultant
   * @return a instance of {@link GetConsultantDTOBuilder}
   */
  public static GetConsultantDTOBuilder getInstance(Consultant consultant) {
    return new GetConsultantDTOBuilder(consultant);
  }

  /**
   * Generates the {@link GetConsultantResponseDTO} containing the {@link ConsultantDTO} resource
   * and navigation hal links.
   *
   * @return the generated {@link GetConsultantResponseDTO}
   */
  public GetConsultantResponseDTO buildResponseDTO() {
    ConsultantDTO consultantDTO = new ConsultantAdminMapper(this.consultant).mapData();
    GetLinks getLinks = new GetLinks()
        .self(buildSelfLink())
        .update(buildUpdateLink())
        .delete(buildDeleteLink());

    return new GetConsultantResponseDTO()
        .embedded(consultantDTO)
        .links(getLinks);
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

}

package de.caritas.cob.userservice.api.admin.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.admin.controller.UserAdminController;
import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultLinks;
import de.caritas.cob.userservice.api.model.HalLink;
import de.caritas.cob.userservice.api.model.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder to create a {@link ConsultantAgencyAdminResultDTO}.
 */
public class ConsultantAgencyAdminResultDTOBuilder implements HalLinkBuilder {

  private List<ConsultantAgency> agencyList;
  private String consultantId;

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
   * Sets the consultantId.
   *
   * @param consultantId for building links
   * @return the current {@link ConsultantAgencyAdminResultDTOBuilder}
   */
  public ConsultantAgencyAdminResultDTOBuilder withConsultantId(String consultantId) {
    this.consultantId = consultantId;
    return this;
  }

  /**
   * Sets the result param.
   *
   * @param agencyList the repository result for building links and embedded objects
   * @return the current {@link ConsultantAgencyAdminResultDTOBuilder}
   */
  public ConsultantAgencyAdminResultDTOBuilder withResult(List<ConsultantAgency> agencyList) {
    this.agencyList = agencyList;
    return this;
  }

  /**
   * Creates the {@link ConsultantAgencyAdminResultDTO}.
   *
   * @return the generated {@link ConsultantAgencyAdminResultDTO}
   */
  public ConsultantAgencyAdminResultDTO build() {
    return new ConsultantAgencyAdminResultDTO()
        .embedded(buildSessionAdminResult())
        .links(buildResultLinks());
  }

  private List<ConsultantAgencyAdminDTO> buildSessionAdminResult() {
    if (isNull(this.agencyList)) {
      return emptyList();
    }
    return this.agencyList.stream()
        .map(this::fromConsultantAgency)
        .collect(Collectors.toList());
  }

  private ConsultantAgencyAdminDTO fromConsultantAgency(ConsultantAgency consultantAgency) {

    return new ConsultantAgencyAdminDTO()
        .agencyId(consultantAgency.getAgencyId())
        .consultantId(consultantAgency.getConsultant().getId())
        .createDate(String.valueOf(consultantAgency.getCreateDate()))
        .updateDate((String.valueOf(consultantAgency.getUpdateDate())));
  }

  private ConsultantAgencyAdminResultLinks buildResultLinks() {
    return new ConsultantAgencyAdminResultLinks()
        .self(buildSelfLink());
  }

  private HalLink buildSelfLink() {
    return buildHalLink(methodOn(UserAdminController.class).getConsultantAgency(consultantId),
        MethodEnum.GET);
  }

}

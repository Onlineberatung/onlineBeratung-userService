package de.caritas.cob.userservice.api.admin.service.admin;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminLinks;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.admin.hallink.HalLinkBuilder;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdminResponseDTOBuilder implements HalLinkBuilder {

  private final Admin admin;

  public static AdminResponseDTOBuilder getInstance(final Admin admin) {
    return new AdminResponseDTOBuilder(admin);
  }

  public AdminResponseDTO buildResponseDTO() {
    return new AdminResponseDTO().embedded(buildAdminDTO()).links(buildLinks());
  }

  private AdminLinks buildLinks() {
    return new AdminLinks()
        .self(buildSelfLink())
        .update(buildUpdateLink())
        .delete(buildDeleteLink())
        .agencies(buildAgenciesLink());
  }

  private HalLink buildDeleteLink() {
    return buildHalLink(
        methodOn(UseradminApi.class).deleteAgencyAdmin(this.admin.getId()),
        HalLink.MethodEnum.DELETE);
  }

  private HalLink buildUpdateLink() {
    return buildHalLink(
        methodOn(UseradminApi.class).updateAgencyAdmin(this.admin.getId(), null),
        HalLink.MethodEnum.PUT);
  }

  private HalLink buildSelfLink() {
    return buildHalLink(
        methodOn(UseradminApi.class).getAgencyAdmin(this.admin.getId()), HalLink.MethodEnum.GET);
  }

  private HalLink buildAgenciesLink() {
    return buildHalLink(
        methodOn(UseradminApi.class).getAdminAgencies(this.admin.getId()), HalLink.MethodEnum.GET);
  }

  private AdminDTO buildAdminDTO() {
    return new AdminDTO()
        .id(admin.getId())
        .username(admin.getUsername())
        .firstname(admin.getFirstName())
        .lastname(admin.getLastName())
        .email(admin.getEmail())
        .createDate(String.valueOf(admin.getCreateDate()))
        .updateDate(String.valueOf(admin.getUpdateDate()));
  }
}

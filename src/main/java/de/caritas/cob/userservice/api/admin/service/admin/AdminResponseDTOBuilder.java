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

  public AdminResponseDTO buildAgencyAdminResponseDTO() {
    return new AdminResponseDTO()
        .embedded(buildAdminDTO())
        .links(buildLinks(Admin.AdminType.AGENCY));
  }

  private AdminLinks buildLinks(Admin.AdminType adminType) {
    return new AdminLinks()
        .self(buildSelfLink(adminType))
        .update(buildUpdateLink(adminType))
        .delete(buildDeleteLink(adminType))
        .agencies(buildAgenciesLink());
  }

  private HalLink buildDeleteLink(Admin.AdminType adminType) {
    switch (adminType) {
      case TENANT:
        return buildHalLink(
            methodOn(UseradminApi.class).deleteTenantAdmin(this.admin.getId()),
            HalLink.MethodEnum.DELETE);
      default:
        return buildHalLink(
            methodOn(UseradminApi.class).deleteAgencyAdmin(this.admin.getId()),
            HalLink.MethodEnum.DELETE);
    }
  }

  private HalLink buildUpdateLink(Admin.AdminType adminType) {
    switch (adminType) {
      case TENANT:
        return buildHalLink(
            methodOn(UseradminApi.class).updateTenantAdmin(this.admin.getId(), null),
            HalLink.MethodEnum.PUT);
      default:
        return buildHalLink(
            methodOn(UseradminApi.class).updateAgencyAdmin(this.admin.getId(), null),
            HalLink.MethodEnum.PUT);
    }
  }

  private HalLink buildSelfLink(Admin.AdminType adminType) {

    switch (adminType) {
      case TENANT:
        return buildHalLink(
            methodOn(UseradminApi.class).getTenantAdmin(this.admin.getId()),
            HalLink.MethodEnum.GET);
      default:
        return buildHalLink(
            methodOn(UseradminApi.class).getAgencyAdmin(this.admin.getId()),
            HalLink.MethodEnum.GET);
    }
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
        .tenantId(String.valueOf(admin.getTenantId()))
        .createDate(String.valueOf(admin.getCreateDate()))
        .updateDate(String.valueOf(admin.getUpdateDate()));
  }
}

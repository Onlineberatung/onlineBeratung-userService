package de.caritas.cob.userservice.api.admin.hallink;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.adapters.web.controller.UserAdminController;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.RootDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.RootLinks;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;

/** Builder to create the root navigation hal DTO. */
public class RootDTOBuilder implements HalLinkBuilder {

  public static final Integer DEFAULT_PAGE = 1;
  public static final Integer DEFAULT_PER_PAGE = 20;

  /**
   * Builds the root navigation DTO.
   *
   * @return the {@link RootDTO} containing hal links
   */
  public RootDTO buildRootDTO() {
    return new RootDTO()
        .links(
            new RootLinks()
                .self(buildSelfLink())
                .sessions(buildSessionsLink())
                .consultants(buildConsultantsLink())
                .consultantAgencies(buildConsultantAgenciesLink())
                .createConsultant(buildCreateConsultantLink())
                .deleteAsker(buildDeleteAskerLink()));
  }

  private HalLink buildSelfLink() {
    return buildHalLink(methodOn(UserAdminController.class).getRoot(), MethodEnum.GET);
  }

  private HalLink buildSessionsLink() {
    return buildHalLink(
        methodOn(UseradminApi.class).getSessions(DEFAULT_PAGE, DEFAULT_PER_PAGE, null),
        MethodEnum.GET);
  }

  private HalLink buildConsultantsLink() {
    return buildHalLink(
        methodOn(UseradminApi.class).getConsultants(DEFAULT_PAGE, DEFAULT_PER_PAGE, null, null),
        MethodEnum.GET);
  }

  private HalLink buildConsultantAgenciesLink() {
    return buildHalLink(methodOn(UseradminApi.class).getConsultantAgencies(null), MethodEnum.GET);
  }

  private HalLink buildCreateConsultantLink() {
    return buildHalLink(methodOn(UseradminApi.class).createConsultant(null), MethodEnum.POST);
  }

  private HalLink buildDeleteAskerLink() {
    return buildHalLink(methodOn(UseradminApi.class).markAskerForDeletion(null), MethodEnum.DELETE);
  }
}

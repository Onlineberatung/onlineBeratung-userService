package de.caritas.cob.userservice.api.adapters.web.mapping;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.caritas.cob.userservice.api.adapters.web.controller.UserAdminController;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminLinks;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.PaginationLinks;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDtoMapper implements DtoMapperUtils {

  private final @NonNull TenantService tenantService;

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  public AdminSearchResultDTO adminSearchResultOf(
      Map<String, Object> resultMap,
      String query,
      Integer page,
      Integer perPage,
      String field,
      String order) {
    var admins = new ArrayList<AdminResponseDTO>();

    var adminMaps = (List<Map<String, Object>>) resultMap.get("admins");
    adminMaps.forEach(
        adminMap -> {
          var response = new AdminResponseDTO();
          response.setEmbedded(adminDtoOf(adminMap));
          response.setLinks(consultantLinksOf(adminMap));
          admins.add(response);
        });

    var result = new AdminSearchResultDTO();
    result.setTotal((Integer) resultMap.get("totalElements"));
    result.setEmbedded(admins);

    var pagination = new PaginationLinks().self(pageLinkOf(query, page, perPage, field, order));
    if (!(boolean) resultMap.get("isFirstPage")) {
      pagination.previous(pageLinkOf(query, page - 1, perPage, field, order));
    }
    if (!(boolean) resultMap.get("isLastPage")) {
      pagination.next(pageLinkOf(query, page + 1, perPage, field, order));
    }
    result.setLinks(pagination);

    return result;
  }

  public AdminLinks consultantLinksOf(Map<String, Object> adminMap) {
    var id = (String) adminMap.get("id");

    return new AdminLinks()
        .self(adminLinkOf(id, MethodEnum.GET))
        .update(adminLinkOf(id, MethodEnum.PUT))
        .delete(adminLinkOf(id, MethodEnum.DELETE))
        .agencies(adminAgencyLinkOf(id, MethodEnum.GET))
        .addAgency(adminAgencyLinkOf(id, MethodEnum.POST));
  }

  public HalLink adminAgencyLinkOf(String id, MethodEnum method) {
    var userAdminApi = methodOn(UseradminApi.class);
    HttpEntity<?> httpEntity;
    if (method == MethodEnum.POST) {
      httpEntity = userAdminApi.createAdminAgencyRelation(id, null);
    } else {
      httpEntity = userAdminApi.getAdminAgencies(id);
    }

    return halLinkOf(httpEntity, method);
  }

  private HalLink pageLinkOf(String query, int page, int perPage, String field, String order) {
    var httpEntity =
        methodOn(UserAdminController.class).searchAgencyAdmins(query, page, perPage, field, order);

    return halLinkOf(httpEntity, MethodEnum.GET);
  }

  public HalLink adminLinkOf(String id, MethodEnum method) {
    var userAdminApi = methodOn(UseradminApi.class);
    HttpEntity<?> httpEntity;
    switch (method) {
      case PUT:
        httpEntity = userAdminApi.updateAgencyAdmin(id, null);
        break;
      case DELETE:
        httpEntity = userAdminApi.deleteAgencyAdmin(id);
        break;
      default:
        httpEntity = userAdminApi.getAgencyAdmin(id);
    }

    return halLinkOf(httpEntity, method);
  }

  private AdminDTO adminDtoOf(Map<String, Object> adminUserMap) {
    var adminDTO = new AdminDTO();
    adminDTO.setId((String) adminUserMap.get("id"));
    adminDTO.setEmail((String) adminUserMap.get("email"));
    adminDTO.setFirstname((String) adminUserMap.get("firstName"));
    adminDTO.setLastname((String) adminUserMap.get("lastName"));
    adminDTO.setUsername((String) adminUserMap.get("username"));
    adminDTO.setCreateDate((String) adminUserMap.get("createdAt"));
    adminDTO.setUpdateDate((String) adminUserMap.get("updatedAt"));

    if (multiTenancyEnabled) {
      enrichResponseWithTenantInformation(adminUserMap, adminDTO);
    }

    var agencies = new ArrayList<AgencyAdminResponseDTO>();
    var agencyMaps = (ArrayList<Map<String, Object>>) adminUserMap.get("agencies");
    agencyMaps.forEach(
        agencyMap -> {
          var agency = new AgencyAdminResponseDTO();
          agency.setId((Long) agencyMap.get("id"));
          agency.setName((String) agencyMap.get("name"));
          agency.setPostcode((String) agencyMap.get("postcode"));
          agency.setCity((String) agencyMap.get("city"));
          agency.setDescription((String) agencyMap.get("description"));
          agency.setTeamAgency((Boolean) agencyMap.get("isTeamAgency"));
          agency.setOffline((Boolean) agencyMap.get("isOffline"));
          agency.setConsultingType((Integer) agencyMap.get("consultingType"));
          agencies.add(agency);
        });
    adminDTO.setAgencies(agencies);

    return adminDTO;
  }

  private void enrichResponseWithTenantInformation(
      Map<String, Object> adminUserMap, AdminDTO adminDTO) {
    Long tenantId = (Long) adminUserMap.get("tenantId");
    adminDTO.setTenantId(String.valueOf(tenantId));
    if (tenantId != null) {
      enrichWithTenantSubdomainAndName(adminDTO, tenantId);
    }
  }

  private void enrichWithTenantSubdomainAndName(AdminDTO adminDTO, Long tenantId) {
    RestrictedTenantDTO restrictedTenantData = tenantService.getRestrictedTenantData(tenantId);
    adminDTO.setTenantSubdomain(restrictedTenantData.getSubdomain());
    adminDTO.setTenantName(restrictedTenantData.getName());
  }
}

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
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminDtoMapper implements DtoMapperUtils {

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

  private AdminDTO adminDtoOf(Map<String, Object> consultantMap) {
    var adminDTO = new AdminDTO();
    adminDTO.setId((String) consultantMap.get("id"));
    adminDTO.setEmail((String) consultantMap.get("email"));
    adminDTO.setFirstname((String) consultantMap.get("firstName"));
    adminDTO.setLastname((String) consultantMap.get("lastName"));
    adminDTO.setUsername((String) consultantMap.get("username"));
    adminDTO.setCreateDate((String) consultantMap.get("createdAt"));
    adminDTO.setUpdateDate((String) consultantMap.get("updatedAt"));

    var agencies = new ArrayList<AgencyAdminResponseDTO>();
    var agencyMaps = (ArrayList<Map<String, Object>>) consultantMap.get("agencies");
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
}

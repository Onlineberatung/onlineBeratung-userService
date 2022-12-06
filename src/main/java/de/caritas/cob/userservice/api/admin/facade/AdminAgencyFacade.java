package de.caritas.cob.userservice.api.admin.facade;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.AdminAgencyRelationService;
import de.caritas.cob.userservice.api.admin.service.admin.AdminAgencyService;
import de.caritas.cob.userservice.api.admin.service.admin.search.AdminFilterService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAgencyFacade {

  private final @NonNull AdminAgencyService adminAgencyService;
  private final @NonNull AdminAgencyRelationService adminAgencyRelationService;
  private final @NonNull AdminFilterService adminFilterService;

  public AdminResponseDTO createNewAdminAgency(final CreateAgencyAdminDTO createAgencyAdminDTO) {
    return this.adminAgencyService.createNewAdminAgency(createAgencyAdminDTO);
  }

  public AdminResponseDTO findAgencyAdmin(final String adminId) {
    return this.adminAgencyService.findAgencyAdmin(adminId);
  }

  public AdminResponseDTO updateAgencyAdmin(
      final String adminId, final UpdateAgencyAdminDTO updateAgencyAdminDTO) {
    return this.adminAgencyService.updateAgencyAdmin(adminId, updateAgencyAdminDTO);
  }

  public void deleteAgencyAdmin(final String adminId) {
    this.adminAgencyService.deleteAgencyAdmin(adminId);
  }

  public List<Long> findAdminUserAgencyIds(String userId) {
    return this.adminAgencyService.findAgenciesOfAdmin(userId);
  }

  public void createNewAdminAgencyRelation(
      final String adminId, final CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO) {
    this.adminAgencyRelationService.createAdminAgencyRelation(
        adminId, createAdminAgencyRelationDTO);
  }

  public void deleteAdminAgencyRelation(final String adminId, final Long agencyId) {
    this.adminAgencyRelationService.deleteAdminAgencyRelation(adminId, agencyId);
  }

  public void setAdminAgenciesRelation(
      final String adminId, final List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs) {
    this.adminAgencyRelationService.synchronizeAdminAgenciesRelation(
        adminId, newAdminAgencyRelationDTOs);
  }

  public AgencyAdminSearchResultDTO findFilteredAdminsAgency(
      final Integer page, final Integer perPage, final AdminFilter adminFilter, Sort sort) {
    var filteredAdmins = adminFilterService.findFilteredAdmins(page, perPage, adminFilter, sort);
    enrichAdminsWithAgencies(filteredAdmins);

    return filteredAdmins;
  }

  private void enrichAdminsWithAgencies(AgencyAdminSearchResultDTO filteredAdmins) {
    if (nonNull(filteredAdmins)) {
      var admins =
          filteredAdmins.getEmbedded().stream()
              .map(AdminResponseDTO::getEmbedded)
              .collect(Collectors.toSet());
      adminAgencyRelationService.appendAgenciesForAdmins(admins);
    }
  }

  public Map<String, Object> findAgencyAdminsByInfix(
      final String infix,
      final int pageNumber,
      final int pageSize,
      final String fieldName,
      final boolean isAscending) {
    var direction = isAscending ? Direction.ASC : Direction.DESC;
    var pageRequest = PageRequest.of(pageNumber, pageSize, direction, fieldName);
    return this.adminAgencyService.findAgencyAdminsByInfix(infix, pageRequest);
  }
}

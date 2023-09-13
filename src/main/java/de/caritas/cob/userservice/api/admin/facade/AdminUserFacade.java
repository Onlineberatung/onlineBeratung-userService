package de.caritas.cob.userservice.api.admin.facade;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateTenantAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.AdminAgencyRelationService;
import de.caritas.cob.userservice.api.admin.service.admin.AgencyAdminUserService;
import de.caritas.cob.userservice.api.admin.service.admin.TenantAdminUserService;
import de.caritas.cob.userservice.api.admin.service.admin.search.AdminFilterService;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserFacade {

  private final @NonNull AgencyAdminUserService agencyAdminUserService;

  private final @NonNull TenantAdminUserService tenantAdminUserService;
  private final @NonNull AdminAgencyRelationService adminAgencyRelationService;
  private final @NonNull AdminFilterService adminFilterService;

  private final @NonNull AuthenticatedUser authenticatedUser;

  public AdminResponseDTO createNewTenantAdmin(final CreateAdminDTO createTenantAdminDTO) {
    return this.tenantAdminUserService.createNewTenantAdmin(createTenantAdminDTO);
  }

  public AdminResponseDTO createNewAgencyAdmin(final CreateAdminDTO createAgencyAdminDTO) {
    return this.agencyAdminUserService.createNewAgencyAdmin(createAgencyAdminDTO);
  }

  public AdminResponseDTO findAgencyAdmin(final String adminId) {
    return this.agencyAdminUserService.findAgencyAdmin(adminId);
  }

  public AdminResponseDTO findTenantAdmin(final String adminId) {
    return this.tenantAdminUserService.findTenantAdmin(adminId);
  }

  public AdminResponseDTO updateTenantAdmin(
      final String adminId, final UpdateTenantAdminDTO updateTenantAdminDTO) {
    return this.tenantAdminUserService.updateTenantAdmin(adminId, updateTenantAdminDTO);
  }

  public AdminResponseDTO updateAgencyAdmin(
      final String adminId, final UpdateAgencyAdminDTO updateAgencyAdminDTO) {
    return this.agencyAdminUserService.updateAgencyAdmin(adminId, updateAgencyAdminDTO);
  }

  public void deleteAgencyAdmin(final String adminId) {
    this.agencyAdminUserService.deleteAgencyAdmin(adminId);
  }

  public void deleteTenantAdmin(final String adminId) {
    this.tenantAdminUserService.deleteTenantAdmin(adminId);
  }

  public List<Long> findAdminUserAgencyIds(String userId) {
    return this.agencyAdminUserService.findAgenciesOfAdmin(userId);
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

  public AdminSearchResultDTO findFilteredAdminsAgency(
      final Integer page, final Integer perPage, final AdminFilter adminFilter, Sort sort) {
    var filteredAdmins = adminFilterService.findFilteredAdmins(page, perPage, adminFilter, sort);
    enrichAdminsWithAgencies(filteredAdmins);

    return filteredAdmins;
  }

  private void enrichAdminsWithAgencies(AdminSearchResultDTO filteredAdmins) {
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
    return this.agencyAdminUserService.findAgencyAdminsByInfix(infix, pageRequest);
  }

  public Map<String, Object> findTenantAdminsByInfix(
      final String infix,
      final int pageNumber,
      final int pageSize,
      final String fieldName,
      final boolean isAscending) {
    var direction = isAscending ? Direction.ASC : Direction.DESC;
    var pageRequest = PageRequest.of(pageNumber, pageSize, direction, fieldName);
    return this.tenantAdminUserService.findTenantAdminsByInfix(infix, pageRequest);
  }

  public List<AdminResponseDTO> findTenantAdmins(Integer tenantId) {
    return tenantAdminUserService.findTenantAdmins(Long.valueOf(tenantId));
  }

  public AdminResponseDTO patchAdminUserData(PatchAdminDTO patchAdminDTO) {
    var adminId = authenticatedUser.getUserId();

    if (authenticatedUser.isRestrictedAgencyAdmin() && !authenticatedUser.isSingleTenantAdmin()) {

      return agencyAdminUserService.patchAgencyAdmin(adminId, patchAdminDTO);
    } else if (authenticatedUser.isSingleTenantAdmin()) {
      return tenantAdminUserService.patchTenantAdmin(adminId, patchAdminDTO);
    } else {
      throw new AccessDeniedException("User does not have permissions change admin data");
    }
  }
}

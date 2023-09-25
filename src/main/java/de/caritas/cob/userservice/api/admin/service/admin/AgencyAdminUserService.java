package de.caritas.cob.userservice.api.admin.service.admin;

import de.caritas.cob.userservice.api.UserServiceMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.create.CreateAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.delete.DeleteAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.update.UpdateAdminService;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminBase;
import de.caritas.cob.userservice.api.model.AdminAgency.AdminAgencyBase;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgencyAdminUserService {

  private final @NonNull RetrieveAdminService retrieveAdminService;
  private final @NonNull CreateAdminService createAdminService;
  private final @NonNull UpdateAdminService updateAdminService;
  private final @NonNull DeleteAdminService deleteAdminService;
  private final @NonNull UserServiceMapper userServiceMapper;
  private final @NonNull AgencyService agencyService;
  private final @NonNull TenantService tenantService;

  public AdminResponseDTO createNewAgencyAdmin(final CreateAdminDTO createAgencyAdminDTO) {
    final Admin newAdmin = createAdminService.createNewAgencyAdmin(createAgencyAdminDTO);
    return AdminResponseDTOBuilder.getInstance(newAdmin).buildAgencyAdminResponseDTO();
  }

  public AdminResponseDTO findAgencyAdmin(final String adminId) {
    final Admin admin = retrieveAdminService.findAdmin(adminId, Admin.AdminType.AGENCY);
    return AdminResponseDTOBuilder.getInstance(admin).buildAgencyAdminResponseDTO();
  }

  public AdminResponseDTO updateAgencyAdmin(
      final String adminId, final UpdateAgencyAdminDTO updateAgencyAdminDTO) {
    final Admin updatedAdmin = updateAdminService.updateAgencyAdmin(adminId, updateAgencyAdminDTO);
    return AdminResponseDTOBuilder.getInstance(updatedAdmin).buildAgencyAdminResponseDTO();
  }

  public void deleteAgencyAdmin(final String adminId) {
    this.deleteAdminService.deleteAgencyAdmin(adminId);
  }

  public List<Long> findAgenciesOfAdmin(final String adminId) {
    return retrieveAdminService.findAgencyIdsOfAdmin(adminId);
  }

  public Map<String, Object> findAgencyAdminsByInfix(String infix, PageRequest pageRequest) {
    Page<AdminBase> adminsPage =
        retrieveAdminService.findAllByInfix(infix, Admin.AdminType.AGENCY, pageRequest);
    var adminIds = adminsPage.stream().map(AdminBase::getId).collect(Collectors.toSet());
    var fullAdmins = retrieveAdminService.findAllById(adminIds);

    var tenantIdsToNameMap =
        adminsPage.stream()
            .filter(admin -> admin.getTenantId() != null)
            .collect(
                Collectors.toMap(
                    AdminBase::getTenantId,
                    admin -> tenantService.getRestrictedTenantData(admin.getTenantId()).getName(),
                    (existing, replacement) -> existing));

    var agenciesOfAdmin = retrieveAdminService.agenciesOfAdmin(adminIds);
    var agencyIds =
        agenciesOfAdmin.stream()
            .map(AdminAgencyBase::getAgencyId)
            .distinct()
            .collect(Collectors.toList());

    var agencies = agencyService.getAgenciesWithoutCaching(agencyIds);

    return userServiceMapper.mapOfAdmin(
        adminsPage, fullAdmins, agencies, agenciesOfAdmin, tenantIdsToNameMap);
  }

  public AdminResponseDTO patchAgencyAdmin(String adminId, PatchAdminDTO patchAdminDTO) {

    final Admin updatedAdmin = updateAdminService.patchAgencyAdmin(adminId, patchAdminDTO);
    return AdminResponseDTOBuilder.getInstance(updatedAdmin).buildAgencyAdminResponseDTO();
  }
}

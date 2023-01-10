package de.caritas.cob.userservice.api.admin.service.admin;

import de.caritas.cob.userservice.api.UserServiceMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateTenantAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.create.CreateAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.delete.DeleteAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.update.UpdateAdminService;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminBase;
import de.caritas.cob.userservice.api.model.AdminAgency.AdminAgencyBase;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantAdminUserService {

  private final @NonNull RetrieveAdminService retrieveAdminService;
  private final @NonNull CreateAdminService createAdminService;
  private final @NonNull UpdateAdminService updateAdminService;
  private final @NonNull DeleteAdminService deleteAdminService;
  private final @NonNull UserServiceMapper userServiceMapper;
  private final @NonNull AgencyService agencyService;

  public AdminResponseDTO createNewTenantAdmin(final CreateAdminDTO createAgencyAdminDTO) {
    final Admin newAdmin = createAdminService.createNewTenantAdmin(createAgencyAdminDTO);
    return AdminResponseDTOBuilder.getInstance(newAdmin).buildResponseDTO();
  }

  public AdminResponseDTO findTenantAdmin(final String adminId) {
    final Admin admin = retrieveAdminService.findAdmin(adminId, Admin.AdminType.TENANT);
    return AdminResponseDTOBuilder.getInstance(admin).buildResponseDTO();
  }

  public AdminResponseDTO updateTenantAdmin(
      final String adminId, final UpdateTenantAdminDTO updateTenantAdminDTO) {
    final Admin updatedAdmin = updateAdminService.updateTenantAdmin(adminId, updateTenantAdminDTO);
    return AdminResponseDTOBuilder.getInstance(updatedAdmin).buildResponseDTO();
  }

  public void deleteTenantAdmin(final String adminId) {
    this.deleteAdminService.deleteTenantAdmin(adminId);
  }

  public Map<String, Object> findAgencyAdminsByInfix(String infix, PageRequest pageRequest) {
    Page<AdminBase> adminsPage = retrieveAdminService.findAllByInfix(infix, pageRequest);
    var adminIds = adminsPage.stream().map(AdminBase::getId).collect(Collectors.toSet());
    var fullAdmins = retrieveAdminService.findAllById(adminIds);

    var agenciesOfAdmin = retrieveAdminService.agenciesOfAdmin(adminIds);
    var agencyIds =
        agenciesOfAdmin.stream()
            .map(AdminAgencyBase::getAgencyId)
            .distinct()
            .collect(Collectors.toList());

    var agencies = agencyService.getAgenciesWithoutCaching(agencyIds);

    return userServiceMapper.mapOfAdmin(adminsPage, fullAdmins, agencies, agenciesOfAdmin);
  }
}

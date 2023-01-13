package de.caritas.cob.userservice.api.admin.service.admin;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.UserServiceMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateTenantAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.create.CreateAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.delete.DeleteAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.update.UpdateAdminService;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminBase;
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

  public AdminResponseDTO createNewTenantAdmin(final CreateAdminDTO createTenantAdminDTO) {
    validateCreateAdmin(createTenantAdminDTO);
    final Admin newAdmin = createAdminService.createNewTenantAdmin(createTenantAdminDTO);
    return AdminResponseDTOBuilder.getInstance(newAdmin).buildAgencyAdminResponseDTO();
  }

  private void validateCreateAdmin(CreateAdminDTO createTenantAdminDTO) {
    validateTenantId(createTenantAdminDTO.getTenantId());
  }

  private void validateUpdateAdmin(UpdateTenantAdminDTO updateTenantAdminDTO) {
    validateTenantId(updateTenantAdminDTO.getTenantId());
  }

  private void validateTenantId(Integer inputTenantId) {
    if (inputTenantId == null) {
      throw new BadRequestException("Tenant id must be provided");
    }
    if (inputTenantId.equals(0)) {
      throw new BadRequestException("Tenant id cannot be equal to 0");
    }
  }

  public AdminResponseDTO findTenantAdmin(final String adminId) {
    final Admin admin = retrieveAdminService.findAdmin(adminId, Admin.AdminType.TENANT);
    return AdminResponseDTOBuilder.getInstance(admin).buildAgencyAdminResponseDTO();
  }

  public AdminResponseDTO updateTenantAdmin(
      final String adminId, final UpdateTenantAdminDTO updateTenantAdminDTO) {
    validateUpdateAdmin(updateTenantAdminDTO);
    final Admin updatedAdmin = updateAdminService.updateTenantAdmin(adminId, updateTenantAdminDTO);
    return AdminResponseDTOBuilder.getInstance(updatedAdmin).buildAgencyAdminResponseDTO();
  }

  public void deleteTenantAdmin(final String adminId) {
    this.deleteAdminService.deleteTenantAdmin(adminId);
  }

  public Map<String, Object> findTenantAdminsByInfix(String infix, PageRequest pageRequest) {
    Page<AdminBase> adminsPage =
        retrieveAdminService.findAllByInfix(infix, Admin.AdminType.TENANT, pageRequest);
    var adminIds = adminsPage.stream().map(AdminBase::getId).collect(Collectors.toSet());
    var fullAdmins = retrieveAdminService.findAllById(adminIds);

    return userServiceMapper.mapOfAdmin(
        adminsPage, fullAdmins, Lists.newArrayList(), Lists.newArrayList());
  }
}

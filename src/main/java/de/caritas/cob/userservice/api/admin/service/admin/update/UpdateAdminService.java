package de.caritas.cob.userservice.api.admin.service.admin.update;

import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateTenantAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateAdminService {

  private final @NonNull IdentityClient identityClient;
  private final @NonNull UserAccountInputValidator userAccountInputValidator;
  private final @NonNull AdminRepository adminRepository;
  private final @NonNull RetrieveAdminService retrieveAdminService;

  public Admin updateAgencyAdmin(
      final String adminId, final UpdateAgencyAdminDTO updateAgencyAdminDTO) {
    final Admin admin = retrieveAdminService.findAdmin(adminId, Admin.AdminType.AGENCY);
    final UserDTO userDTO = buildValidatedUserDTO(updateAgencyAdminDTO, admin);
    this.identityClient.updateUserData(
        admin.getId(),
        userDTO,
        updateAgencyAdminDTO.getFirstname(),
        updateAgencyAdminDTO.getLastname());

    return this.adminRepository.save(buildAdmin(updateAgencyAdminDTO, admin));
  }

  private UserDTO buildValidatedUserDTO(
      final UpdateAgencyAdminDTO updateAgencyAdminDTO, final Admin admin) {
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail(updateAgencyAdminDTO.getEmail());
    userDTO.setUsername(admin.getUsername());
    userDTO.setTenantId(admin.getTenantId());

    this.userAccountInputValidator.validateUserDTO(userDTO);
    return userDTO;
  }

  private UserDTO buildValidatedUserDTO(
      final UpdateTenantAdminDTO updateTenantAdminDTO, final Admin admin) {
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail(updateTenantAdminDTO.getEmail());
    userDTO.setUsername(admin.getUsername());
    userDTO.setTenantId(Long.valueOf(updateTenantAdminDTO.getTenantId()));

    this.userAccountInputValidator.validateUserDTO(userDTO);
    return userDTO;
  }

  private Admin buildAdmin(final UpdateAgencyAdminDTO updateAgencyAdminDTO, final Admin admin) {
    admin.setLastName(updateAgencyAdminDTO.getLastname());
    admin.setFirstName(updateAgencyAdminDTO.getFirstname());
    admin.setEmail(updateAgencyAdminDTO.getEmail());
    return admin;
  }

  private Admin buildAdmin(final UpdateTenantAdminDTO updateTenantAdminDTO, final Admin admin) {
    admin.setLastName(updateTenantAdminDTO.getLastname());
    admin.setFirstName(updateTenantAdminDTO.getFirstname());
    admin.setEmail(updateTenantAdminDTO.getEmail());
    admin.setTenantId(Long.valueOf(updateTenantAdminDTO.getTenantId()));
    return admin;
  }

  public Admin updateTenantAdmin(String adminId, UpdateTenantAdminDTO updateTenantAdminDTO) {
    final Admin admin = retrieveAdminService.findAdmin(adminId, Admin.AdminType.TENANT);
    final UserDTO userDTO = buildValidatedUserDTO(updateTenantAdminDTO, admin);
    this.identityClient.updateUserData(
        admin.getId(),
        userDTO,
        updateTenantAdminDTO.getFirstname(),
        updateTenantAdminDTO.getLastname());

    return this.adminRepository.save(buildAdmin(updateTenantAdminDTO, admin));
  }
}

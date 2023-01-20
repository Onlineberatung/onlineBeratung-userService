package de.caritas.cob.userservice.api.admin.service.admin.update;

import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
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
    final Admin admin = retrieveAdminService.findAgencyAdmin(adminId);
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

    this.userAccountInputValidator.validateUserDTO(userDTO);
    return userDTO;
  }

  private Admin buildAdmin(final UpdateAgencyAdminDTO updateAgencyAdminDTO, final Admin admin) {
    admin.setLastName(updateAgencyAdminDTO.getLastname());
    admin.setFirstName(updateAgencyAdminDTO.getFirstname());
    admin.setEmail(updateAgencyAdminDTO.getEmail());
    return admin;
  }
}

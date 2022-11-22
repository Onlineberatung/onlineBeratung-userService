package de.caritas.cob.userservice.api.admin.service.admin;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAgencyService {
  private final @NonNull IdentityClient identityClient;

  private final @NonNull UserAccountInputValidator userAccountInputValidator;
  private final @NonNull UserHelper userHelper;
  private final @NonNull AdminAgencyRepository adminAgencyRepository;

  public AdminResponseDTO createNewAdminAgency(final CreateAgencyAdminDTO createAgencyAdminDTO) {
    String keycloakUserId = createKeycloakUser(createAgencyAdminDTO);
    String password = userHelper.getRandomPassword();
    identityClient.updatePassword(keycloakUserId, password);
    identityClient.updateRole(keycloakUserId, UserRole.AGENCY_ADMIN);

    final Admin admin = buildAdmin(createAgencyAdminDTO, keycloakUserId);
    Admin newAdmin = adminAgencyRepository.save(admin);
    return AdminResponseDTOBuilder.getInstance(newAdmin).buildResponseDTO();
  }

  public AdminResponseDTO findAgencyAdmin(final String adminId) {
    final Admin admin =
        this.adminAgencyRepository
            .findById(adminId)
            .orElseThrow(
                () ->
                    new NoContentException(
                        String.format("Agency Admin with id %s not found", adminId)));
    return AdminResponseDTOBuilder.getInstance(admin).buildResponseDTO();
  }

  public AdminResponseDTO updateAgencyAdmin(
      final String adminId, final UpdateAgencyAdminDTO updateAgencyAdminDTO) {
    final Admin admin =
        this.adminAgencyRepository
            .findById(adminId)
            .orElseThrow(
                () ->
                    new NoContentException(
                        String.format("Agency Admin with id %s not found", adminId)));

    UserDTO userDTO = buildValidatedUserDTO(updateAgencyAdminDTO, admin);
    this.identityClient.updateUserData(
        admin.getAdminId(),
        userDTO,
        updateAgencyAdminDTO.getFirstname(),
        updateAgencyAdminDTO.getLastname());

    Admin updatedAdmin = this.adminAgencyRepository.save(buildAdmin(updateAgencyAdminDTO, admin));
    return AdminResponseDTOBuilder.getInstance(updatedAdmin).buildResponseDTO();
  }

  public void deleteAgencyAdmin(final String adminId) {
    this.adminAgencyRepository.deleteById(adminId);
  }

  private Admin buildAdmin(final UpdateAgencyAdminDTO updateAgencyAdminDTO, final Admin admin) {
    admin.setLastName(updateAgencyAdminDTO.getLastname());
    admin.setFirstName(updateAgencyAdminDTO.getFirstname());
    admin.setEmail(updateAgencyAdminDTO.getEmail());
    return admin;
  }

  private static Admin buildAdmin(
      final CreateAgencyAdminDTO createAgencyAdminDTO, final String keycloakUserId) {
    final Integer tenantId = createAgencyAdminDTO.getTenantId();
    return Admin.builder()
        .adminId(keycloakUserId)
        .type(Admin.AdminType.AGENCY)
        .tenantId(tenantId == null ? null : Long.valueOf(tenantId))
        .username(createAgencyAdminDTO.getUsername())
        .firstName(createAgencyAdminDTO.getFirstname())
        .lastName(createAgencyAdminDTO.getLastname())
        .email(createAgencyAdminDTO.getEmail())
        .build();
  }

  private String createKeycloakUser(final CreateAgencyAdminDTO createAgencyAdminDTO) {
    UserDTO userDto =
        buildUserDTO(
            createAgencyAdminDTO.getUsername(),
            createAgencyAdminDTO.getEmail(),
            createAgencyAdminDTO.getTenantId());
    this.userAccountInputValidator.validateUserDTO(userDto);
    KeycloakCreateUserResponseDTO response =
        identityClient.createKeycloakUser(
            userDto, createAgencyAdminDTO.getFirstname(), createAgencyAdminDTO.getLastname());
    this.userAccountInputValidator.validateKeycloakResponse(response);
    return response.getUserId();
  }

  private UserDTO buildUserDTO(String username, String email, Integer tenantId) {
    UserDTO userDto = new UserDTO();
    userDto.setUsername(new UsernameTranscoder().encodeUsername(username));
    userDto.setEmail(email);
    userDto.setTenantId(tenantId == null ? null : Long.valueOf(tenantId));
    return userDto;
  }

  private UserDTO buildValidatedUserDTO(UpdateAgencyAdminDTO updateAgencyAdminDTO, Admin admin) {
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail(updateAgencyAdminDTO.getEmail());
    userDTO.setUsername(admin.getUsername());

    this.userAccountInputValidator.validateUserDTO(userDTO);
    return userDTO;
  }
}

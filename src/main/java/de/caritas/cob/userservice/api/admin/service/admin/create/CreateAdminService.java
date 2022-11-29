package de.caritas.cob.userservice.api.admin.service.admin.create;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateAdminService {

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  private final @NonNull IdentityClient identityClient;
  private final @NonNull UserAccountInputValidator userAccountInputValidator;
  private final @NonNull UserHelper userHelper;
  private final @NonNull AdminRepository adminRepository;

  public Admin createNewAdminAgency(final CreateAgencyAdminDTO createAgencyAdminDTO) {
    final String keycloakUserId = createKeycloakUser(createAgencyAdminDTO);
    final String password = userHelper.getRandomPassword();
    identityClient.updatePassword(keycloakUserId, password);
    identityClient.updateRole(keycloakUserId, UserRole.RESTRICTED_AGENCY_ADMIN);
    identityClient.updateRole(keycloakUserId, UserRole.USER_ADMIN);
    assignCurrentTenantContext(createAgencyAdminDTO);

    return adminRepository.save(buildAdmin(createAgencyAdminDTO, keycloakUserId));
  }

  private String createKeycloakUser(final CreateAgencyAdminDTO createAgencyAdminDTO) {
    final UserDTO userDto = buildValidatedUserDTO(createAgencyAdminDTO);

    final KeycloakCreateUserResponseDTO response =
        identityClient.createKeycloakUser(
            userDto, createAgencyAdminDTO.getFirstname(), createAgencyAdminDTO.getLastname());
    this.userAccountInputValidator.validateKeycloakResponse(response);

    return response.getUserId();
  }

  private UserDTO buildValidatedUserDTO(final CreateAgencyAdminDTO createAgencyAdminDTO) {
    UserDTO userDto = new UserDTO();
    userDto.setUsername(
        new UsernameTranscoder().encodeUsername(createAgencyAdminDTO.getUsername()));
    userDto.setEmail(createAgencyAdminDTO.getEmail());

    Integer tenantId = createAgencyAdminDTO.getTenantId();
    userDto.setTenantId(tenantId == null ? null : Long.valueOf(tenantId));

    this.userAccountInputValidator.validateUserDTO(userDto);
    return userDto;
  }

  private Admin buildAdmin(
      final CreateAgencyAdminDTO createAgencyAdminDTO, final String keycloakUserId) {
    final Integer tenantId = createAgencyAdminDTO.getTenantId();
    return Admin.builder()
        .id(keycloakUserId)
        .type(Admin.AdminType.AGENCY)
        .tenantId(tenantId == null ? null : Long.valueOf(tenantId))
        .username(createAgencyAdminDTO.getUsername())
        .firstName(createAgencyAdminDTO.getFirstname())
        .lastName(createAgencyAdminDTO.getLastname())
        .email(createAgencyAdminDTO.getEmail())
        .createDate(nowInUtc())
        .updateDate(nowInUtc())
        .build();
  }

  private void assignCurrentTenantContext(CreateAgencyAdminDTO createAgencyAdminDTO) {
    if (multiTenancyEnabled) {
      createAgencyAdminDTO.setTenantId(TenantContext.getCurrentTenant().intValue());
    }
  }
}

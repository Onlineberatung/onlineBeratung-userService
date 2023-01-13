package de.caritas.cob.userservice.api.admin.service.admin.create;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.List;
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

  public Admin createNewAgencyAdmin(CreateAdminDTO createAgencyAdminDTO) {
    return createNewAdmin(createAgencyAdminDTO, Admin.AdminType.AGENCY);
  }

  public Admin createNewTenantAdmin(CreateAdminDTO createAgencyAdminDTO) {
    return createNewAdmin(createAgencyAdminDTO, Admin.AdminType.TENANT);
  }

  List<UserRole> getDefaultRoles(Admin.AdminType adminType) {
    if (Admin.AdminType.AGENCY.equals(adminType)) {
      return Lists.newArrayList(UserRole.RESTRICTED_AGENCY_ADMIN, UserRole.USER_ADMIN);
    }
    if (Admin.AdminType.TENANT.equals(adminType)) {
      return Lists.newArrayList(UserRole.USER_ADMIN, UserRole.SINGLE_TENANT_ADMIN);
    }
    return Lists.newArrayList();
  }

  public Admin createNewAdmin(final CreateAdminDTO createAdminDTO, Admin.AdminType adminType) {
    final String keycloakUserId = createKeycloakUser(createAdminDTO);
    final String password = userHelper.getRandomPassword();
    identityClient.updatePassword(keycloakUserId, password);
    getDefaultRoles(adminType).stream()
        .forEach(role -> identityClient.updateRole(keycloakUserId, role));
    assignCurrentTenantContextForAgencyAdmins(createAdminDTO, adminType);
    return adminRepository.save(buildAdmin(createAdminDTO, adminType, keycloakUserId));
  }

  private void assignCurrentTenantContextForAgencyAdmins(
      CreateAdminDTO createAdminDTO, Admin.AdminType adminType) {
    if (adminType == Admin.AdminType.AGENCY) {
      assignCurrentTenantContext(createAdminDTO);
    }
  }

  private String createKeycloakUser(final CreateAdminDTO createAgencyAdminDTO) {
    final UserDTO userDto = buildValidatedUserDTO(createAgencyAdminDTO);

    final KeycloakCreateUserResponseDTO response =
        identityClient.createKeycloakUser(
            userDto, createAgencyAdminDTO.getFirstname(), createAgencyAdminDTO.getLastname());
    this.userAccountInputValidator.validateKeycloakResponse(response);

    return response.getUserId();
  }

  private UserDTO buildValidatedUserDTO(final CreateAdminDTO createAgencyAdminDTO) {
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
      final CreateAdminDTO createAgencyAdminDTO,
      Admin.AdminType adminType,
      final String keycloakUserId) {
    final Integer tenantId = createAgencyAdminDTO.getTenantId();
    return Admin.builder()
        .id(keycloakUserId)
        .type(adminType)
        .tenantId(tenantId == null ? null : Long.valueOf(tenantId))
        .username(createAgencyAdminDTO.getUsername())
        .firstName(createAgencyAdminDTO.getFirstname())
        .lastName(createAgencyAdminDTO.getLastname())
        .email(createAgencyAdminDTO.getEmail())
        .createDate(nowInUtc())
        .updateDate(nowInUtc())
        .build();
  }

  private void assignCurrentTenantContext(CreateAdminDTO createAgencyAdminDTO) {
    if (multiTenancyEnabled && !isTechnicalTenant(TenantContext.getCurrentTenant())) {
      createAgencyAdminDTO.setTenantId(TenantContext.getCurrentTenant().intValue());
    }
  }

  private boolean isTechnicalTenant(Long tenantId) {
    return tenantId != null && tenantId.equals(0L);
  }
}

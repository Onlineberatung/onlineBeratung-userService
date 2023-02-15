package de.caritas.cob.userservice.api.admin.service.admin.search;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminBase;
import de.caritas.cob.userservice.api.model.Admin.AdminType;
import de.caritas.cob.userservice.api.model.AdminAgency;
import de.caritas.cob.userservice.api.model.AdminAgency.AdminAgencyBase;
import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrieveAdminService {

  public static final String ADMIN_WITH_ID_S_NOT_FOUND = "Admin with id %s not found";
  private final @NonNull AdminRepository adminRepository;
  private final @NonNull AdminAgencyRepository adminAgencyRepository;

  public Admin findAdmin(final String adminId, Admin.AdminType adminType) {
    Optional<Admin> byId = this.adminRepository.findByIdAndType(adminId, adminType);
    return byId.filter(admin -> admin.getType().equals(adminType))
        .orElseThrow(
            () -> new NoContentException(String.format(ADMIN_WITH_ID_S_NOT_FOUND, adminId)));
  }

  public List<Long> findAgencyIdsOfAdmin(final String adminId) {
    final Optional<Admin> admin = adminRepository.findById(adminId);
    if (admin.isEmpty()) {
      throw new BadRequestException(String.format(ADMIN_WITH_ID_S_NOT_FOUND, adminId));
    }

    return adminAgencyRepository.findByAdminId(adminId).stream()
        .map(AdminAgency::getAgencyId)
        .collect(Collectors.toList());
  }

  public Page<AdminBase> findAllByInfix(
      String infix, Admin.AdminType adminType, PageRequest pageRequest) {
    return adminRepository.findAllByInfix(infix, adminType, pageRequest);
  }

  public List<Admin> findAllById(Set<String> adminIds) {
    return adminRepository.findAllByIdIn(adminIds);
  }

  public List<AdminAgencyBase> agenciesOfAdmin(Set<String> adminIds) {
    return adminAgencyRepository.findByAdminIdIn(adminIds);
  }

  public List<Admin> findTenantAdminsByTenantId(Long tenantId) {
    return this.adminRepository.findByTenantIdAndType(tenantId, AdminType.TENANT);
  }
}

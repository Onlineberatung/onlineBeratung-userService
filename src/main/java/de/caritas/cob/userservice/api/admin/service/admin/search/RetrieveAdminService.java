package de.caritas.cob.userservice.api.admin.service.admin.search;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminBase;
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

  public static final String AGENCY_ADMIN_WITH_ID_S_NOT_FOUND = "Agency Admin with id %s not found";
  private final @NonNull AdminRepository adminRepository;
  private final @NonNull AdminAgencyRepository adminAgencyRepository;

  public Admin findAgencyAdmin(final String adminId) {
    return this.adminRepository
        .findById(adminId)
        .orElseThrow(
            () -> new NoContentException(String.format(AGENCY_ADMIN_WITH_ID_S_NOT_FOUND, adminId)));
  }

  public List<Long> findAgencyIdsOfAdmin(final String adminId) {
    final Optional<Admin> admin = adminRepository.findById(adminId);
    if (admin.isEmpty()) {
      throw new BadRequestException(String.format(AGENCY_ADMIN_WITH_ID_S_NOT_FOUND, adminId));
    }

    return adminAgencyRepository.findByAdminId(adminId).stream()
        .map(AdminAgency::getAgencyId)
        .collect(Collectors.toList());
  }

  public Page<AdminBase> findAllByInfix(String infix, PageRequest pageRequest) {
    return adminRepository.findAllByInfix(infix, pageRequest);
  }

  public List<Admin> findAllById(Set<String> adminIds) {
    return adminRepository.findAllByIdIn(adminIds);
  }

  public List<AdminAgencyBase> agenciesOfAdmin(Set<String> adminIds) {
    return adminAgencyRepository.findByAdminIdIn(adminIds);
  }
}

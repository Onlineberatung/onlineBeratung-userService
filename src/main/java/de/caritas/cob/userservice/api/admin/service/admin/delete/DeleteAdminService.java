package de.caritas.cob.userservice.api.admin.service.admin.delete;

import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteAdminService {

  private final @NonNull AdminRepository adminRepository;
  private final @NonNull AdminAgencyRepository adminAgencyRepository;
  private final @NonNull IdentityClient identityClient;

  public void deleteAgencyAdmin(String adminId) {
    this.adminAgencyRepository.deleteByAdminId(adminId);
    this.identityClient.deleteUser(adminId);
    this.adminRepository.deleteById(adminId);
  }

  public void deleteTenantAdmin(String adminId) {
    this.identityClient.deleteUser(adminId);
    this.adminRepository.deleteById(adminId);
  }
}

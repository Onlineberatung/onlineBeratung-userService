package de.caritas.cob.userservice.api.admin.facade;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminAgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.AdminAgencyService;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAgencyFacade {

  private final @NonNull AdminAgencyService adminAgencyService;

  public AdminResponseDTO createNewAdminAgency(final CreateAgencyAdminDTO createAgencyAdminDTO) {
    return this.adminAgencyService.createNewAdminAgency(createAgencyAdminDTO);
  }

  public AdminResponseDTO findAgencyAdmin(final String adminId) {
    return this.adminAgencyService.findAgencyAdmin(adminId);
  }

  public AdminResponseDTO updateAgencyAdmin(
      final String adminId, final UpdateAgencyAdminDTO updateAgencyAdminDTO) {
    return this.adminAgencyService.updateAgencyAdmin(adminId, updateAgencyAdminDTO);
  }

  public void deleteAgencyAdmin(final String adminId) {
    this.adminAgencyService.deleteAgencyAdmin(adminId);
  }

  public AdminAgencyResponseDTO findAdminAgencies(String adminId) {
    return this.adminAgencyService.findAgenciesOfAdmin(adminId);
  }

  public Collection<Long> findAdminUserAgencyIds(String userId) {
    var adminAgencies = findAdminAgencies(userId).getEmbedded();
    return adminAgencies.stream()
        .map(agency -> agency.getEmbedded().getId())
        .collect(Collectors.toList());
  }
}

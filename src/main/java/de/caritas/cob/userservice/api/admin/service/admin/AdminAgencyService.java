package de.caritas.cob.userservice.api.admin.service.admin;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminAgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.create.CreateAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.delete.DeleteAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.admin.service.admin.update.UpdateAdminService;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import de.caritas.cob.userservice.api.model.Admin;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAgencyService {

  private final @NonNull RetrieveAdminService retrieveAdminService;
  private final @NonNull CreateAdminService createAdminService;
  private final @NonNull UpdateAdminService updateAdminService;
  private final @NonNull DeleteAdminService deleteAdminService;
  private final @NonNull AgencyAdminService agencyService;

  public AdminResponseDTO createNewAdminAgency(final CreateAgencyAdminDTO createAgencyAdminDTO) {
    final Admin newAdmin = createAdminService.createNewAdminAgency(createAgencyAdminDTO);
    return AdminResponseDTOBuilder.getInstance(newAdmin).buildResponseDTO();
  }

  public AdminResponseDTO findAgencyAdmin(final String adminId) {
    final Admin admin = retrieveAdminService.findAgencyAdmin(adminId);
    return AdminResponseDTOBuilder.getInstance(admin).buildResponseDTO();
  }

  public AdminResponseDTO updateAgencyAdmin(
      final String adminId, final UpdateAgencyAdminDTO updateAgencyAdminDTO) {
    final Admin updatedAdmin = updateAdminService.updateAgencyAdmin(adminId, updateAgencyAdminDTO);
    return AdminResponseDTOBuilder.getInstance(updatedAdmin).buildResponseDTO();
  }

  public void deleteAgencyAdmin(final String adminId) {
    this.deleteAdminService.deleteAgencyAdmin(adminId);
  }

  public AdminAgencyResponseDTO findAgenciesOfAdmin(final String adminId) {
    var adminAgencyIds = retrieveAdminService.findAgencyIdsOfAdmin(adminId);

    var agencyList =
        this.agencyService.retrieveAllAgencies().stream()
            .filter(agency -> adminAgencyIds.contains(agency.getId()))
            .collect(Collectors.toList());

    return AdminAgencyResponseDTOBuilder.getInstance()
        .withAdminId(adminId)
        .withAgencies(agencyList)
        .build();
  }
}

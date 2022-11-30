package de.caritas.cob.userservice.api.admin.service.admin;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.ADMIN_AGENCY_RELATION_DOES_NOT_EXIST;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.admin.service.admin.create.agencyrelation.CreateAdminAgencyRelationService;
import de.caritas.cob.userservice.api.admin.service.admin.update.agencyrelation.SynchronizeAdminAgencyRelation;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.AdminAgency;
import de.caritas.cob.userservice.api.model.AdminAgency.AdminAgencyBase;
import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAgencyRelationService {

  private final @NonNull AdminAgencyRepository adminAgencyRepository;
  private final @NonNull AgencyAdminService agencyAdminService;
  private final @NonNull CreateAdminAgencyRelationService createAdminAgencyRelationService;
  private final @NonNull SynchronizeAdminAgencyRelation synchronizeAdminAgencyRelation;

  public void createAdminAgencyRelation(
      final String adminId, final CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO) {
    createAdminAgencyRelationService.create(adminId, createAdminAgencyRelationDTO);
  }

  public void deleteAdminAgencyRelation(final String adminId, final Long agencyId) {
    List<AdminAgency> adminAgencyRelations =
        adminAgencyRepository.findByAdminIdAndAgencyId(adminId, agencyId);
    if (isEmpty(adminAgencyRelations)) {
      throw new CustomValidationHttpStatusException(ADMIN_AGENCY_RELATION_DOES_NOT_EXIST);
    }

    adminAgencyRepository.deleteByAdminIdAndAgencyId(adminId, agencyId);
  }

  public void synchronizeAdminAgenciesRelation(
      final String adminId, final List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs) {
    this.synchronizeAdminAgencyRelation.synchronizeAdminAgenciesRelation(
        adminId, newAdminAgencyRelationDTOs);
  }

  public void appendAgenciesForAdmins(final Set<AdminDTO> admins) {
    var adminIds = admins.stream().map(AdminDTO::getId).collect(Collectors.toSet());

    var adminAgencies = adminAgencyRepository.findByAdminIdIn(adminIds);

    var agencyIds =
        adminAgencies.stream().map(AdminAgencyBase::getAgencyId).collect(Collectors.toSet());

    var agencies =
        this.agencyAdminService.retrieveAllAgencies().stream()
            .filter(agency -> agencyIds.contains(agency.getId()))
            .map(this::buildCopiedAgency)
            .collect(Collectors.toList());

    admins.forEach(
        admin -> admin.setAgencies(resolveAgenciesOfAdmin(admin.getId(), adminAgencies, agencies)));
  }

  private List<AgencyAdminResponseDTO> resolveAgenciesOfAdmin(
      final String adminId,
      final List<AdminAgencyBase> adminAgencies,
      final List<AgencyAdminResponseDTO> agencies) {
    var agencyIdsOfAdmin =
        adminAgencies.stream()
            .filter(adminAgency -> adminId.equals(adminAgency.getAdminId()))
            .map(AdminAgencyBase::getAgencyId)
            .collect(Collectors.toList());

    return agencies.stream()
        .filter(agency -> agencyIdsOfAdmin.contains(agency.getId()))
        .collect(Collectors.toList());
  }

  @SneakyThrows
  private AgencyAdminResponseDTO buildCopiedAgency(
      de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO
          agency) {
    var result = new AgencyAdminResponseDTO();
    BeanUtils.copyProperties(result, agency);

    return result;
  }
}

package de.caritas.cob.userservice.api.admin.service.admin.update.agencyrelation;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.AdminAgency;
import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynchronizeAdminAgencyRelation {

  private final @NonNull RetrieveAdminService retrieveAdminService;
  private final @NonNull AdminAgencyRepository adminAgencyRepository;

  public void synchronizeAdminAgenciesRelation(
      final String adminId, final List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs) {
    var admin = retrieveAdminService.findAdmin(adminId, Admin.AdminType.AGENCY);
    List<AdminAgency> existingAdminAgencyRelations = adminAgencyRepository.findByAdminId(adminId);
    List<AdminAgency> adminAgencyRelationsToDelete =
        emptyIfNull(existingAdminAgencyRelations).stream()
            .filter(
                existingRelation ->
                    shouldBeDeleted(existingRelation.getAgencyId(), newAdminAgencyRelationDTOs))
            .collect(Collectors.toList());

    List<AdminAgency> adminAgencyRelationsToAdd =
        emptyIfNull(newAdminAgencyRelationDTOs).stream()
            .filter(relation -> shouldBeAdded(relation, existingAdminAgencyRelations))
            .map(relation -> buildAdminAgency(admin, relation.getAgencyId()))
            .collect(Collectors.toList());

    adminAgencyRepository.deleteAll(adminAgencyRelationsToDelete);
    adminAgencyRepository.saveAll(adminAgencyRelationsToAdd);
  }

  private boolean shouldBeAdded(
      final CreateAdminAgencyRelationDTO newAdminAgencyRelationDTO,
      final List<AdminAgency> existingAdminAgencyRelations) {
    return emptyIfNull(existingAdminAgencyRelations).stream()
        .noneMatch(
            adminAgency ->
                adminAgency.getAgencyId().equals(newAdminAgencyRelationDTO.getAgencyId()));
  }

  private boolean shouldBeDeleted(
      final Long agencyId, final List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs) {
    return emptyIfNull(newAdminAgencyRelationDTOs).stream()
        .noneMatch(relation -> relation.getAgencyId().equals(agencyId));
  }

  private AdminAgency buildAdminAgency(final Admin admin, final Long agencyId) {
    return AdminAgency.builder()
        .admin(admin)
        .agencyId(agencyId)
        .createDate(nowInUtc())
        .updateDate(nowInUtc())
        .build();
  }
}

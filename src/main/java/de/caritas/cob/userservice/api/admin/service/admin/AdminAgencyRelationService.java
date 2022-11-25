package de.caritas.cob.userservice.api.admin.service.admin;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.ADMIN_AGENCY_RELATION_DOES_NOT_EXIST;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.AdminAgency;
import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAgencyRelationService {

  private final @NonNull AdminRepository adminRepository;
  private final @NonNull AdminAgencyRepository adminAgencyRepository;
  private final @NonNull AgencyService agencyService;

  public void createAdminAgencyRelation(
      final String adminId, final CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO) {
    var admin = retrieveAdmin(adminId);
    var agency = retrieveAgency(createAdminAgencyRelationDTO.getAgencyId());
    adminAgencyRepository.save(buildAdminAgency(admin, agency.getId()));
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
    var admin = retrieveAdmin(adminId);
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
    return false;
  }

  private boolean shouldBeDeleted(
      final Long agencyId, final List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs) {
    return emptyIfNull(newAdminAgencyRelationDTOs).stream()
        .noneMatch(relation -> relation.getAgencyId().equals(agencyId));
  }

  private Admin retrieveAdmin(final String adminId) {
    return adminRepository
        .findById(adminId)
        .orElseThrow(
            () ->
                new BadRequestException(String.format("Admin with id %s does not exist", adminId)));
  }

  private AdminAgency buildAdminAgency(final Admin admin, final Long agencyId) {
    return AdminAgency.builder()
        .admin(admin)
        .agencyId(agencyId)
        .createDate(nowInUtc())
        .updateDate(nowInUtc())
        .build();
  }

  private AgencyDTO retrieveAgency(Long agencyId) {
    var agencyDto = this.agencyService.getAgencyWithoutCaching(agencyId);
    return Optional.ofNullable(agencyDto)
        .orElseThrow(
            () ->
                new BadRequestException(
                    String.format("AgencyId %s is not a valid agency", agencyId)));
  }
}

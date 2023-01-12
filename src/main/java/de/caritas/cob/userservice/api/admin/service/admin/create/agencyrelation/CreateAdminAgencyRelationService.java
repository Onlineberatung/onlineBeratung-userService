package de.caritas.cob.userservice.api.admin.service.admin.create.agencyrelation;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.AdminAgency;
import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateAdminAgencyRelationService {

  private final @NonNull RetrieveAdminService retrieveAdminService;
  private final @NonNull AgencyService agencyService;
  private final @NonNull AdminAgencyRepository adminAgencyRepository;

  public void create(
      final String adminId, final CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO) {
    var admin = retrieveAdminService.findAdmin(adminId, Admin.AdminType.AGENCY);
    var agency = retrieveAgency(createAdminAgencyRelationDTO.getAgencyId());
    adminAgencyRepository.save(buildAdminAgency(admin, agency.getId()));
  }

  private AgencyDTO retrieveAgency(Long agencyId) {
    var agencyDto = this.agencyService.getAgencyWithoutCaching(agencyId);
    return Optional.ofNullable(agencyDto)
        .orElseThrow(
            () ->
                new BadRequestException(
                    String.format("AgencyId %s is not a valid agency", agencyId)));
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

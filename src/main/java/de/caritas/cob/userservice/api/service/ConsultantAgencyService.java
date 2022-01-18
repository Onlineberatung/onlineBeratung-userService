package de.caritas.cob.userservice.api.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.AgencyResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultantAgencyService {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull AgencyService agencyService;

  /**
   * Save a {@link ConsultantAgency} to the database.
   *
   * @param consultantAgency {@link ConsultantAgency}
   * @return the {@link ConsultantAgency}
   */
  public ConsultantAgency saveConsultantAgency(ConsultantAgency consultantAgency) {
    return consultantAgencyRepository.save(consultantAgency);
  }

  /**
   * Returns a List of {@link ConsultantAgency} Consultants with the given agency ID.
   *
   * @param agencyId agency ID
   * @return {@link List} of {@link ConsultantAgency}
   */
  public List<ConsultantAgency> findConsultantsByAgencyId(Long agencyId) {
    return consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(agencyId);
  }

  /**
   * Checks if provided consultant is assigned to provided agency.
   *
   * @param consultantId consultant ID
   * @param agencyId     agency ID
   * @return true if provided consultant is assigned to provided agency
   */
  public boolean isConsultantInAgency(String consultantId, Long agencyId) {
    List<ConsultantAgency> agencyList =
        consultantAgencyRepository.findByConsultantIdAndAgencyIdAndDeleteDateIsNull(
            consultantId, agencyId);

    return isNotEmpty(agencyList);
  }

  /**
   * Returns a {@link List} of {@link ConsultantAgency} for the provided agency IDs.
   *
   * @param agencyIds list of agency Ids
   * @return {@link List} of {@link ConsultantAgency}
   */
  public List<ConsultantAgency> getConsultantsOfAgencies(List<Long> agencyIds) {
    return consultantAgencyRepository.findByAgencyIdInAndDeleteDateIsNull(agencyIds);
  }

  /**
   * Returns an alphabetically sorted list of {@link ConsultantResponseDTO} depending on the
   * provided agencyId.
   *
   * @param agencyId agency ID
   * @return {@link List} of {@link ConsultantResponseDTO}
   */
  public List<ConsultantResponseDTO> getConsultantsOfAgency(Long agencyId) {

    List<ConsultantAgency> agencyList =
        consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNullOrderByConsultantFirstNameAsc(
            agencyId);

    if (isNotEmpty(agencyList)) {
      return agencyList.stream()
          .map(this::convertToConsultantResponseDTO)
          .collect(Collectors.toList());
    }

    return emptyList();
  }

  private ConsultantResponseDTO convertToConsultantResponseDTO(ConsultantAgency agency) {

    checkForInconsistencies(agency);

    return new ConsultantResponseDTO()
        .consultantId(agency.getConsultant().getId())
        .firstName(agency.getConsultant().getFirstName())
        .lastName(agency.getConsultant().getLastName());
  }

  private void checkForInconsistencies(ConsultantAgency agency) {
    checkForMissingAgency(agency);
    checkForMissingConsultant(agency);
  }

  private void checkForMissingAgency(ConsultantAgency agency) {
    if (isNull(agency)) {
      throw new InternalServerErrorException(
          "Database inconsistency: agency is null", LogService::logDatabaseError);
    }
  }

  private void checkForMissingConsultant(ConsultantAgency agency) {
    if (isNull(agency.getConsultant())) {
      throw new InternalServerErrorException(
          String.format(
              "Database inconsistency: could not get assigned consultant for agency with id %s",
              agency.getAgencyId()),
          LogService::logDatabaseError);
    }
  }

  /**
   * Returns all agencies of given consultant.
   *
   * @param consultantId the id of the consultant
   * @return the related agencies
   */
  public List<AgencyResponseDTO> getAgenciesOfConsultant(String consultantId) {
    var agencyIds = consultantAgencyRepository.findByConsultantId(consultantId).stream()
        .map(ConsultantAgency::getAgencyId)
        .collect(Collectors.toList());

    return agencyService.getAgencies(agencyIds).stream()
        .map(this::toAgencyResponseDTO)
        .collect(Collectors.toList());
  }

  private AgencyResponseDTO toAgencyResponseDTO(AgencyDTO agencyDTO) {
    return new AgencyResponseDTO()
        .id(agencyDTO.getId())
        .city(agencyDTO.getCity())
        .consultingType(agencyDTO.getConsultingType())
        .postcode(agencyDTO.getPostcode())
        .name(agencyDTO.getName())
        .description(agencyDTO.getDescription())
        .teamAgency(agencyDTO.getTeamAgency())
        .offline(agencyDTO.getOffline());
  }
}

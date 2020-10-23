package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ConsultantAgencyService {

  @Autowired
  ConsultantAgencyRepository consultantAgencyRepository;

  /**
   * Save a {@link ConsultantAgency} to the database.
   *
   * @param consultantAgency {@link ConsultantAgency}
   * @return the {@link ConsultantAgency}
   */
  public ConsultantAgency saveConsultantAgency(ConsultantAgency consultantAgency) {
    try {
      return consultantAgencyRepository.save(consultantAgency);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while saving consultant agency",
          LogService::logDatabaseError);
    }
  }

  /**
   * Returns a List of {@link ConsultantAgency} Consultants with the given agency ID.
   *
   * @param agencyId agency ID
   * @return {@link List} of {@link ConsultantAgency}
   */
  public List<ConsultantAgency> findConsultantsByAgencyId(Long agencyId) {
    try {
      return consultantAgencyRepository.findByAgencyId(agencyId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while loading consultants by agency",
          LogService::logDatabaseError);
    }
  }

  /**
   * Checks if provided consultant is assigned to provided agency.
   *
   * @param consultantId consultant ID
   * @param agencyId     agency ID
   * @return true if provided consultant is assigned to provided agency
   */
  public boolean isConsultantInAgency(String consultantId, Long agencyId) {
    try {

      List<ConsultantAgency> agencyList =
          consultantAgencyRepository.findByConsultantIdAndAgencyId(consultantId, agencyId);

      return isNotEmpty(agencyList);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while getting agency id data set for"
          + " consultant", LogService::logDatabaseError);
    }
  }

  /**
   * Returns an alphabetically sorted list of {@link ConsultantResponseDTO} depending on the
   * provided agencyId.
   *
   * @param agencyId agency ID
   * @return {@link List} of {@link ConsultantResponseDTO}
   */
  public List<ConsultantResponseDTO> getConsultantsOfAgency(Long agencyId) {

    List<ConsultantAgency> agencyList;
    List<ConsultantResponseDTO> responseList = null;

    try {
      agencyList = consultantAgencyRepository.findByAgencyIdOrderByConsultantFirstNameAsc(agencyId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while loading consultant list",
          LogService::logDatabaseError);
    }

    if (agencyList != null) {
      responseList = agencyList.stream().map(this::convertToConsultantResponseDTO)
          .collect(Collectors.toList());
    }

    return responseList;
  }

  private ConsultantResponseDTO convertToConsultantResponseDTO(ConsultantAgency agency) {

    String error;

    if (agency == null) {
      error = "Database inconsistency: agency is null";
      throw new InternalServerErrorException(error, LogService::logDatabaseError);
    }
    if (agency.getConsultant() == null) {
      error = String.format(
          "Database inconsistency: could not get assigned consultant for agency with id %s",
          agency.getAgencyId());
      throw new InternalServerErrorException(error, LogService::logDatabaseError);
    }

    return new ConsultantResponseDTO()
        .consultantId(agency.getConsultant().getId())
        .firstName(agency.getConsultant().getFirstName())
        .lastName(agency.getConsultant().getLastName());
  }

}

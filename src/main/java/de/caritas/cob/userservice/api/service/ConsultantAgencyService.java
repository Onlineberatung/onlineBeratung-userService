package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ConsultantAgencyService {

  @Autowired
  ConsultantAgencyRepository consultantAgencyRepository;

  /**
   * Save a {@link ConsultantAgency} to the database
   * 
   * @param consultantAgency
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
   * Returns a List of {@Link ConsultantAgency} Consultants with the given agency id
   * 
   * @param agencyId
   * @return
   */
  public List<ConsultantAgency> findConsultantsByAgencyId(Long agencyId) {
    try {
      return consultantAgencyRepository.findByAgencyId(agencyId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while loading consultants by agency",
          LogService::logDatabaseError);
    }
  }

  public boolean isConsultantInAgency(String consultantId, Long agencyId) {
    try {

      List<ConsultantAgency> agencyList =
          consultantAgencyRepository.findByConsultantIdAndAgencyId(consultantId, agencyId);

      return agencyList != null && agencyList.size() > 0;
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while getting agency id data set for"
          + " consultant", LogService::logDatabaseError);
    }
  }

  /**
   * Returns an alphabetically sorted list of {@link ConsultantResponseDTO} depending on the
   * provided agencyId
   *
   * @param agencyId
   * @return A list of {@link ConsultantResponseDTO}
   */
  public List<ConsultantResponseDTO> getConsultantsOfAgency(Long agencyId) {

    List<ConsultantAgency> agencyList = null;
    List<ConsultantResponseDTO> responseList = null;

    try {
      agencyList = consultantAgencyRepository.findByAgencyIdOrderByConsultantFirstNameAsc(agencyId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while loading consultant list",
          LogService::logDatabaseError);
    }

    if (agencyList != null) {
      responseList = agencyList.stream().map(agency -> convertToConsultantResponseDTO(agency))
          .collect(Collectors.toList());
    }

    return responseList;
  }

  /**
   * Converts a {@link ConsultantAgency} to a {@link ConsultantResponseDTO}
   * 
   * @param agency
   * @return
   */
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

    return new ConsultantResponseDTO(agency.getConsultant().getId(),
        agency.getConsultant().getFirstName(), agency.getConsultant().getLastName());
  }

}

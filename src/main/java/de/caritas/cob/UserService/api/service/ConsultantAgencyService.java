package de.caritas.cob.UserService.api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.model.ConsultantResponseDTO;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgencyRepository;

@Service
public class ConsultantAgencyService {

  @Autowired
  ConsultantAgencyRepository consultantAgencyRepository;
  @Autowired
  LogService logService;

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
      logService.logDatabaseError(ex);
      throw new ServiceException("Database error while saving consultant agency");
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
      logService.logDatabaseError(ex);
      throw new ServiceException("Database error while loading consultants by agency");
    }
  }

  public boolean isConsultantInAgency(String consultantId, Long agencyId) {
    try {

      List<ConsultantAgency> agencyList =
          consultantAgencyRepository.findByConsultantIdAndAgencyId(consultantId, agencyId);

      return agencyList != null && agencyList.size() > 0;
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException("Database error while getting agency id data set for consultant");
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
      logService.logDatabaseError(ex);
      throw new ServiceException("Database error while loading consultant list");
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
      logService.logDatabaseInconsistency(error);
      throw new ServiceException(error);
    }
    if (agency.getConsultant() == null) {
      error = String.format(
          "Database inconsistency: could not get assigned consultant for agency with id %s",
          agency.getAgencyId());
      logService.logDatabaseInconsistency(error);
      throw new ServiceException(error);
    }

    return new ConsultantResponseDTO(agency.getConsultant().getId(),
        agency.getConsultant().getFirstName(), agency.getConsultant().getLastName());
  }

}

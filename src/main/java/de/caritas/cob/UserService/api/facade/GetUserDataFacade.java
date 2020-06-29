package de.caritas.cob.UserService.api.facade;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.helper.SessionDataHelper;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.model.UserDataResponseDTO;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

/**
 * Facade to encapsulate getting the agency data for the corresponding user
 *
 */

@Service
public class GetUserDataFacade {

  private final LogService logService;
  private final AgencyServiceHelper agencyServiceHelper;
  private final SessionService sessionService;
  private final SessionDataHelper sessionDataHelper;

  @Autowired
  public GetUserDataFacade(LogService logService, AgencyServiceHelper agencyServiceHelper,
      SessionService sessionService, SessionDataHelper sessionDataHelper) {
    this.logService = logService;
    this.agencyServiceHelper = agencyServiceHelper;
    this.sessionService = sessionService;
    this.sessionDataHelper = sessionDataHelper;
  }

  /**
   * Assign a {@link Consultant} repository information to an {@link UserDataResponseDTO} and get
   * the {@link AgencyDTO} for the consultant's assigned agencies.
   * 
   * @param consultant
   * @return
   */
  public UserDataResponseDTO getConsultantData(Consultant consultant) {
    List<AgencyDTO> agencyDTOs = null;

    if (!consultant.getConsultantAgencies().isEmpty()) {
      try {
        agencyDTOs = consultant.getConsultantAgencies().stream()
            .map(agency -> agencyServiceHelper.getAgency(agency.getAgencyId()))
            .collect(Collectors.toList());
      } catch (AgencyServiceHelperException agencyServiceHelperException) {
        logService.logAgencyServiceHelperException(String
            .format("Error while getting agencies of consultant with id %s", consultant.getId()),
            agencyServiceHelperException);

        return null;
      }
    }

    return new UserDataResponseDTO(consultant.getId(), consultant.getUsername(),
        consultant.getFirstName(), consultant.getLastName(), consultant.getEmail(),
        consultant.isAbsent(), consultant.isLanguageFormal(), consultant.getAbsenceMessage(),
        consultant.isTeamConsultant(), agencyDTOs, null, null, null);
  }

  /**
   * Returns the session data for every consulting type of the given {@link User}
   * 
   * @param user
   * @return UserDataResponseDTO
   */
  public UserDataResponseDTO getUserData(User user) {
    List<AgencyDTO> agencyDTOs = null;
    if (!user.getUserAgencies().isEmpty()) {
      try {
        agencyDTOs = user.getUserAgencies().stream()
            .map(agency -> agencyServiceHelper.getAgency(agency.getAgencyId()))
            .collect(Collectors.toList());
      } catch (AgencyServiceHelperException agencyServiceHelperException) {
        logService.logAgencyServiceHelperException(String
                .format("Error while getting agencies of user with id %s", user.getUserId()),
            agencyServiceHelperException);
        return null;
      }
    }
    UserDataResponseDTO responseDTO = new UserDataResponseDTO(user.getUserId(), user.getUsername(),
        null, null, null, false, user.isLanguageFormal(), null, false, agencyDTOs, null, null,
        null);
    LinkedHashMap<String, Object> sessionData = new LinkedHashMap<String, Object>();

    for (ConsultingType type : ConsultingType.values()) {
      List<Session> sessionList = sessionService.getSessionsForUserByConsultingType(user, type);
      LinkedHashMap<String, Object> typeSessionData = sessionList.size() > 0
          ? sessionDataHelper.getSessionDataMapFromSession(sessionList.get(0))
          : null;
      sessionData.put(Integer.toString(type.getValue()), typeSessionData);
    }

    responseDTO.setSessionData(sessionData);

    return responseDTO;
  }

}

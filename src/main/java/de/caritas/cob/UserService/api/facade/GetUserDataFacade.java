package de.caritas.cob.UserService.api.facade;

import de.caritas.cob.UserService.api.repository.userAgency.UserAgency;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

/**
 * Facade to encapsulate getting the agency data for the corresponding user
 *
 */

@Service
public class GetUserDataFacade {

  private final LogService logService;
  private final AgencyServiceHelper agencyServiceHelper;
  private final SessionDataHelper sessionDataHelper;

  @Autowired
  public GetUserDataFacade(LogService logService, AgencyServiceHelper agencyServiceHelper,
      SessionDataHelper sessionDataHelper) {
    this.logService = logService;
    this.agencyServiceHelper = agencyServiceHelper;
    this.sessionDataHelper = sessionDataHelper;
  }

  /**
   * Assign a {@link Consultant} repository information to an {@link UserDataResponseDTO} and get
   * the {@link AgencyDTO} for the consultant's assigned agencies.
   *
   * @param consultant - {@link Consultant}
   * @return UserDataResponseDTO - {@link UserDataResponseDTO}
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
   * @param user - {@link User}
   * @return UserDataResponseDTO - {@link UserDataResponseDTO}
   */
  public UserDataResponseDTO getUserData(User user) {
    UserDataResponseDTO responseDTO = new UserDataResponseDTO(user.getUserId(), user.getUsername(),
        null, null, null, false, user.isLanguageFormal(), null, false, null, null, null,
        null);

    responseDTO.setConsultingTypes(getConsultingTypes(user));

    return responseDTO;
  }

  /**
   * @param user - {@link User }
   * @return LinkedHashMap<String, Object> - HashMap with all consultingtypes and data
   */
  private LinkedHashMap<String, Object> getConsultingTypes(User user) {

    LinkedHashMap<String, Object> consultingTypes = new LinkedHashMap<>();
    Set<Session> sessionList =
        user.getSessions() != null ? user.getSessions() : Collections.emptySet();
    List<Long> agencyIds = new ArrayList<>();
    agencyIds.addAll(getAgencyIdsInSessions(sessionList));
    agencyIds.addAll(getAgencyIdsInDatabase(user));
    List<AgencyDTO> agencyDTOs;
    try {
      agencyDTOs = agencyServiceHelper.getAgencies(agencyIds);
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      logService.logAgencyServiceHelperException(String
              .format("Error while getting agencies of user with id %s", agencyIds),
          agencyServiceHelperException);

      return null;
    }

    for (ConsultingType type : ConsultingType.values()) {
      consultingTypes.put(Integer.toString(type.getValue()),
          getConsultingTypeData(type, sessionList, agencyDTOs));
    }

    return consultingTypes;
  }

  /**
   * @param type        - {@link ConsultingType}
   * @param sessionList - {@link User#getSessions()}
   * @param agencyDTOs  - List of {@link AgencyDTO}
   * @return LinkedHashMap<String, Object> - Hashmap containing data (SessionData,isRegistered,agency)
   */
  private LinkedHashMap<String, Object> getConsultingTypeData(ConsultingType type,
      Set<Session> sessionList, List<AgencyDTO> agencyDTOs) {

    LinkedHashMap<String, Object> consultingTypeData = new LinkedHashMap<>();
    Optional<Session> consultingTypeSession = sessionList.stream()
        .filter(session -> session.getConsultingType() == type)
        .findFirst();
    Optional<LinkedHashMap<String, Object>> consultingTypeSessionData = consultingTypeSession
        .map(sessionDataHelper::getSessionDataMapFromSession);

    consultingTypeData.put("sessionData",
        consultingTypeSessionData.orElse(null));
    consultingTypeData.put("isRegistered", consultingTypeSession.isPresent());
    consultingTypeData.put("agency",
        agencyDTOs.stream().filter(agencyDTO -> agencyDTO.getConsultingType() == type).findFirst()
            .orElse(null));

    return consultingTypeData;
  }

  /**
   * @param sessionList - {@link User#getSessions()}
   * @return List<Long> - list of agencyIds
   */
  private List<Long> getAgencyIdsInSessions(Set<Session> sessionList) {
    if (sessionList != null) {
      return sessionList.stream().map(Session::getAgencyId).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  /**
   * @param user - {@link User}
   * @return - list of agencyIds
   */
  private List<Long> getAgencyIdsInDatabase(User user) {
    if (user.getUserAgencies() != null) {
      return user.getUserAgencies().stream().map(UserAgency::getAgencyId)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }
}

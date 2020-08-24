package de.caritas.cob.userservice.api.facade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgency;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;

/**
 * Facade to encapsulate getting the agency data for the corresponding user
 */

@Service
public class GetUserDataFacade {

  private final AgencyServiceHelper agencyServiceHelper;
  private final SessionDataHelper sessionDataHelper;

  @Autowired
  public GetUserDataFacade(AgencyServiceHelper agencyServiceHelper,
      SessionDataHelper sessionDataHelper) {
    this.agencyServiceHelper = agencyServiceHelper;
    this.sessionDataHelper = sessionDataHelper;
  }

  /**
   * Assign a {@link Consultant} repository information to an {@link UserDataResponseDTO} and get
   * the {@link AgencyDTO} for the consultant's assigned agencies.
   *
   * @param consultant {@link Consultant}
   * @return UserDataResponseDTO {@link UserDataResponseDTO}
   */
  public UserDataResponseDTO getConsultantData(Consultant consultant) {
    List<AgencyDTO> agencyDTOs = null;

    if (!consultant.getConsultantAgencies().isEmpty()) {
      try {
        agencyDTOs = consultant.getConsultantAgencies().stream()
            .map(agency -> agencyServiceHelper.getAgency(agency.getAgencyId()))
            .collect(Collectors.toList());
      } catch (AgencyServiceHelperException agencyServiceHelperException) {
        LogService.logAgencyServiceHelperException(String
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
   * @param user {@link User}
   * @return UserDataResponseDTO {@link UserDataResponseDTO}
   */
  public UserDataResponseDTO getUserData(User user) {
    UserDataResponseDTO responseDTO = new UserDataResponseDTO(user.getUserId(), user.getUsername(),
        null, null, null, false, user.isLanguageFormal(), null, false, null, null, null, null);

    responseDTO.setConsultingTypes(getConsultingTypes(user));

    return responseDTO;
  }

  /**
   * Returns information for every consulting type of the given {@link User}
   *
   * @param user {@link User }
   * @return LinkedHashMap<String, Object> HashMap with all consultingtypes and data
   */
  private LinkedHashMap<String, Object> getConsultingTypes(User user) {

    Set<Session> sessionList =
        user.getSessions() != null ? user.getSessions() : Collections.emptySet();
    List<Long> agencyIds = getAgencyIds(user, sessionList);
    List<AgencyDTO> agencyDTOs;
    try {
      agencyDTOs = agencyServiceHelper.getAgencies(agencyIds);
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      throw new ServiceException(
          String.format("Invalid agencyIds: %s for user with id %s", agencyIds, user.getUserId()));
    }
    LinkedHashMap<String, Object> consultingTypes = new LinkedHashMap<>();
    for (ConsultingType type : ConsultingType.values()) {
      consultingTypes.put(Integer.toString(type.getValue()),
          getConsultingTypeData(type, sessionList, agencyDTOs));
    }

    return consultingTypes;
  }

  /**
   * Returns a {@link LinkedHashMap} with user information for the given consulting type.
   *
   * @param type {@link ConsultingType}
   * @param sessionList List of the user's {@link Session}s
   * @param agencyDTOs List of {@link AgencyDTO} that the user is registered to
   * @return LinkedHashMap<String, Object> {@link LinkedHashMap} containing user data
   * (sessionData,isRegistered,agency)
   */
  private LinkedHashMap<String, Object> getConsultingTypeData(ConsultingType type,
      Set<Session> sessionList, List<AgencyDTO> agencyDTOs) {

    LinkedHashMap<String, Object> consultingTypeData = new LinkedHashMap<>();
    Optional<Session> consultingTypeSession =
        sessionList.stream().filter(session -> session.getConsultingType() == type).findFirst();
    Optional<LinkedHashMap<String, Object>> consultingTypeSessionData =
        consultingTypeSession.map(sessionDataHelper::getSessionDataMapFromSession);
    Optional<AgencyDTO> agency =
        agencyDTOs.stream().filter(agencyDTO -> agencyDTO.getConsultingType() == type).findFirst();

    consultingTypeData.put("sessionData", consultingTypeSessionData.orElse(null));
    consultingTypeData.put("isRegistered", agency.isPresent());
    consultingTypeData.put("agency", agency.orElse(null));

    return consultingTypeData;
  }

  /**
   * Returns List of agency IDs from provided Set of {@link Session}.
   *
   * @param sessionList {@link User#getSessions()}
   * @return List<Long> list of agencyIds
   */
  private List<Long> getAgencyIdsFromSessions(Set<Session> sessionList) {
    if (sessionList != null) {
      return sessionList.stream().map(Session::getAgencyId).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  /**
   * Returns List of agency IDs from provided {@link User}.
   *
   * @param user - {@link User}
   * @return list of agencyIds
   */
  private List<Long> getAgencyIdsFromUser(User user) {
    if (user.getUserAgencies() != null) {
      return user.getUserAgencies().stream().map(UserAgency::getAgencyId)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  /**
   * Returns List of agency IDs from provided {@link User} and Set of {@link Session}.
   *
   * @param user {@link User}
   * @param sessionList {@link User#getSessions()}
   * @return list of agencyIds
   */
  private List<Long> getAgencyIds(User user, Set<Session> sessionList) {
    List<Long> agencyIds = new ArrayList<>();
    agencyIds.addAll(getAgencyIdsFromSessions(sessionList));
    agencyIds.addAll(getAgencyIdsFromUser(user));
    return agencyIds;
  }
}

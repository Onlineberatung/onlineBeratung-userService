package de.caritas.cob.userservice.api.facade.userdata;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgency;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provider for asker information.
 */
@Component
@RequiredArgsConstructor
public class AskerDataProvider {

  private final @NonNull AgencyServiceHelper agencyServiceHelper;
  private final @NonNull SessionDataProvider sessionDataProvider;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  @Value("${keycloakService.user.dummySuffix}")
  private String emailDummySuffix;

  /**
   * Retrieve the user data of an asker, e.g. username, email, name, ...
   *
   * @param user a {@link User} instance
   * @return the user data
   */
  public UserDataResponseDTO retrieveData(User user) {
    String email = observeUserEmailAddress(user);
    UserDataResponseDTO responseDTO = new UserDataResponseDTO(user.getUserId(), user.getUsername(),
        null, null, email, false, user.isLanguageFormal(), null, false, null,
        authenticatedUser.getRoles(), authenticatedUser.getGrantedAuthorities(), null);

    responseDTO.setConsultingTypes(getConsultingTypes(user));

    return responseDTO;
  }

  private String observeUserEmailAddress(User user) {
    return user.getEmail().endsWith(this.emailDummySuffix) ? null : user.getEmail();
  }

  private LinkedHashMap<String, Object> getConsultingTypes(User user) {

    Set<Session> sessionList = SetUtils.emptyIfNull(user.getSessions());
    List<Long> agencyIds = mergeAgencyIdsFromSessionAndUser(user, sessionList);
    List<AgencyDTO> agencyDTOs = fetchAgenciesViaAgencyService(user, agencyIds);
    LinkedHashMap<String, Object> consultingTypes = new LinkedHashMap<>();
    for (int type : consultingTypeManager.getAllConsultingIds()) {
      consultingTypes.put(Integer.toString(type),
          getConsultingTypeData(type, sessionList, agencyDTOs));
    }

    return consultingTypes;
  }

  private List<AgencyDTO> fetchAgenciesViaAgencyService(User user, List<Long> agencyIds) {
    try {
      return agencyServiceHelper.getAgencies(agencyIds);
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      throw new InternalServerErrorException(
          String.format("Invalid agencyIds: %s for user with id %s", agencyIds, user.getUserId()));
    }
  }

  private LinkedHashMap<String, Object> getConsultingTypeData(int consultingType,
      Set<Session> sessionList, List<AgencyDTO> agencyDTOs) {

    LinkedHashMap<String, Object> consultingTypeData = new LinkedHashMap<>();
    Optional<Session> consultingTypeSession = findSessionByConsultingType(consultingType,
        sessionList);
    Optional<Map<String, Object>> consultingTypeSessionData =
        consultingTypeSession.map(sessionDataProvider::getSessionDataMapFromSession);
    Optional<AgencyDTO> agency = findAgencyByConsultingType(consultingType, agencyDTOs);

    consultingTypeData.put("sessionData", consultingTypeSessionData.orElse(null));
    consultingTypeData.put("isRegistered", agency.isPresent());
    consultingTypeData.put("agency", agency.orElse(null));

    return consultingTypeData;
  }

  private Optional<AgencyDTO> findAgencyByConsultingType(int consultingId,
      List<AgencyDTO> agencyDTOs) {
    return agencyDTOs.stream()
        .filter(agencyDTO -> agencyDTO.getConsultingId() == consultingId)
        .findFirst();
  }

  private Optional<Session> findSessionByConsultingType(int consultingType,
      Set<Session> sessionList) {
    return sessionList.stream()
        .filter(session -> session.getConsultingId() == consultingType)
        .findFirst();
  }

  private List<Long> mergeAgencyIdsFromSessionAndUser(User user, Set<Session> sessionList) {
    List<Long> agencyIds = new ArrayList<>();
    agencyIds.addAll(collectAgencyIdsFromSessions(sessionList));
    agencyIds.addAll(collectAgencyIdsFromUser(user));
    return agencyIds;
  }

  private List<Long> collectAgencyIdsFromSessions(Set<Session> sessionList) {
    return CollectionUtils.isNotEmpty(sessionList) ? sessionList.stream().map(Session::getAgencyId)
        .collect(Collectors.toList()) : Collections.emptyList();
  }

  private List<Long> collectAgencyIdsFromUser(User user) {
    return CollectionUtils.isNotEmpty(user.getUserAgencies()) ? user.getUserAgencies().stream()
        .map(UserAgency::getAgencyId)
        .collect(Collectors.toList()) : Collections.emptyList();
  }

}

package de.caritas.cob.userservice.api.facade.userdata;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgency;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provider for asker information.
 */
@Component
@RequiredArgsConstructor
public class AskerDataProvider {

  private final @NonNull AgencyService agencyService;
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
    return UserDataResponseDTO.builder()
        .userId(user.getUserId())
        .userName(user.getUsername())
        .email(observeUserEmailAddress(user))
        .isAbsent(false)
        .isFormalLanguage(user.isLanguageFormal())
        .isInTeamAgency(false)
        .userRoles(authenticatedUser.getRoles())
        .grantedAuthorities(authenticatedUser.getGrantedAuthorities())
        .consultingTypes(getConsultingTypes(user))
        .hasAnonymousConversations(false)
        .build();
  }

  private String observeUserEmailAddress(User user) {
    return user.getEmail().endsWith(this.emailDummySuffix) ? null : user.getEmail();
  }

  private LinkedHashMap<String, Object> getConsultingTypes(User user) {

    Set<Session> sessionList = SetUtils.emptyIfNull(user.getSessions());
    List<Long> agencyIds = mergeAgencyIdsFromSessionAndUser(user, sessionList);
    List<AgencyDTO> agencyDTOs = this.agencyService.getAgencies(agencyIds);
    LinkedHashMap<String, Object> consultingTypes = new LinkedHashMap<>();
    for (int type : consultingTypeManager.getAllConsultingTypeIds()) {
      consultingTypes.put(Integer.toString(type),
          getConsultingTypeData(type, sessionList, agencyDTOs));
    }

    return consultingTypes;
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

  private Optional<AgencyDTO> findAgencyByConsultingType(int consultingTypeId,
      List<AgencyDTO> agencyDTOs) {
    return agencyDTOs.stream()
        .filter(agencyDTO -> agencyDTO.getConsultingType() == consultingTypeId)
        .findFirst();
  }

  private Optional<Session> findSessionByConsultingType(int consultingTypeId,
      Set<Session> sessionList) {
    return sessionList.stream()
        .filter(session -> session.getConsultingTypeId() == consultingTypeId)
        .findFirst();
  }

  private List<Long> mergeAgencyIdsFromSessionAndUser(User user, Set<Session> sessionList) {
    List<Long> agencyIds = new ArrayList<>();
    agencyIds.addAll(collectAgencyIdsFromSessions(sessionList));
    agencyIds.addAll(collectAgencyIdsFromUser(user));
    return agencyIds;
  }

  private List<Long> collectAgencyIdsFromSessions(Set<Session> sessionList) {
    return CollectionUtils.isNotEmpty(sessionList) ? sessionList.stream()
        .map(Session::getAgencyId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList()) : Collections.emptyList();
  }

  private List<Long> collectAgencyIdsFromUser(User user) {
    return CollectionUtils.isNotEmpty(user.getUserAgencies()) ? user.getUserAgencies().stream()
        .map(UserAgency::getAgencyId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList()) : Collections.emptyList();
  }

}

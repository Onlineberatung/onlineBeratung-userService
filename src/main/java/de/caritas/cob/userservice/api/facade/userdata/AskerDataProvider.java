package de.caritas.cob.userservice.api.facade.userdata;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgency;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provider for asker information
 */
@Component
public class AskerDataProvider {

  private final AgencyServiceHelper agencyServiceHelper;
  private final SessionDataHelper sessionDataHelper;
  private final AuthenticatedUser authenticatedUser;
  @Value("${keycloakService.user.dummySuffix}")
  private String emailDummySuffix;

  public AskerDataProvider(AgencyServiceHelper agencyServiceHelper,
      SessionDataHelper sessionDataHelper, AuthenticatedUser authenticatedUser) {
    this.agencyServiceHelper = requireNonNull(agencyServiceHelper);
    this.sessionDataHelper = requireNonNull(sessionDataHelper);
    this.authenticatedUser = requireNonNull(authenticatedUser);
  }

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
    List<Long> agencyIds = getAgencyIds(user, sessionList);
    List<AgencyDTO> agencyDTOs;
    try {
      agencyDTOs = agencyServiceHelper.getAgencies(agencyIds);
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      throw new InternalServerErrorException(
          String.format("Invalid agencyIds: %s for user with id %s", agencyIds, user.getUserId()));
    }
    LinkedHashMap<String, Object> consultingTypes = new LinkedHashMap<>();
    for (ConsultingType type : ConsultingType.values()) {
      consultingTypes.put(Integer.toString(type.getValue()),
          getConsultingTypeData(type, sessionList, agencyDTOs));
    }

    return consultingTypes;
  }

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

  private List<Long> getAgencyIds(User user, Set<Session> sessionList) {
    List<Long> agencyIds = new ArrayList<>();
    agencyIds.addAll(getAgencyIdsFromSessions(sessionList));
    agencyIds.addAll(getAgencyIdsFromUser(user));
    return agencyIds;
  }

  private List<Long> getAgencyIdsFromSessions(Set<Session> sessionList) {
    if (nonNull(sessionList)) {
      return sessionList.stream().map(Session::getAgencyId).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private List<Long> getAgencyIdsFromUser(User user) {
    if (nonNull(user.getUserAgencies())) {
      return user.getUserAgencies().stream().map(UserAgency::getAgencyId)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

}

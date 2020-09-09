package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgency;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.ValidatedUserAccountProvider;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate getting the agency data for the corresponding user
 */

@Service
public class GetUserDataFacade {

  private final AgencyServiceHelper agencyServiceHelper;
  private final SessionDataHelper sessionDataHelper;
  private final AuthenticatedUser authenticatedUser;
  private final ValidatedUserAccountProvider userAccountProvider;

  public GetUserDataFacade(AgencyServiceHelper agencyServiceHelper,
      SessionDataHelper sessionDataHelper, AuthenticatedUser authenticatedUser,
      ValidatedUserAccountProvider userAccountProvider) {
    this.agencyServiceHelper = requireNonNull(agencyServiceHelper);
    this.sessionDataHelper = requireNonNull(sessionDataHelper);
    this.authenticatedUser = requireNonNull(authenticatedUser);
    this.userAccountProvider = requireNonNull(userAccountProvider);
  }

  /**
   * Returns the user data of the authenticated user preferred by role consultant.
   *
   * @return UserDataResponseDTO {@link UserDataResponseDTO}
   */
  public UserDataResponseDTO buildUserDataPreferredByConsultantRole() {
    Set<String> roles = authenticatedUser.getRoles();

    if (roles.contains(UserRole.CONSULTANT.getValue())) {
      return getConsultantData(userAccountProvider.retrieveValidatedConsultant());
    } else if (roles.contains(UserRole.USER.getValue())) {
      return getUserData(userAccountProvider.retrieveValidatedUser());
    } else {
      throw new InternalServerErrorException(
          String.format("User with id %s has neither Consultant-Role, nor User-Role .",
          authenticatedUser.getUserId()));
    }
  }

  private UserDataResponseDTO getConsultantData(Consultant consultant) {
    List<AgencyDTO> agencyDTOs = null;

    if (!consultant.getConsultantAgencies().isEmpty()) {
      agencyDTOs = consultant.getConsultantAgencies().stream()
          .map(this::fromConsultantAgency)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }

    if (CollectionUtils.isEmpty(agencyDTOs)) {
      throw new InternalServerErrorException(String.format("No agency available for "
          + "consultant %s", consultant.getId()));
    }

    return new UserDataResponseDTO(consultant.getId(), consultant.getUsername(),
        consultant.getFirstName(), consultant.getLastName(), consultant.getEmail(),
        consultant.isAbsent(), consultant.isLanguageFormal(), consultant.getAbsenceMessage(),
        consultant.isTeamConsultant(), agencyDTOs, authenticatedUser.getRoles(),
        authenticatedUser.getGrantedAuthorities(), null);
  }

  private AgencyDTO fromConsultantAgency(ConsultantAgency consultantAgency) {
    try {
      return this.agencyServiceHelper.getAgency(consultantAgency.getAgencyId());
    } catch (AgencyServiceHelperException e) {
      LogService.logAgencyServiceHelperException(String
              .format("Error while getting agencies of consultant with id %s",
                  consultantAgency.getId()), e);
    }
    return null;
  }

  private UserDataResponseDTO getUserData(User user) {
    UserDataResponseDTO responseDTO = new UserDataResponseDTO(user.getUserId(), user.getUsername(),
        null, null, null, false, user.isLanguageFormal(), null, false, null,
        authenticatedUser.getRoles(), authenticatedUser.getGrantedAuthorities(), null);

    responseDTO.setConsultingTypes(getConsultingTypes(user));

    return responseDTO;
  }

  private LinkedHashMap<String, Object> getConsultingTypes(User user) {

    Set<Session> sessionList =
        user.getSessions() != null ? user.getSessions() : Collections.emptySet();
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

  private List<Long> getAgencyIdsFromSessions(Set<Session> sessionList) {
    if (sessionList != null) {
      return sessionList.stream().map(Session::getAgencyId).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private List<Long> getAgencyIdsFromUser(User user) {
    if (user.getUserAgencies() != null) {
      return user.getUserAgencies().stream().map(UserAgency::getAgencyId)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private List<Long> getAgencyIds(User user, Set<Session> sessionList) {
    List<Long> agencyIds = new ArrayList<>();
    agencyIds.addAll(getAgencyIdsFromSessions(sessionList));
    agencyIds.addAll(getAgencyIdsFromUser(user));
    return agencyIds;
  }
}

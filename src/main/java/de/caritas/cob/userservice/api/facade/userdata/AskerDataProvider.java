package de.caritas.cob.userservice.api.facade.userdata;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDataResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDataResponseDTO.UserDataResponseDTOBuilder;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.session.SessionMapper;
import de.caritas.cob.userservice.api.service.session.SessionService;
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
import org.springframework.stereotype.Component;

/** Provider for asker information. */
@Component
@RequiredArgsConstructor
public class AskerDataProvider {

  private final @NonNull AgencyService agencyService;
  private final @NonNull SessionDataProvider sessionDataProvider;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull IdentityClientConfig identityClientConfig;

  private final @NonNull SessionService sessionService;

  private final @NonNull EmailNotificationMapper emailNotificationMapper;

  /**
   * Retrieve the user data of an asker, e.g. username, email, name, ...
   *
   * @param user a {@link User} instance
   * @return the user data
   */
  public UserDataResponseDTO retrieveData(User user) {
    var userDataResponseDTOBuilder =
        UserDataResponseDTO.builder()
            .userId(user.getUserId())
            .userName(user.getUsername())
            .email(observeUserEmailAddress(user))
            .isAbsent(false)
            .encourage2fa(user.getEncourage2fa())
            .isFormalLanguage(user.isLanguageFormal())
            .preferredLanguage(LanguageCode.fromValue(user.getLanguageCode().toString()))
            .isInTeamAgency(false)
            .userRoles(authenticatedUser.getRoles())
            .grantedAuthorities(authenticatedUser.getGrantedAuthorities())
            .consultingTypes(getConsultingTypes(user))
            .hasAnonymousConversations(false)
            .hasArchive(false)
            .dataPrivacyConfirmation(user.getDataPrivacyConfirmation())
            .termsAndConditionsConfirmation(user.getTermsAndConditionsConfirmation())
            .emailNotifications(emailNotificationMapper.toEmailNotificationsDTO(user));

    enrichWithUserSessions(user, userDataResponseDTOBuilder);
    return userDataResponseDTOBuilder.build();
  }

  private void enrichWithUserSessions(
      User user, UserDataResponseDTOBuilder userDataResponseDTOBuilder) {
    List<Session> sessionsByUser = sessionService.findSessionsByUser(user);
    if (CollectionUtils.isNotEmpty(sessionsByUser)) {
      SessionMapper sessionMapper = new SessionMapper();
      userDataResponseDTOBuilder.sessions(
          sessionsByUser.stream()
              .map(sessionMapper::convertToSessionDTO)
              .collect(Collectors.toSet()));
    }
  }

  private String observeUserEmailAddress(User user) {
    return user.getEmail().endsWith(identityClientConfig.getEmailDummySuffix())
        ? null
        : user.getEmail();
  }

  private LinkedHashMap<String, Object> getConsultingTypes(User user) {

    Set<Session> sessionList = SetUtils.emptyIfNull(user.getSessions());
    List<Long> agencyIds = mergeAgencyIdsFromSessionAndUser(user, sessionList);
    List<AgencyDTO> agencyDTOs = this.agencyService.getAgencies(agencyIds);
    LinkedHashMap<String, Object> consultingTypes = new LinkedHashMap<>();
    for (int type : consultingTypeManager.getAllConsultingTypeIds()) {
      consultingTypes.put(
          Integer.toString(type), getConsultingTypeData(type, sessionList, agencyDTOs));
    }

    return consultingTypes;
  }

  private LinkedHashMap<String, Object> getConsultingTypeData(
      int consultingType, Set<Session> sessionList, List<AgencyDTO> agencyDTOs) {

    LinkedHashMap<String, Object> consultingTypeData = new LinkedHashMap<>();
    Optional<Session> consultingTypeSession =
        findSessionByConsultingType(consultingType, sessionList);
    Optional<Map<String, Object>> consultingTypeSessionData =
        consultingTypeSession.map(sessionDataProvider::getSessionDataMapFromSession);
    Optional<AgencyDTO> agency = findAgencyByConsultingType(consultingType, agencyDTOs);

    consultingTypeData.put("sessionData", consultingTypeSessionData.orElse(null));
    consultingTypeData.put("isRegistered", agency.isPresent());
    consultingTypeData.put("agency", agency.orElse(null));

    return consultingTypeData;
  }

  private Optional<AgencyDTO> findAgencyByConsultingType(
      int consultingTypeId, List<AgencyDTO> agencyDTOs) {
    return agencyDTOs.stream()
        .filter(agencyDTO -> agencyDTO.getConsultingType() == consultingTypeId)
        .findFirst();
  }

  private Optional<Session> findSessionByConsultingType(
      int consultingTypeId, Set<Session> sessionList) {
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
    return CollectionUtils.isNotEmpty(sessionList)
        ? sessionList.stream()
            .map(Session::getAgencyId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        : Collections.emptyList();
  }

  private List<Long> collectAgencyIdsFromUser(User user) {
    return CollectionUtils.isNotEmpty(user.getUserAgencies())
        ? user.getUserAgencies().stream()
            .map(UserAgency::getAgencyId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        : Collections.emptyList();
  }
}

package de.caritas.cob.userservice.api.facade.userdata;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDataResponseDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Language;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Provider for consultant information.
 */
@Component
@RequiredArgsConstructor
public class ConsultantDataProvider {

  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull AgencyService agencyService;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull SessionRepository sessionRepository;

  /**
   * Retrieve the user data of a consultant, e.g. agencies, absence-state, username, name, ...
   *
   * @param consultant a {@link Consultant} instance
   * @return the user data
   */
  public UserDataResponseDTO retrieveData(Consultant consultant) {
    if (isEmpty(consultant.getConsultantAgencies())) {
      throw new InternalServerErrorException(
          String.format("No agency available for consultant %s", consultant.getId())
      );
    }

    return userDataResponseDtoOf(consultant);
  }

  private List<Long> agencyIdsOf(Set<ConsultantAgency> agencies) {
    return agencies.stream()
        .map(ConsultantAgency::getAgencyId)
        .collect(Collectors.toList());
  }

  private UserDataResponseDTO userDataResponseDtoOf(Consultant consultant) {

    return UserDataResponseDTO.builder()
        .userId(consultant.getId())
        .userName(consultant.getUsername())
        .firstName(consultant.getFirstName())
        .lastName(consultant.getLastName())
        .email(consultant.getEmail())
        .isAbsent(consultant.isAbsent())
        .isFormalLanguage(consultant.isLanguageFormal())
        .languages(languageStringsOf(consultant.getLanguages()))
        .encourage2fa(consultant.getEncourage2fa())
        .absenceMessage(consultant.getAbsenceMessage())
        .isInTeamAgency(consultant.isTeamConsultant())
        .agencies(agencyDTOsOf(consultant))
        .userRoles(authenticatedUser.getRoles())
        .grantedAuthorities(authenticatedUser.getGrantedAuthorities())
        .isWalkThroughEnabled(consultant.isWalkThroughEnabled())
        .hasAnonymousConversations(
            hasAtLeastOneTypeWithAllowedAnonymousConversations(agencyDTOsOf(consultant))
        )
        .hasArchive(hasArchive(consultant))
        .build();
  }

  private List<AgencyDTO> agencyDTOsOf(Consultant consultant) {
    return agencyService.getAgencies(
        agencyIdsOf(consultant.getConsultantAgencies())
    );
  }

  private Set<String> languageStringsOf(Set<Language> languages) {
    return languages.stream()
        .map(Language::getLanguageCode)
        .map(LanguageCode::name)
        .collect(Collectors.toSet());
  }

  private boolean hasAtLeastOneTypeWithAllowedAnonymousConversations(List<AgencyDTO> agencyDTOS) {
    return agencyDTOS.stream()
        .map(AgencyDTO::getConsultingType)
        .map(this.consultingTypeManager::getConsultingTypeSettings)
        .anyMatch(this::hasAnonymousConversationAllowed);
  }

  private boolean hasAnonymousConversationAllowed(
      ExtendedConsultingTypeResponseDTO consultingTypeResponseDTO) {
    return nonNull(consultingTypeResponseDTO) && isTrue(
        consultingTypeResponseDTO.getIsAnonymousConversationAllowed());
  }

  private boolean hasArchive(Consultant consultant) {
    return hasAtLeastOneRegisteredSessionInProgressOrArchive(consultant) || consultant
        .isTeamConsultant();
  }

  private boolean hasAtLeastOneRegisteredSessionInProgressOrArchive(Consultant consultant) {
    Long count = sessionRepository.countByConsultantAndStatusInAndRegistrationType(consultant,
        List.of(SessionStatus.IN_PROGRESS, SessionStatus.IN_ARCHIVE), RegistrationType.REGISTERED);
    return nonNull(count) && count > 0L;
  }

}

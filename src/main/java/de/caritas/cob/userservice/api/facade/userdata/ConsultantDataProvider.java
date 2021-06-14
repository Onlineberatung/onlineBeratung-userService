package de.caritas.cob.userservice.api.facade.userdata;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springframework.util.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
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

  /**
   * Retrieve the user data of a consultant, e.g. agencies, absence-state, username, name, ...
   *
   * @param consultant a {@link Consultant} instance
   * @return the user data
   */
  public UserDataResponseDTO retrieveData(Consultant consultant) {
    var agencyDTOs = obtainAgencies(consultant);
    return createUserDataResponseDTO(consultant, agencyDTOs);
  }

  private List<AgencyDTO> obtainAgencies(Consultant consultant) {
    if (isEmpty(consultant.getConsultantAgencies())) {
      throw new InternalServerErrorException(
          String.format("No agency available for consultant %s", consultant.getId()));
    }
    var agencyIds = obtainAgencyIds(consultant);

    return this.agencyService.getAgencies(agencyIds);
  }

  private List<Long> obtainAgencyIds(Consultant consultant) {
    return consultant.getConsultantAgencies().stream()
        .map(ConsultantAgency::getAgencyId)
        .collect(Collectors.toList());
  }

  private UserDataResponseDTO createUserDataResponseDTO(Consultant consultant,
      List<AgencyDTO> agencyDTOs) {
    return UserDataResponseDTO.builder()
        .userId(consultant.getId())
        .userName(consultant.getUsername())
        .firstName(consultant.getFirstName())
        .lastName(consultant.getLastName())
        .email(consultant.getEmail())
        .isAbsent(consultant.isAbsent())
        .isFormalLanguage(consultant.isLanguageFormal())
        .absenceMessage(consultant.getAbsenceMessage())
        .isInTeamAgency(consultant.isTeamConsultant())
        .agencies(agencyDTOs)
        .userRoles(authenticatedUser.getRoles())
        .grantedAuthorities(authenticatedUser.getGrantedAuthorities())
        .hasAnonymousConversations(hasAtLeastOneTypeWithAllowedAnonymousConversations(agencyDTOs))
        .build();
  }

  private boolean hasAtLeastOneTypeWithAllowedAnonymousConversations(
      List<AgencyDTO> agencyDTOS) {
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

}

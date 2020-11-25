package de.caritas.cob.userservice.api.facade.userdata;

import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Provider for consultant information
 */
@Component
public class ConsultantDataProvider {

  private final AuthenticatedUser authenticatedUser;
  private final AgencyServiceHelper agencyServiceHelper;

  public ConsultantDataProvider(AuthenticatedUser authenticatedUser,
      AgencyServiceHelper agencyServiceHelper) {
    this.authenticatedUser = requireNonNull(authenticatedUser);
    this.agencyServiceHelper = requireNonNull(agencyServiceHelper);
  }

  /**
   * Retrieve the user data of a consultant, e.g. agencies, absence-state, username, name, ...
   *
   * @param consultant a {@link Consultant} instance
   * @return the user data
   */
  public UserDataResponseDTO retrieveData(Consultant consultant) {

    List<AgencyDTO> agencyDTOs = obtainAgencies(consultant);

    if (CollectionUtils.isEmpty(agencyDTOs)) {
      throw new InternalServerErrorException(String.format("No agency available for "
          + "consultant %s", consultant.getId()));
    }

    return createUserDataResponseDTO(consultant, agencyDTOs);

  }

  private List<AgencyDTO> obtainAgencies(
      de.caritas.cob.userservice.api.repository.consultant.Consultant consultant) {
    return consultant.getConsultantAgencies().isEmpty() ? null
        : consultant.getConsultantAgencies().stream()
            .map(this::fetchAgencyViaAgencyService)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  private UserDataResponseDTO createUserDataResponseDTO(
      de.caritas.cob.userservice.api.repository.consultant.Consultant consultant,
      List<AgencyDTO> agencyDTOs) {
    return new UserDataResponseDTO(consultant.getId(), consultant.getUsername(),
        consultant.getFirstName(), consultant.getLastName(), consultant.getEmail(),
        consultant.isAbsent(), consultant.isLanguageFormal(), consultant.getAbsenceMessage(),
        consultant.isTeamConsultant(), agencyDTOs, authenticatedUser.getRoles(),
        authenticatedUser.getGrantedAuthorities(), null);
  }

  private AgencyDTO fetchAgencyViaAgencyService(ConsultantAgency consultantAgency) {
    try {
      return this.agencyServiceHelper.getAgency(consultantAgency.getAgencyId());
    } catch (AgencyServiceHelperException e) {
      LogService.logAgencyServiceHelperException(String
          .format("Error while getting agencies of consultant with id %s",
              consultantAgency.getId()), e);
    }
    return null;
  }

}

package de.caritas.cob.userservice.api.admin.service.consultant.create;

import de.caritas.cob.userservice.api.admin.service.ConsultantAgencyAdminResultDTOBuilder;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Creator class to generate new {@link ConsultantAgency } instances.
 */
@Service
@RequiredArgsConstructor
public class ConsultantAgencyCreatorService {

  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull AgencyServiceHelper agencyServiceHelper;
  private final @NonNull KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull SessionRepository sessionRepository;

  /**
   * Creates a new {@Link ConsultantAgency} based on the consultantId and {@Link
   * CreateConsultantAgencyDTO} input.
   *
   * @param consultantId              the consultant to use
   * @param createConsultantAgencyDTO the agencyId and role
   * @return the generated and persisted {@link ConsultantAgency} representation as {@link
   * ConsultantAgencyAdminResultDTO}
   */
  public ConsultantAgencyAdminResultDTO createNewConsultantAgency(String consultantId,
      CreateConsultantAgencyDTO createConsultantAgencyDTO) {
    Consultant consultant = this.getConsultant(consultantId);

    this.checkConsultantHasRole(consultantId, createConsultantAgencyDTO.getRole());

    AgencyDTO agency = getAgency(createConsultantAgencyDTO.getAgencyId());

    // TODO: Check Kreuzbund

    this.addConsultantToAgencyEnquiries(consultant, agency.getId());

    if (agency.getTeamAgency()) {
      this.addConsultantToTeamConsulting(consultant, agency.getId());

      if (!consultant.isTeamConsultant()) {
        consultant.setTeamConsultant(true);
        consultantRepository.save(consultant);
      }
    }

    ConsultantAgency consultantAgency = consultantAgencyService
        .saveConsultantAgency(buildConsultantAgency(consultant, agency.getId()));

    return ConsultantAgencyAdminResultDTOBuilder
        .getInstance()
        .withConsultantId(consultant.getId())
        .withResult(Stream.of(consultantAgency).collect(Collectors.toList()))
        .build();
  }

  private void addConsultantToTeamConsulting(Consultant consultant, Long id) {
  }

  private Consultant getConsultant(String consultantId) {
    Optional<Consultant> consultant = consultantRepository.findById(consultantId);
    if (!consultant.isPresent()) {
      throw new BadRequestException(
          String.format("Consultant with id %s does not exist", consultantId));
    }
    return consultant.get();
  }

  private void checkConsultantHasRole(String consultantId, String role) {
    if (!keycloakAdminClientHelper.userHasRole(consultantId, role)) {
      throw new BadRequestException(
          String.format("Consultant with id %s does not have the role %s", consultantId, role));
    }
  }

  private AgencyDTO getAgency(Long agencyId) {
    try {
      return this.agencyServiceHelper.getAgencyWithoutCaching(agencyId);
    } catch (AgencyServiceHelperException e) {
      throw new InternalServerErrorException(String.format(
          "AgencyService error while retrieving the agency for the ConsultantAgency-creating for agency %s",
          agencyId), e, LogService::logAgencyServiceHelperException);
    }
  }

  private ConsultantAgency buildConsultantAgency(Consultant consultant, Long agencyId) {
    return ConsultantAgency
        .builder()
        .consultant(consultant)
        .agencyId(agencyId)
        .createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now())
        .build();
  }

  private void addConsultantToAgencyEnquiries(Consultant consultant, Long agencyId) {
    List<Session> agencySessions = sessionRepository.findByAgencyId(agencyId);
    for (Session session : agencySessions) {

      try {
        rocketChatService.addTechnicalUserToGroup(session.getGroupId());

        rocketChatService.addUserToGroup(consultant.getRocketChatId(), session.getGroupId());

        if (session.getFeedbackGroupId() != null) {
          rocketChatService
              .addUserToGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
        }

        rocketChatService.removeTechnicalUserFromGroup(session.getGroupId());
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

}

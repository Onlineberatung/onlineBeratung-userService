package de.caritas.cob.userservice.api.admin.service.consultant.create;

import de.caritas.cob.userservice.api.admin.service.ConsultantAgencyAdminResultDTOBuilder;
import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
  private final @NonNull ConsultingTypeManager consultingTypeManager;

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
    CreateConsultantAgencyDTOInputAdapter adapter = new CreateConsultantAgencyDTOInputAdapter(
        consultantId, createConsultantAgencyDTO);
    return createNewConsultantAgency(adapter);
  }

  private ConsultantAgencyAdminResultDTO createNewConsultantAgency(
      ConsultantAgencyCreationInput input) {
    Consultant consultant = this.getConsultant(input.getConsultantId());

    this.checkConsultantHasRole(input);

    AgencyDTO agency = getAgency(input);

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
        .withResult(Collections.singletonList(consultantAgency))
        .build();
  }

  private Consultant getConsultant(String consultantId) {
    Optional<Consultant> consultant = consultantRepository.findById(consultantId);
    if (!consultant.isPresent()) {
      throw new BadRequestException(
          String.format("Consultant with id %s does not exist", consultantId));
    }
    return consultant.get();
  }

  private void checkConsultantHasRole(ConsultantAgencyCreationInput input) {
    if (!keycloakAdminClientHelper.userHasRole(input.getConsultantId(), input.getRole())) {
      throw new BadRequestException(
          String.format("Consultant with id %s does not have the role %s", input.getConsultantId(),
              input.getRole()));
    }
  }

  private AgencyDTO getAgency(ConsultantAgencyCreationInput input) {
    AgencyDTO agencyDto;
    try {
      agencyDto = this.agencyServiceHelper.getAgencyWithoutCaching(input.getAgencyId());
    } catch (AgencyServiceHelperException e) {
      throw new InternalServerErrorException(String.format(
          "AgencyService error while retrieving the agency for the ConsultantAgency-creating for agency %s",
          input.getAgencyId()), e, LogService::logAgencyServiceHelperException);
    }
    if (agencyDto == null) {
      throw new BadRequestException(
          String.format("AngecyId %s is not a valid agency", input.getAgencyId()));
    }
    return agencyDto;
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

  private void addConsultantToTeamConsulting(Consultant consultant, Long agencyId) {
    List<Session> agencySessions = sessionRepository
        .findByAgencyIdAndStatus(agencyId, SessionStatus.IN_PROGRESS);
    for (Session session : agencySessions) {
      try {
        rocketChatService.addTechnicalUserToGroup(session.getGroupId());

        if (canAddToTeamConsultingSession(session, consultant)) {
          rocketChatService.addUserToGroup(consultant.getRocketChatId(), session.getGroupId());
        }

        if (session.getFeedbackGroupId() != null && isMainConsultant(consultant)) {
          rocketChatService
              .addUserToGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
        }

        rocketChatService.removeTechnicalUserFromGroup(session.getGroupId());
      } catch (Exception e) {
        doRollbackAddConsultantToTeamConsulting(consultant, agencySessions, session);
        throw new InternalServerErrorException(
            getAddConsultantToAgencyEnquiriesLogMessage(session, consultant,
                "addConsultantToTeamConsulting"), e,
            LogService::logRocketChatError);
      }
    }
  }

  private void doRollbackAddConsultantToTeamConsulting(Consultant consultant,
      List<Session> agencySessions, Session lastSession) {
    for (Session session : agencySessions) {
      try {
        rocketChatService.addTechnicalUserToGroup(session.getGroupId());

        if (canAddToTeamConsultingSession(session, consultant)) {
          rocketChatService.removeUserFromGroup(consultant.getRocketChatId(), session.getGroupId());
        }

        if (session.getFeedbackGroupId() != null && isMainConsultant(consultant)) {
          rocketChatService
              .removeUserFromGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
        }

        rocketChatService.removeTechnicalUserFromGroup(session.getGroupId());
      } catch (Exception e) {
        throw new InternalServerErrorException(
            getdoRollbackAddConsultantToAgencyEnquiriesLogMessage(consultant, agencySessions,
                lastSession, "addConsultantToTeamConsulting"), e,
            LogService::logRocketChatError);
      }
    }
  }

  private boolean canAddToTeamConsultingSession(Session session, Consultant consultant) {
    ConsultingTypeSettings consultingTypeSettings = (ConsultingTypeSettings) consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType());

    return !consultingTypeSettings.getConsultingType().equals(ConsultingType.U25)
        || isMainConsultant(consultant);
  }

  private boolean isMainConsultant(Consultant consultant) {
    return keycloakAdminClientHelper
        .userHasAuthority(consultant.getId(), Authority.VIEW_ALL_FEEDBACK_SESSIONS)
        || keycloakAdminClientHelper
        .userHasRole(consultant.getId(), UserRole.U25_MAIN_CONSULTANT.name());
  }

  private void addConsultantToAgencyEnquiries(Consultant consultant, Long agencyId) {
    List<Session> agencySessions = sessionRepository
        .findByAgencyIdAndStatus(agencyId, SessionStatus.NEW);
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
        doRollbackAddConsultantToAgencyEnquiries(consultant, agencySessions,
            session);
        throw new InternalServerErrorException(
            getAddConsultantToAgencyEnquiriesLogMessage(session, consultant,
                "addConsultantToAgencyEnquiries"), e,
            LogService::logRocketChatError);
      }
    }
  }

  private void doRollbackAddConsultantToAgencyEnquiries(Consultant consultant,
      List<Session> agencySessions, Session lastSession) {
    for (Session session : agencySessions) {

      try {
        rocketChatService.addTechnicalUserToGroup(session.getGroupId());

        rocketChatService.removeUserFromGroup(consultant.getRocketChatId(), session.getGroupId());

        if (session.getFeedbackGroupId() != null) {
          rocketChatService
              .removeUserFromGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
        }

        rocketChatService.removeTechnicalUserFromGroup(session.getGroupId());

        if (session.getId().equals(lastSession.getId())) {
          return;
        }

      } catch (Exception e) {
        throw new InternalServerErrorException(
            getdoRollbackAddConsultantToAgencyEnquiriesLogMessage(consultant, agencySessions,
                lastSession, "addConsultantToAgencyEnquiries"), e,
            LogService::logRocketChatError);
      }
    }
  }

  private String getdoRollbackAddConsultantToAgencyEnquiriesLogMessage(Consultant consultant,
      List<Session> agencySessions, Session lastSession, String method) {
    StringBuilder sb = new StringBuilder(
        "Rollback error while roleback of ");
    sb.append(method);
    sb.append(" (consultantId:");
    sb.append(consultant.getId());
    sb.append(", sessions:");
    for (Session session : agencySessions) {
      sb.append("[sessionId:");
      sb.append(session.getId());
      sb.append(", groupId:");
      sb.append(session.getGroupId());
      if (session.getFeedbackGroupId() != null) {
        sb.append(", feedbackGroupId:");
        sb.append(session.getFeedbackGroupId());
      }
      sb.append("]");
      if (session.getId().equals(lastSession.getId())) {
        break;
      }
    }
    sb.append(")");
    return sb.toString();
  }

  private String getAddConsultantToAgencyEnquiriesLogMessage(Session session,
      Consultant consultant, String method) {
    return String.format(
        "RocketChatService error while setting up a Rocket.Chat room during consultantAgency-creation with Method %s for groupId (%s)%s and consultantId (%s)",
        method,
        session.getGroupId(),
        (session.getFeedbackGroupId() == null ? ""
            : String.format(" or feedbackGroupId (%s)", session.getFeedbackGroupId())),
        consultant.getId());
  }

}

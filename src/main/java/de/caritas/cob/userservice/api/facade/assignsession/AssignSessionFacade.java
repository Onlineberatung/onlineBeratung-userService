package de.caritas.cob.userservice.api.facade.assignsession;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.service.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps for accepting an enquiry and/or assigning a session to a
 * consultant.
 */
@Service
@RequiredArgsConstructor
public class AssignSessionFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull EmailNotificationFacade emailNotificationFacade;
  private final @NonNull SessionToConsultantVerifier sessionToConsultantVerifier;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UnauthorizedMembersProvider unauthorizedMembersProvider;
  private final @NonNull StatisticsService statisticsService;

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   *
   * <p>If the statistics function is enabled, the assignment of the session is processed as a
   * statistical event.
   */
  public void assignSession(Session session, Consultant consultant) {
    var consultantSessionDTO = ConsultantSessionDTO.builder()
        .consultant(consultant)
        .session(session)
        .build();
    sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO);

    updateSessionInDatabase(session, consultant);
    addNewConsultantToRocketChatGroup(session, consultant);
    removeUnauthorizedMembersFromGroups(session, consultant);
    sendEmailForConsultantChange(session, consultant);

    statisticsService.fireEvent(
        new AssignSessionStatisticsEvent(consultant.getId(), UserRole.CONSULTANT, session.getId()));
  }

  private void updateSessionInDatabase(Session session, Consultant consultant) {
    var initialStatus = session.getStatus();
    sessionService.updateConsultantAndStatusForSession(session, consultant,
        initialStatus == SessionStatus.NEW ? SessionStatus.IN_PROGRESS : initialStatus);
  }

  private void addNewConsultantToRocketChatGroup(Session session, Consultant consultant) {
    addConsultantToRocketChatGroup(session.getGroupId(), consultant);
    if (session.hasFeedbackChat()) {
      addConsultantToRocketChatGroup(session.getFeedbackGroupId(), consultant);
    }
  }

  private void addConsultantToRocketChatGroup(String rcGroupId, Consultant consultant) {
    rocketChatFacade.addUserToRocketChatGroup(consultant.getRocketChatId(), rcGroupId);
    rocketChatFacade.removeSystemMessagesFromRocketChatGroup(rcGroupId);
  }

  private void removeUnauthorizedMembersFromGroups(Session session, Consultant consultant) {
    var memberList = rocketChatFacade.retrieveRocketChatMembers(session.getGroupId());
    removeUnauthorizedMembersFromGroup(session, consultant, memberList);

    if (session.hasFeedbackChat()) {
      var feedbackMemberList = rocketChatFacade
          .retrieveRocketChatMembers(session.getFeedbackGroupId());
      removeUnauthorizedMembersFromFeedbackGroup(session, consultant, feedbackMemberList);
    }
  }

  private void removeUnauthorizedMembersFromGroup(Session session, Consultant consultant,
      List<GroupMemberDTO> memberList) {
    var consultantsToRemoveFromRocketChat =
        unauthorizedMembersProvider.obtainConsultantsToRemove(session.getGroupId(), session,
            consultant, memberList);

    RocketChatRemoveFromGroupOperationService
        .getInstance(this.rocketChatFacade, this.keycloakAdminClientService,
            this.consultingTypeManager)
        .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
        .removeFromGroupOrRollbackOnFailure();
  }

  private void removeUnauthorizedMembersFromFeedbackGroup(Session session,
      Consultant consultant, List<GroupMemberDTO> memberList) {
    var consultantsToRemoveFromRocketChat =
        unauthorizedMembersProvider.obtainConsultantsToRemove(session.getFeedbackGroupId(), session,
            consultant, memberList);

    RocketChatRemoveFromGroupOperationService
        .getInstance(this.rocketChatFacade, this.keycloakAdminClientService,
            this.consultingTypeManager)
        .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
        .removeFromFeedbackGroupOrRollbackOnFailure();
  }

  private void sendEmailForConsultantChange(Session session, Consultant consultant) {
    if (!authenticatedUser.getUserId().equals(consultant.getId())) {
      emailNotificationFacade.sendAssignEnquiryEmailNotification(consultant,
          authenticatedUser.getUserId(), session.getUser().getUsername());
    }
  }

}

package de.caritas.cob.userservice.api.facade.assignsession;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps for accepting an enquiry and/or assigning a session to a
 * consultant.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssignSessionFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull EmailNotificationFacade emailNotificationFacade;
  private final @NonNull SessionToConsultantVerifier sessionToConsultantVerifier;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UnauthorizedMembersProvider unauthorizedMembersProvider;
  private final @NonNull StatisticsService statisticsService;
  private final @NotNull RocketChatService rocketChatService;
  private final @NotNull CreateEnquiryMessageFacade createEnquiryMessageFacade;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull AssignEnquiryFacade assignEnquiryFacade;
  private final @NonNull HttpServletRequest httpServletRequest;

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   *
   * <p>If the statistics function is enabled, the assignment of the session is processed as a
   * statistical event.
   */
  public void assignSession(
      Session session, Consultant consultantToAssign, Consultant authConsultant) {
    var consultantSessionDTO =
        ConsultantSessionDTO.builder().consultant(consultantToAssign).session(session).build();
    sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO);

    updateSessionInDatabase(session, consultantToAssign);
    addConsultantToRocketChatGroup(session.getGroupId(), consultantToAssign);
    removeUnauthorizedMembersFromGroups(session, consultantToAssign, authConsultant);
    createNewFeedbackGroup(session, consultantToAssign);
    if (!authenticatedUser.isAdviceSeeker()) {
      sendEmailForConsultantChange(session, consultantToAssign);
    }

    var event =
        new AssignSessionStatisticsEvent(
            consultantToAssign.getId(), UserRole.CONSULTANT, session.getId());
    event.setRequestUri(httpServletRequest.getRequestURI());
    event.setRequestReferer(httpServletRequest.getHeader(HttpHeaders.REFERER));
    statisticsService.fireEvent(event);
  }

  private void createNewFeedbackGroup(Session session, Consultant consultantToAssign) {
    try {
      if (session.hasFeedbackChat()) {
        rocketChatService.deleteGroupAsSystemUser(session.getFeedbackGroupId());
        session.setFeedbackGroupId(null);
        sessionService.saveSession(session);
      }
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
          consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId());

      if (isTrue(extendedConsultingTypeResponseDTO.getInitializeFeedbackChat())) {
        List<ConsultantAgency> allAgencyConsultants =
            consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());
        String rcFeedbackGroupId =
            createEnquiryMessageFacade.createRcFeedbackGroup(
                session, session.getGroupId(), allAgencyConsultants);
        session.setFeedbackGroupId(rcFeedbackGroupId);
        sessionService.saveSession(session);
        assignEnquiryFacade.updateRocketChatRooms(rcFeedbackGroupId, session, consultantToAssign);
      }
    } catch (CreateEnquiryException exception) {
      log.error("CreateEnquiryMessageFacade error: ", exception);
      throw new InternalServerErrorException(exception.getMessage(), exception);
    }
  }

  private void updateSessionInDatabase(Session session, Consultant consultant) {
    var initialStatus = session.getStatus();
    sessionService.updateConsultantAndStatusForSession(
        session,
        consultant,
        initialStatus == SessionStatus.NEW ? SessionStatus.IN_PROGRESS : initialStatus);
  }

  private void addConsultantToRocketChatGroup(String rcGroupId, Consultant consultant) {
    rocketChatFacade.addUserToRocketChatGroup(consultant.getRocketChatId(), rcGroupId);
    rocketChatFacade.removeSystemMessagesFromRocketChatGroup(rcGroupId);
  }

  private void removeUnauthorizedMembersFromGroups(
      Session session, Consultant consultant, Consultant consultantToKeep) {
    var memberList = rocketChatFacade.retrieveRocketChatMembers(session.getGroupId());
    removeUnauthorizedMembersFromGroup(session, consultant, memberList, consultantToKeep);
  }

  private void removeUnauthorizedMembersFromGroup(
      Session session,
      Consultant consultant,
      List<GroupMemberDTO> memberList,
      Consultant consultantToKeep) {
    var consultantsToRemoveFromRocketChat =
        unauthorizedMembersProvider.obtainConsultantsToRemove(
            session.getGroupId(), session, consultant, memberList, consultantToKeep);

    RocketChatRemoveFromGroupOperationService.getInstance(
            this.rocketChatFacade, this.identityClient, this.consultingTypeManager)
        .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
        .removeFromGroupOrRollbackOnFailure();
  }

  private void removeUnauthorizedMembersFromFeedbackGroup(
      Session session,
      Consultant consultant,
      List<GroupMemberDTO> memberList,
      Consultant consultantToKeep) {
    var consultantsToRemoveFromRocketChat =
        unauthorizedMembersProvider.obtainConsultantsToRemove(
            session.getFeedbackGroupId(), session, consultant, memberList, consultantToKeep);

    RocketChatRemoveFromGroupOperationService.getInstance(
            this.rocketChatFacade, this.identityClient, this.consultingTypeManager)
        .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
        .removeFromFeedbackGroupOrRollbackOnFailure();
  }

  private void sendEmailForConsultantChange(Session session, Consultant consultant) {
    if (!authenticatedUser.getUserId().equals(consultant.getId())) {
      emailNotificationFacade.sendAssignEnquiryEmailNotification(
          consultant,
          authenticatedUser.getUserId(),
          session.getUser().getUsername(),
          TenantContext.getCurrentTenantData());
    }
  }
}

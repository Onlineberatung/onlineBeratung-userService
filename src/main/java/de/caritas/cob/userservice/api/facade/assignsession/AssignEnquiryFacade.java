package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.NEW;
import static java.util.Objects.nonNull;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.AssignSessionStatisticsEvent;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps for accepting an enquiry and/or assigning a enquiry to a
 * consultant.
 */
@Service
@RequiredArgsConstructor
public class AssignEnquiryFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull SessionToConsultantVerifier sessionToConsultantVerifier;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UnauthorizedMembersProvider unauthorizedMembersProvider;
  private final @NonNull StatisticsService statisticsService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   *
   * <p>If the statistics function is enabled, the assignment of the enquired is processed as
   * statistical event.
   *
   * @param session    the session to assign the consultant
   * @param consultant the consultant to assign
   */
  public void assignRegisteredEnquiry(Session session, Consultant consultant) {
    assignEnquiry(session, consultant);
    supplyAsync(updateRocketChatRooms(session, consultant, TenantContext.getCurrentTenant()));
    statisticsService.fireEvent(
        new AssignSessionStatisticsEvent(consultant.getId(), UserRole.CONSULTANT, session.getId()));
  }

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Add the given {@link
   * Consultant} to the Rocket.Chat group.
   *
   * @param session    the session to assign the consultant
   * @param consultant the consultant to assign
   */
  public void assignAnonymousEnquiry(Session session, Consultant consultant) {
    assignEnquiry(session, consultant);
    try {
      this.rocketChatFacade.addUserToRocketChatGroup(
          consultant.getRocketChatId(), session.getGroupId());
      this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup(session.getGroupId());
    } catch (Exception e) {
      rollbackSessionUpdate(session);
      throw new InternalServerErrorException(
          String.format(
              "Could not add consultant %s to group %s",
              consultant.getRocketChatId(), session.getGroupId()));
    }
  }

  private void assignEnquiry(Session session, Consultant consultant) {
    var consultantSessionDTO =
        ConsultantSessionDTO.builder().consultant(consultant).session(session).build();
    sessionToConsultantVerifier.verifySessionIsNotInProgress(consultantSessionDTO);
    sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO);

    sessionService.updateConsultantAndStatusForSession(session, consultant, IN_PROGRESS);
  }

  private Supplier<Object> updateRocketChatRooms(Session session, Consultant consultant, Long currentTenantId) {
    return () -> {
      tenantContextProvider.setCurrentTenantContextIfMissing(currentTenantId);
      updateRocketChatRooms(session.getGroupId(), session, consultant);
      if (session.hasFeedbackChat()) {
        updateRocketChatRooms(session.getFeedbackGroupId(), session, consultant);
      }
      return null;
    };
  }

  private void updateRocketChatRooms(String rcGroupId, Session session, Consultant consultant) {
    try {
      var memberList = this.rocketChatFacade.retrieveRocketChatMembers(rcGroupId);
      removeUnauthorizedMembers(rcGroupId, session, consultant, memberList);
      if (session.hasFeedbackChat()) {
        this.rocketChatFacade.addUserToRocketChatGroup(consultant.getRocketChatId(), rcGroupId);
      }
      this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup(rcGroupId);

    } catch (Exception e) {
      LogService.logRocketChatError(e);
      throw e;
    }
  }


  private void removeUnauthorizedMembers(String rcGroupId, Session session, Consultant consultant,
      List<GroupMemberDTO> memberList) {
    var consultantsToRemoveFromRocketChat = unauthorizedMembersProvider
        .obtainConsultantsToRemove(rcGroupId, session, consultant, memberList);

    var rocketChatRemoveFromGroupOperationService = RocketChatRemoveFromGroupOperationService
        .getInstance(this.rocketChatFacade, this.identityClient, this.consultingTypeManager)
        .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat));

    if (rcGroupId.equalsIgnoreCase(session.getGroupId())) {
      rocketChatRemoveFromGroupOperationService.removeFromGroup();
    }
    if (rcGroupId.equalsIgnoreCase(session.getFeedbackGroupId())) {
      rocketChatRemoveFromGroupOperationService.removeFromFeedbackGroup();
    }
  }

  private void rollbackSessionUpdate(Session session) {
    if (nonNull(session)) {
      sessionService.updateConsultantAndStatusForSession(session, null, NEW);
    }
  }
}

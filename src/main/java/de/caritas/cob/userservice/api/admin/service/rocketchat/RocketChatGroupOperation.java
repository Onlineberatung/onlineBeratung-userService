package de.caritas.cob.userservice.api.admin.service.rocketchat;

import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class RocketChatGroupOperation {

  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull IdentityClient identityClient;

  protected Consumer<String> logMethod = LogService::logInfo;

  void addConsultantToGroupOfSession(
      Session session, Consultant consultant, ConsultingTypeManager consultingTypeManager) {
    var operationConditionProvider =
        new RocketChatOperationConditionProvider(
            this.identityClient, session, consultant, consultingTypeManager);

    if (operationConditionProvider.canAddToRocketChatGroup()) {
      rocketChatFacade.addUserToRocketChatGroup(consultant.getRocketChatId(), session.getGroupId());
      logMethod.accept(
          String.format(
              "Consultant added to rc group %s (%s).",
              session.getGroupId(), resolveTypeOfSession(session)));
    }

    if (operationConditionProvider.canAddToRocketChatFeedbackGroup()) {
      rocketChatFacade.addUserToRocketChatGroup(
          consultant.getRocketChatId(), session.getFeedbackGroupId());
      logMethod.accept(
          String.format(
              "Consultant added to rc feedback group %s (%s).",
              session.getFeedbackGroupId(), resolveTypeOfSession(session)));
    }
  }

  String resolveTypeOfSession(Session session) {
    if (SessionStatus.NEW.equals(session.getStatus())) {
      return "enquiry";
    }
    return session.isTeamSession() ? "team-session" : "standard-session";
  }

  void removeConsultantsFromSessionGroups(Session session, List<Consultant> consultants) {
    removeConsultantsFromRocketChatGroup(
        session.getGroupId(), consultants, rocketChatFacade::removeUserFromGroup);
    removeConsultantsFromRocketChatGroup(
        session.getFeedbackGroupId(), consultants, rocketChatFacade::removeUserFromGroup);
  }

  void removeConsultantsFromSessionGroup(String rcGroupId, List<Consultant> consultants) {
    removeConsultantsFromRocketChatGroup(
        rcGroupId, consultants, rocketChatFacade::removeUserFromGroup);
  }

  void removeConsultantsFromSessionGroupAndIgnoreGroupNotFound(
      String rcGroupId, List<Consultant> consultants) {
    removeConsultantsFromRocketChatGroup(
        rcGroupId, consultants, rocketChatFacade::removeUserFromGroupIgnoreGroupNotFound);
  }

  private void removeConsultantsFromRocketChatGroup(
      String rcGroupId,
      List<Consultant> consultants,
      BiConsumer<String, String> removeFromRocketchatGroupMethod) {
    if (rcGroupId == null) {
      return;
    }
    List<String> groupMemberList = obtainRocketChatGroupMemberIds(rcGroupId);

    rocketChatFacade.addTechnicalUserToGroup(rcGroupId);
    consultants.stream()
        .map(Consultant::getRocketChatId)
        .filter(groupMemberList::contains)
        .forEach(rcUserId -> removeFromRocketchatGroupMethod.accept(rcUserId, rcGroupId));
    rocketChatFacade.leaveFromGroupAsTechnicalUser(rcGroupId);
  }

  private List<String> obtainRocketChatGroupMemberIds(String groupId) {
    return this.rocketChatFacade.retrieveRocketChatMembers(groupId).stream()
        .map(GroupMemberDTO::get_id)
        .collect(Collectors.toList());
  }
}

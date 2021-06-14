package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.authorization.Authority.AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS;
import static de.caritas.cob.userservice.api.authorization.Authority.AuthorityValue.VIEW_ALL_PEER_SESSIONS;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provides consultants of Rocket.Chat group that don't have the authorization for it.
 */
@Service
@RequiredArgsConstructor
public class UnauthorizedMembersProvider {

  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  private final @NonNull ConsultantService consultantService;
  private final @NonNull RocketChatCredentialsProvider rocketChatCredentialsProvider;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;

  public List<Consultant> obtainConsultantsToRemove(String rcGroupId, Session session,
      Consultant consultant, List<GroupMemberDTO> memberList) {
    List<String> authorizedMembers = obtainAuthorizedMembers(rcGroupId, session, consultant);
    return memberList.stream()
        .map(GroupMemberDTO::get_id)
        .filter(memberRcId -> !authorizedMembers.contains(memberRcId))
        .map(consultantService::getConsultantByRcUserId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private List<String> obtainAuthorizedMembers(String rcGroupId, Session session,
      Consultant consultant) {
    List<String> authorizedMembers = new ArrayList<>();
    addConsultantAndAskerOfSession(session, consultant, authorizedMembers);
    addTechnicalUsers(authorizedMembers);
    addTeamConsultantsIfNecessary(rcGroupId, session, authorizedMembers);

    return authorizedMembers;
  }

  private void addConsultantAndAskerOfSession(Session session, Consultant consultant,
      List<String> authorizedMembers) {
    authorizedMembers.add(session.getUser().getRcUserId());
    authorizedMembers.add(consultant.getRocketChatId());
  }

  private void addTechnicalUsers(List<String> authorizedMembers) {
    try {
      authorizedMembers.add(rocketChatCredentialsProvider.getTechnicalUser().getRocketChatUserId());
    } catch (RocketChatUserNotInitializedException e) {
      throw new InternalServerErrorException("Rocket.Chat technical user not initialized.");
    }
    authorizedMembers.add(rocketChatSystemUserId);
  }

  private void addTeamConsultantsIfNecessary(String rcGroupId, Session session,
      List<String> authorizedMembers) {
    addTeamConsultantsIfTeamSession(session, authorizedMembers);
    addMainConsultantsIfFeedbackTeamSession(rcGroupId, session, authorizedMembers);
  }

  private void addTeamConsultantsIfTeamSession(Session session, List<String> authorizedMembers) {
    if (session.isTeamSession() && !session.hasFeedbackChat()) {
      consultantService.findConsultantsByAgencyId(session.getAgencyId()).stream()
          .filter(Consultant::isTeamConsultant)
          .map(Consultant::getRocketChatId)
          .filter(rocketChatId -> !rocketChatId.equalsIgnoreCase(
              session.getConsultant().getRocketChatId()))
          .forEach(authorizedMembers::add);
    }
  }

  private void addMainConsultantsIfFeedbackTeamSession(String rcGroupId, Session session,
      List<String> authorizedMembers) {
    if (session.isTeamSession() && session.hasFeedbackChat()) {
      consultantService.findConsultantsByAgencyId(session.getAgencyId()).stream()
          .filter(consultant -> !isPeerConsultantOfGroup(rcGroupId, session, consultant))
          .filter(consultant -> !isPeerConsultantOfFeedbackGroup(rcGroupId, session, consultant))
          .map(Consultant::getRocketChatId)
          .forEach(authorizedMembers::add);
    }
  }

  private boolean isPeerConsultantOfGroup(String rcGroupId, Session session, Consultant consultant) {
    return rcGroupId.equalsIgnoreCase(session.getGroupId())
        && !keycloakAdminClientService.userHasAuthority(consultant.getId(), VIEW_ALL_PEER_SESSIONS);
  }

  private boolean isPeerConsultantOfFeedbackGroup(String rcGroupId, Session session,
      Consultant consultant) {
    return rcGroupId.equalsIgnoreCase(session.getFeedbackGroupId())
        && !keycloakAdminClientService.userHasAuthority(consultant.getId(),
        VIEW_ALL_FEEDBACK_SESSIONS);
  }
}

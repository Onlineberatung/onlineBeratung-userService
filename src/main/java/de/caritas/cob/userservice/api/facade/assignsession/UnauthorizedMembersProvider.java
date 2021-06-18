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
import java.util.function.Predicate;
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

  /**
   * Obtains a list of {@link Consultant}s which are not authorized to view the given Rocket.Chat
   * group and therefore should be removed.
   *
   * @param rcGroupId  the Rocket.Chat group ID
   * @param session    {@link Session}
   * @param consultant {@link Consultant}
   * @param memberList list of {@link GroupMemberDTO} containing the current members of the group
   * @return list of {@link Consultant}s to be removed
   */
  public List<Consultant> obtainConsultantsToRemove(String rcGroupId, Session session,
      Consultant consultant, List<GroupMemberDTO> memberList) {
    var authorizedMembers = obtainAuthorizedMembers(rcGroupId, session, consultant);
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
    List<Consultant> consultantsOfAgency =
        consultantService.findConsultantsByAgencyId(session.getAgencyId());
    addTeamConsultantsIfTeamSession(session, authorizedMembers, consultantsOfAgency);
    addMainConsultantsIfFeedbackTeamSession(rcGroupId, session, authorizedMembers,
        consultantsOfAgency);
  }

  private void addTeamConsultantsIfTeamSession(Session session, List<String> authorizedMembers,
      List<Consultant> consultantsOfAgency) {
    if (session.isTeamSession() && !session.hasFeedbackChat()) {
      consultantsOfAgency.stream()
          .filter(Consultant::isTeamConsultant)
          .map(Consultant::getRocketChatId)
          .filter(rocketChatId -> !rocketChatId.equalsIgnoreCase(
              session.getConsultant().getRocketChatId()))
          .forEach(authorizedMembers::add);
    }
  }

  private void addMainConsultantsIfFeedbackTeamSession(String rcGroupId, Session session,
      List<String> authorizedMembers, List<Consultant> consultantsOfAgency) {
    if (isTeamSessionWithFeedbackChat(session)) {
      if (rcGroupId.equalsIgnoreCase(session.getGroupId())) {
        obtainMainConsultantsOfGroup(authorizedMembers, consultantsOfAgency,
            this::hasAuthorityToViewPeerGroups);
      }
      if (rcGroupId.equalsIgnoreCase(session.getFeedbackGroupId())) {
        obtainMainConsultantsOfGroup(authorizedMembers, consultantsOfAgency,
            this::hasAuthorityToViewFeedbackGroups);
      }
    }
  }

  private boolean isTeamSessionWithFeedbackChat(Session session) {
    return session.isTeamSession() && session.hasFeedbackChat();
  }

  private void obtainMainConsultantsOfGroup(List<String> authorizedMembers,
      List<Consultant> consultantsOfAgency, Predicate<Consultant> authorityMethod) {
    consultantsOfAgency.stream()
        .filter(authorityMethod)
        .map(Consultant::getRocketChatId)
        .forEach(authorizedMembers::add);
  }

  private boolean hasAuthorityToViewPeerGroups(Consultant consultant) {
    return keycloakAdminClientService.userHasAuthority(consultant.getId(), VIEW_ALL_PEER_SESSIONS);
  }

  private boolean hasAuthorityToViewFeedbackGroups(Consultant consultant) {
    return keycloakAdminClientService.userHasAuthority(consultant.getId(),
        VIEW_ALL_FEEDBACK_SESSIONS);
  }
}

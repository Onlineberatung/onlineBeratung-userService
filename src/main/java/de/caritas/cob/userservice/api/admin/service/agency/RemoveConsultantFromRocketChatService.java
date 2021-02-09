package de.caritas.cob.userservice.api.admin.service.agency;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service to provide logic to remove a consultant who was a team consultant from Rocket.Chat
 * rooms.
 */
@Service
@RequiredArgsConstructor
public class RemoveConsultantFromRocketChatService {

  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;

  /**
   * Removes the consultant who is not directly assigned to session from Rocket.Chat rooms.
   *
   * @param sessions the sessions where consultant should be removed in Rocket.Chat
   */
  public void removeConsultantFromSessions(List<Session> sessions) {
    Map<Session, List<Consultant>> consultantsFromSession = sessions.stream()
        .collect(Collectors.toMap(session -> session, this::observeConsultantsToRemove));

    RocketChatRemoveFromGroupOperationService
        .getInstance(this.rocketChatService, this.keycloakAdminClientService)
        .onSessionConsultants(consultantsFromSession)
        .removeFromGroupsOrRollbackOnFailure();
  }

  private List<Consultant> observeConsultantsToRemove(Session session) {
    try {
      return observeFromSession(session);
    } catch (RocketChatGetGroupMembersException | RocketChatUserNotInitializedException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
  }

  private List<Consultant> observeFromSession(Session session)
      throws RocketChatGetGroupMembersException, RocketChatUserNotInitializedException {
    return this.rocketChatService.getStandardMembersOfGroup(session.getGroupId())
        .stream()
        .filter(notUserAndNotDirectlyAssignedConsultant(session))
        .map(GroupMemberDTO::get_id)
        .map(this.consultantRepository::findByRocketChatId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Predicate<GroupMemberDTO> notUserAndNotDirectlyAssignedConsultant(Session session) {
    return member ->
        !member.get_id().equals(session.getConsultant().getRocketChatId())
            && !member.get_id().equals(session.getUser().getRcUserId());
  }

}

package de.caritas.cob.userservice.api.admin.service.agency;

import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service to provide logic to remove a consultant who was a team consultant from Rocket.Chat rooms.
 */
@Service
@RequiredArgsConstructor
public class RemoveConsultantFromRocketChatService {

  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Removes the consultant who is not directly assigned to session from Rocket.Chat rooms.
   *
   * @param sessions the sessions where consultant should be removed in Rocket.Chat
   */
  public void removeConsultantFromSessions(List<Session> sessions) {
    Map<Session, List<Consultant>> consultantsFromSession =
        sessions.stream()
            .collect(Collectors.toMap(session -> session, this::observeConsultantsToRemove));

    RocketChatRemoveFromGroupOperationService.getInstance(
            this.rocketChatFacade, this.identityClient, this.consultingTypeManager)
        .onSessionConsultants(consultantsFromSession)
        .removeFromGroupsOrRollbackOnFailure();
  }

  private List<Consultant> observeConsultantsToRemove(Session session) {
    return this.rocketChatFacade.getStandardMembersOfGroup(session.getGroupId()).stream()
        .filter(notUserAndNotDirectlyAssignedConsultant(session))
        .map(GroupMemberDTO::get_id)
        .map(this.consultantRepository::findByRocketChatIdAndDeleteDateIsNull)
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

package de.caritas.cob.userservice.api.deleteworkflow.service;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ALL;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.deleteworkflow.action.asker.DeleteSingleRoomAndSessionAction;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.deleteworkflow.model.SessionDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupsListAllException;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to trigger deletion of inactive sessions and user accounts.
 */
@Service
@RequiredArgsConstructor
public class DeleteInactiveSessionsAndUserService {

  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull UserRepository userRepository;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull DeleteUserAccountService deleteUserAccountService;
  private final @NonNull WorkflowErrorMailService workflowErrorMailService;
  private final @NonNull ActionsRegistry actionsRegistry;

  @Value("${inactive.session.and.user.deleteWorkflow.days}")
  private int inactiveSessionAndUserDeleteWorkflowDays;

  /**
   * Deletes all inactive sessions and even the user accounts, if there are no more active
   * sessions.
   */
  public void deleteInactiveSessionsAndUsers() {
    Map<String, List<String>> userInactiveGroupMap = new HashMap<>();

    fetchAllInactivePrivateGroups()
        .forEach(group -> userInactiveGroupMap
            .computeIfAbsent(group.getUser().getId(), v -> new ArrayList<>())
            .add(group.getId()));

    List<DeletionWorkflowError> workflowErrors = userInactiveGroupMap
        .entrySet()
        .stream()
        .map(this::performDeletionWorkflowForGroup)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    sendWorkflowErrorsMail(workflowErrors);
  }

  private List<GroupDTO> fetchAllInactivePrivateGroups() {
    LocalDateTime dateTimeToCheck = calculateDateTimeToCheck();
    return fetchAllInactivePrivateGroupsSinceGivenDate(dateTimeToCheck);
  }

  private LocalDateTime calculateDateTimeToCheck() {
    return LocalDateTime
        .now()
        .with(LocalTime.MIDNIGHT)
        .minusDays(inactiveSessionAndUserDeleteWorkflowDays);
  }

  private List<GroupDTO> fetchAllInactivePrivateGroupsSinceGivenDate(
      LocalDateTime dateTimeToCheck) {
    try {
      return rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(dateTimeToCheck);
    } catch (RocketChatGetGroupsListAllException ex) {
      var deletionWorkflowError = DeletionWorkflowError.builder()
          .deletionSourceType(ASKER)
          .deletionTargetType(ALL)
          .identifier("n/a")
          .reason("Unable to delete inactive users and sessions")
          .timestamp(nowInUtc())
          .build();
      sendWorkflowErrorsMail(Collections.singletonList(deletionWorkflowError));
    }
    return Collections.emptyList();
  }

  private List<DeletionWorkflowError> performDeletionWorkflowForGroup(
      Entry<String, List<String>> userInactiveGroupEntry) {

    Optional<User> user = userRepository
        .findByRcUserIdAndDeleteDateIsNull(userInactiveGroupEntry.getKey());
    if (user.isPresent()) {
      List<Session> userSessionList = sessionRepository.findByUser(user.get());
      if (userInactiveGroupEntry.getValue().size() == userSessionList.size()) {
        return deleteUserAccountService.performUserDeletion(user.get());
      } else {
        return userInactiveGroupEntry.getValue()
            .stream()
            .map(rcGroupId -> performSessionDeletion(rcGroupId, userSessionList))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  private List<DeletionWorkflowError> performSessionDeletion(String rcGroupId,
      List<Session> userSessionList) {

    Optional<Session> session = userSessionList
        .stream()
        .filter(s -> s.getGroupId().equals(rcGroupId))
        .findFirst();

    if (session.isPresent()) {

      var deletionWorkflowDTO = new SessionDeletionWorkflowDTO(session.get(), new ArrayList<>());

      this.actionsRegistry.buildContainerForType(SessionDeletionWorkflowDTO.class)
          .addActionToExecute(DeleteSingleRoomAndSessionAction.class)
          .executeActions(deletionWorkflowDTO);

      return deletionWorkflowDTO.getDeletionWorkflowErrors();

    }

    var deletionWorkflowError = DeletionWorkflowError.builder()
        .deletionSourceType(ASKER)
        .deletionTargetType(ALL)
        .identifier(rcGroupId)
        .reason("Session with rc group id could not be found.")
        .timestamp(nowInUtc())
        .build();
    return Collections.singletonList(deletionWorkflowError);
  }

  private void sendWorkflowErrorsMail(List<DeletionWorkflowError> workflowErrors) {
    if (isNotEmpty(workflowErrors)) {
      this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }
  }

}

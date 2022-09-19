package de.caritas.cob.userservice.api.workflow.delete.service.provider;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDTO;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupsListAllException;
import de.caritas.cob.userservice.api.helper.CustomLocalDateTime;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.service.LogService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Provider for user to inactive Rocket.Chat group map. */
@Service
@RequiredArgsConstructor
public class InactivePrivateGroupsProvider {

  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ChatRepository chatRepository;

  @Value("${session.inactive.deleteWorkflow.check.days}")
  private int sessionInactiveDeleteWorkflowCheckDays;

  /**
   * Get a map with users and their related inactive Rocket.Chat group ids. Group chats are
   * excluded.
   *
   * @return a map with users and related inactive Rocket.Chat groups ids
   */
  public Map<String, List<String>> retrieveUserWithInactiveGroupsMap() {

    Set<String> groupChatIdSet = buildSetOfGroupChatGroupdIds();

    Map<String, List<String>> userWithInactiveGroupsMap = new HashMap<>();
    fetchAllInactivePrivateGroups().stream()
        .filter(group -> !groupChatIdSet.contains(group.getId()))
        .forEach(
            group ->
                userWithInactiveGroupsMap
                    .computeIfAbsent(group.getUser().getId(), v -> new ArrayList<>())
                    .add(group.getId()));
    return userWithInactiveGroupsMap;
  }

  private Set<String> buildSetOfGroupChatGroupdIds() {
    List<Chat> chatList = IterableUtils.toList(chatRepository.findAll());
    return chatList.stream().map(Chat::getGroupId).collect(Collectors.toSet());
  }

  private List<GroupDTO> fetchAllInactivePrivateGroups() {
    LocalDateTime dateTimeToCheck =
        CustomLocalDateTime.nowInUtc()
            .with(LocalTime.MIDNIGHT)
            .minusDays(sessionInactiveDeleteWorkflowCheckDays);
    try {
      return rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(dateTimeToCheck);
    } catch (RocketChatGetGroupsListAllException ex) {
      LogService.logRocketChatError(ex);
    }
    return Collections.emptyList();
  }
}

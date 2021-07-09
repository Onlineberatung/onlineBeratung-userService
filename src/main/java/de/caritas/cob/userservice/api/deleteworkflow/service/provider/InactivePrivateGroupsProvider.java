package de.caritas.cob.userservice.api.deleteworkflow.service.provider;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupsListAllException;
import de.caritas.cob.userservice.api.helper.DateCalculator;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDTO;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provider for user to inactive Rocket.Chat group map.
 */
@Service
@RequiredArgsConstructor
public class InactivePrivateGroupsProvider {

  private final @NonNull RocketChatService rocketChatService;

  @Value("${session.inactive.deleteWorkflow.check.days}")
  private int sessionInactiveDeleteWorkflowCheckDays;

  /**
   *  Get a map with users and their related inactive Rocket.Chat group ids.
   *
   * @return a map with users and related inactive Rocket.Chat groups ids
   */
  public Map<String, List<String>> retrieveUserWithInactiveGroupsMap() {

    Map<String, List<String>> userWithInactiveGroupsMap = new HashMap<>();
    fetchAllInactivePrivateGroups()
        .forEach(group -> userWithInactiveGroupsMap
            .computeIfAbsent(group.getUser().getId(), v -> new ArrayList<>())
            .add(group.getId()));
    return userWithInactiveGroupsMap;
  }

  private List<GroupDTO> fetchAllInactivePrivateGroups() {
    LocalDateTime dateTimeToCheck = DateCalculator
        .calculateDateInThePastAtMidnight(sessionInactiveDeleteWorkflowCheckDays);
    return fetchAllInactivePrivateRocketChatGroupsSinceGivenDate(dateTimeToCheck);
  }

  private List<GroupDTO> fetchAllInactivePrivateRocketChatGroupsSinceGivenDate(
      LocalDateTime dateTimeToCheck) {
    try {
      return rocketChatService.fetchAllInactivePrivateGroupsSinceGivenDate(dateTimeToCheck);
    } catch (RocketChatGetGroupsListAllException ex) {
      LogService.logRocketChatError(ex);
    }
    return Collections.emptyList();
  }


}

package de.caritas.cob.userservice.api.service.liveevents;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Provider to observe all assigned chat user ids instead of initiator. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RelevantUserAccountIdsByChatProvider implements UserIdsProvider {

  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull UserService userService;
  private final @NonNull ConsultantService consultantService;

  /**
   * Collects all relevant user ids of a chat.
   *
   * @param rcGroupId the rocket chat group id used to lookup chat members
   * @return a {@link List} containing all user ids to be notified
   */
  @Override
  public List<String> collectUserIds(String rcGroupId) {
    return this.rocketChatService.getChatUsers(rcGroupId).parallelStream()
        .map(GroupMemberDTO::get_id)
        .map(this::toUserAccountId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private String toUserAccountId(String rcUserId) {
    return this.userService
        .findUserByRcUserId(rcUserId)
        .map(User::getUserId)
        .orElse(
            this.consultantService
                .getConsultantByRcUserId(rcUserId)
                .map(Consultant::getId)
                .orElse(null));
  }
}

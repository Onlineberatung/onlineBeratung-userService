package de.caritas.cob.userservice.api.service.liveevents;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.UserService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provider to observe all assigned chat user ids instead of initiator.
 */
@Component
public class RelevantUserAccountIdsByChatProvider extends UserIdsProvider {

  private final RocketChatService rocketChatService;
  private final UserService userService;
  private final ConsultantService consultantService;

  @Autowired
  public RelevantUserAccountIdsByChatProvider(AuthenticatedUser authenticatedUser,
      RocketChatService rocketChatService, UserService userService,
      ConsultantService consultantService) {
    super(authenticatedUser);
    this.rocketChatService = requireNonNull(rocketChatService);
    this.userService = requireNonNull(userService);
    this.consultantService = requireNonNull(consultantService);
  }

  /**
   * Collects all relevant user ids of a chat.
   *
   * @param rcGroupId the rocket chat group id used to lookup chat members
   * @return a {@link List} containing all user ids to be notified
   */
  @Override
  List<String> collectUserIds(String rcGroupId) {
    try {
      return extractDependentUserIds(rcGroupId);
    } catch (RocketChatGetGroupMembersException e) {
      LogService.logRocketChatError(
          String.format("Unable to collect rc members for group id %s", rcGroupId));
    }
    return emptyList();
  }

  private List<String> extractDependentUserIds(String rcGroupId)
      throws RocketChatGetGroupMembersException {
    return this.rocketChatService.getMembersOfGroup(rcGroupId).parallelStream()
        .map(GroupMemberDTO::get_id)
        .map(this::toUserAccountId)
        .filter(Objects::nonNull)
        .filter(this::notInitiatingUser)
        .collect(Collectors.toList());
  }

  private String toUserAccountId(String rcUserId) {
    return this.userService.findUserByRcUserId(rcUserId).map(User::getUserId)
        .orElse(this.consultantService.getConsultantByRcUserId(rcUserId).map(Consultant::getId)
            .orElse(null));
  }

}

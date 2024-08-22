package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.helper.Helper.ONE_DAY_IN_HOURS;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLeaveFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade to encapsulate rocket chat logic with exception handling. */
@Service
@RequiredArgsConstructor
public class RocketChatFacade {

  private final @NonNull RocketChatService rocketChatService;

  /**
   * Adds the provided user to the Rocket.Chat group with given groupId.
   *
   * @param rcUserId Rocket.Chat userId
   * @param groupId Rocket.Chat roomId
   */
  public void addUserToRocketChatGroup(String rcUserId, String groupId) {
    try {
      addTechnicalUserToGroup(groupId);
      rocketChatService.addUserToGroup(rcUserId, groupId);
      leaveFromGroupAsTechnicalUser(groupId);
    } catch (RocketChatAddUserToGroupException addUserEx) {
      var message =
          String.format(
              "Could not add user with id %s to Rocket.Chat group with id %s. Initiate rollback.",
              rcUserId, groupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  /**
   * Removes all messages from the specified Rocket.Chat group written by the technical user from
   * the last 24 hours (avoiding time zone failures).
   *
   * @param groupId the rocket chat group id
   */
  public void removeSystemMessagesFromRocketChatGroup(String groupId) {
    try {
      addTechnicalUserToGroup(groupId);
      rocketChatService.removeSystemMessages(
          groupId, nowInUtc().minusHours(ONE_DAY_IN_HOURS), nowInUtc());
      leaveFromGroupAsTechnicalUser(groupId);
    } catch (RocketChatRemoveSystemMessagesException | RocketChatUserNotInitializedException e) {
      var message =
          String.format("Could not remove system messages from Rocket.Chat group id %s", groupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  /**
   * Returns the group/room members of the given Rocket.Chat group id.
   *
   * @param rcGroupId the rocket chat id
   * @return al members of the group
   */
  public List<GroupMemberDTO> retrieveRocketChatMembers(String rcGroupId) {
    if (isBlank(rcGroupId)) {
      return emptyList();
    }
    try {
      addTechnicalUserToGroup(rcGroupId);
      List<GroupMemberDTO> memberList = rocketChatService.getChatUsers(rcGroupId);
      leaveFromGroupAsTechnicalUser(rcGroupId);
      return memberList;
    } catch (Exception exception) {
      var message =
          String.format(
              "Could not get Rocket.Chat group members of group id %s. Initiate rollback.",
              rcGroupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  /**
   * Adds the technical user to the given Rocket.Chat group id.
   *
   * @param groupId the rocket chat group id
   */
  public void addTechnicalUserToGroup(String groupId) {
    try {
      rocketChatService.addTechnicalUserToGroup(groupId);
    } catch (RocketChatAddUserToGroupException | RocketChatUserNotInitializedException addUserEx) {
      var message =
          String.format(
              "Could not add Rocket.Chat technical user to Rocket.Chat group with id %s. Initiate rollback.",
              groupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  /**
   * Leaves group from the given Rocket.Chat group id as the technical user.
   *
   * @param groupId the rocket chat group id
   */
  public void leaveFromGroupAsTechnicalUser(String groupId) {
    try {
      rocketChatService.leaveFromGroupAsTechnicalUser(groupId);
    } catch (RocketChatLeaveFromGroupException e) {
      var message =
          String.format("Could not leave from Rocket.Chat group id %s as technical user", groupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  /**
   * Removes the provided user from the Rocket.Chat group with given groupId.
   *
   * @param rcUserId Rocket.Chat userId
   * @param groupId Rocket.Chat roomId
   */
  public void removeUserFromGroup(String rcUserId, String groupId) {
    try {
      this.rocketChatService.removeUserFromGroup(rcUserId, groupId);
    } catch (RocketChatRemoveUserFromGroupException e) {
      var message =
          String.format(
              "Could not remove user with id %s from Rocket.Chat group id %s", rcUserId, groupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  public void removeUserFromGroupIgnoreGroupNotFound(String rcUserId, String groupId) {
    try {
      this.rocketChatService.removeUserFromGroupIgnoreGroupNotFound(rcUserId, groupId);
    } catch (RocketChatRemoveUserFromGroupException e) {
      var message =
          String.format(
              "Could not remove user with id %s from Rocket.Chat group id %s", rcUserId, groupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  /**
   * Get all standard members (all users except system user and technical user) of a rocket chat
   * group.
   *
   * @param groupId the rocket chat group id
   * @return all standard members of that group
   */
  public List<GroupMemberDTO> getStandardMembersOfGroup(String groupId) {
    try {
      return this.rocketChatService.getStandardMembersOfGroup(groupId);
    } catch (RocketChatGetGroupMembersException | RocketChatUserNotInitializedException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
  }
}

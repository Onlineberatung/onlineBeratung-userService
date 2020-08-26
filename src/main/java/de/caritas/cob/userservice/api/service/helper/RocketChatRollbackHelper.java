package de.caritas.cob.userservice.api.service.helper;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RocketChatRollbackHelper {

  private final RocketChatService rocketChatService;
  private final RocketChatCredentialsHelper rcCredentialsHelper;

  @Autowired
  public RocketChatRollbackHelper(RocketChatService rocketChatService,
      RocketChatCredentialsHelper rcCredentialsHelper) {
    this.rocketChatService = rocketChatService;
    this.rcCredentialsHelper = rcCredentialsHelper;
  }

  /**
   * Roll back for acceptEnquiry method. Adds already removed users back to the Rocket.Chat group.
   *
   * @param memberList
   */
  public void rollbackRemoveUsersFromRocketChatGroup(String groupId,
      List<GroupMemberDTO> memberList) {

    if (memberList != null && groupId != null) {
      try {
        List<GroupMemberDTO> currentList = rocketChatService.getMembersOfGroup(groupId);

        // Add Rocket.Chat technical user, if not in current member list
        if (!listContainsTechUser(currentList)) {
          try {
            rocketChatService.addTechnicalUserToGroup(groupId);
          } catch (RocketChatAddUserToGroupException e) {
            LogService.logInternalServerError(String.format(
                "Could not add techical user from Rocket.Chat group id %s during roll back.",
                groupId));
            return;
          }
        }

        if (currentList.size() != memberList.size()) {
          for (GroupMemberDTO member : currentList) {
            if (!memberList.contains(member)) {
              rocketChatService.addUserToGroup(member.get_id(), groupId);
            }
          }
        }

        // Remove technical user from Rocket.Chat group
        try {
          rocketChatService.removeTechnicalUserFromGroup(groupId);
        } catch (RocketChatRemoveUserFromGroupException e) {
          LogService.logInternalServerError(String.format(
              "Could not remove techical user from Rocket.Chat group id %s during roll back.",
              groupId));
        }

      } catch (Exception ex) {
        LogService.logInternalServerError(String.format(
            "Error during rollback while adding back the users to the Rocket.Chat group with id %s",
            groupId), ex);
      }
    }
  }

  /**
   * Returns true if provided list contains the Rocket.Chat technical user.
   *
   * @param memberList
   * @return
   */
  private boolean listContainsTechUser(List<GroupMemberDTO> memberList) {
    for (GroupMemberDTO member : memberList) {
      try {
        if (member.get_id().equals(rcCredentialsHelper.getTechnicalUser().getRocketChatUserId())) {
          return true;
        }
      } catch (RocketChatUserNotInitializedException e) {
        return false;
      }
    }

    return false;
  }
}

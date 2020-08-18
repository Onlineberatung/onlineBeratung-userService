package de.caritas.cob.userservice.api.service.helper;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;

@Service
public class RocketChatRollbackHelper {

  private final RocketChatService rocketChatService;
  private final RocketChatCredentialsHelper rcCredentialsHelper;
  private final LogService logService;

  @Autowired
  public RocketChatRollbackHelper(RocketChatService rocketChatService, RocketChatCredentialsHelper rcCredentialsHelper, LogService logService) {
    this.rocketChatService = rocketChatService;
    this.rcCredentialsHelper = rcCredentialsHelper;
    this.logService = logService;
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
        if (!listContainsTechUser(currentList)
            && !rocketChatService.addTechnicalUserToGroup(groupId)) {
          logService.logInternalServerError(String.format(
              "Could not add techical user from Rocket.Chat group id %s during roll back.",
              groupId));
          return;
        }

        if (currentList.size() != memberList.size()) {
          for (GroupMemberDTO member : currentList) {
            if (!memberList.contains(member)) {
              rocketChatService.addUserToGroup(member.get_id(), groupId);
            }
          }
        }

        // Remove technical user from Rocket.Chat group
        if (!rocketChatService.removeTechnicalUserFromGroup(groupId)) {
          logService.logInternalServerError(String.format(
              "Could not remove techical user from Rocket.Chat group id %s during roll back.",
              groupId));
          return;
        }

      } catch (Exception ex) {
        logService.logInternalServerError(String.format(
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
      if (member.get_id().equals(rcCredentialsHelper.getTechnicalUser().getRocketChatUserId())) {
        return true;
      }
    }

    return false;
  }
}

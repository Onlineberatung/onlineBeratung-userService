package de.caritas.cob.userservice.api.adapters.rocketchat;

import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLeaveFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RocketChatRollbackService {

  private final RocketChatService rocketChatService;
  private final RocketChatCredentialsProvider rcCredentialsHelper;

  public RocketChatRollbackService(
      RocketChatService rocketChatService, RocketChatCredentialsProvider rcCredentialsHelper) {
    this.rocketChatService = rocketChatService;
    this.rcCredentialsHelper = rcCredentialsHelper;
  }

  /**
   * Roll back for acceptEnquiry method. Adds already removed users back to the Rocket.Chat group.
   *
   * @param memberList
   */
  public void rollbackRemoveUsersFromRocketChatGroup(
      String groupId, List<GroupMemberDTO> memberList) {

    if (memberList != null && groupId != null) {
      try {
        List<GroupMemberDTO> currentList = rocketChatService.getChatUsers(groupId);

        // Add Rocket.Chat technical user, if not in current member list
        if (!listContainsTechUser(currentList)) {
          try {
            rocketChatService.addTechnicalUserToGroup(groupId);
          } catch (RocketChatAddUserToGroupException e) {
            log.error(
                "Internal Server Error: Could not add technical user from Rocket.Chat group "
                    + "id {} during roll back.",
                groupId);
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

        // Leave from Rocket.Chat group as technical user
        try {
          rocketChatService.leaveFromGroupAsTechnicalUser(groupId);
        } catch (RocketChatLeaveFromGroupException e) {
          log.error(
              "Internal Server Error: Could not leave from Rocket.Chat group as technical user "
                  + "id {} during roll back.",
              groupId);
        }

      } catch (Exception ex) {
        log.error(
            "Internal Server Error: Error during rollback while adding back the users to the "
                + "Rocket.Chat group with id {}",
            groupId,
            ex);
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

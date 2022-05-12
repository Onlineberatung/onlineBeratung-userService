package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserChatDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserSessionListService {

  private final @NonNull SessionService sessionService;
  private final @NonNull ChatService chatService;
  private final @NonNull RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  private final @NonNull SessionListAnalyser sessionListAnalyser;

  /**
   * Returns a list of {@link UserSessionResponseDTO} for the specified user ID.
   *
   * @param userId                Keycloak/MariaDB user ID
   * @param rocketChatCredentials the rocket chat credentials
   * @return {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> retrieveSessionsForAuthenticatedUser(String userId,
      RocketChatCredentials rocketChatCredentials) {

    List<UserSessionResponseDTO> sessions = sessionService.getSessionsForUserId(userId);
    List<UserSessionResponseDTO> chats = chatService.getChatsForUserId(userId);

    return mergeUserSessionsAndChats(sessions, chats, rocketChatCredentials);
  }

  /**
   * Returns a list of {@link UserSessionResponseDTO} for given user ID and rocket chat group or
   * feedback group IDs.
   *
   * @param userId                the ID of an user
   * @param rcGroupIds            the rocket chat group or feedback group IDs
   * @param rocketChatCredentials the credentials for accessing rocket chat
   * @param roles                 the roles of given user
   * @return {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> retrieveSessionsForAuthenticatedUserAndGroupIds(String userId,
      List<String> rcGroupIds, RocketChatCredentials rocketChatCredentials, Set<String> roles) {

    var groupIds = new HashSet<>(rcGroupIds);
    var sessions = sessionService.getSessionsByUserAndGroupOrFeedbackGroupIds(userId, groupIds,
        roles);
    var chats = chatService.getChatSessionsByGroupIds(groupIds);

    return mergeUserSessionsAndChats(sessions, chats, rocketChatCredentials);
  }

  private List<UserSessionResponseDTO> mergeUserSessionsAndChats(
      List<UserSessionResponseDTO> sessions, List<UserSessionResponseDTO> chats,
      RocketChatCredentials rocketChatCredentials) {

    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(rocketChatCredentials);

    List<UserSessionResponseDTO> allSessions = new ArrayList<>();
    allSessions.addAll(updateUserSessionValues(sessions, rocketChatRoomInformation,
        rocketChatCredentials.getRocketChatUserId()));
    allSessions.addAll(updateUserChatValues(chats, rocketChatRoomInformation,
        rocketChatCredentials.getRocketChatUserId()));

    return allSessions;
  }

  private List<UserSessionResponseDTO> updateUserSessionValues(
      List<UserSessionResponseDTO> sessions, RocketChatRoomInformation rocketChatRoomInformation,
      String rcUserId) {

    return sessions.stream()
        .map(sessionDTO -> updateRequiredUserSessionValues(rocketChatRoomInformation, rcUserId,
            sessionDTO))
        .collect(Collectors.toList());
  }

  private UserSessionResponseDTO updateRequiredUserSessionValues(
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId,
      UserSessionResponseDTO userSessionDTO) {

    SessionDTO session = userSessionDTO.getSession();
    String groupId = session.getGroupId();

    session.setMessagesRead(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
        rocketChatRoomInformation.getReadMessages(), groupId));
    if (sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        rocketChatRoomInformation.getLastMessagesRoom(), groupId)) {
      new AvailableLastMessageUpdater(this.sessionListAnalyser)
          .updateSessionWithAvailableLastMessage(rocketChatRoomInformation, rcUserId,
              userSessionDTO::setLatestMessage, session, groupId);
    } else {
      userSessionDTO.setLatestMessage(Helper.UNIXTIME_0);
    }
    return userSessionDTO;
  }

  private List<UserSessionResponseDTO> updateUserChatValues(List<UserSessionResponseDTO> chats,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    return chats.stream()
        .map(chat -> updateRequiredUserChatValues(rocketChatRoomInformation, rcUserId, chat))
        .collect(Collectors.toList());
  }

  private UserSessionResponseDTO updateRequiredUserChatValues(
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId,
      UserSessionResponseDTO chatDTO) {
    UserChatDTO chat = chatDTO.getChat();
    String groupId = chat.getGroupId();

    chat.setSubscribed(
        isRocketChatRoomSubscribedByUser(rocketChatRoomInformation.getUserRooms(), groupId));
    chat.setMessagesRead(sessionListAnalyser
        .areMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getReadMessages(),
            groupId));

    if (sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        rocketChatRoomInformation.getLastMessagesRoom(), groupId)) {
      updateUserChatValuesForAvailableLastMessage(rocketChatRoomInformation, rcUserId, chatDTO,
          chat, groupId);
    } else {
      chatDTO.setLatestMessage(Timestamp.valueOf(chat.getStartDateWithTime()));
    }
    return chatDTO;
  }

  private void updateUserChatValuesForAvailableLastMessage(
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId,
      UserSessionResponseDTO chatDTO, UserChatDTO chat, String groupId) {
    RoomsLastMessageDTO roomsLastMessage = rocketChatRoomInformation.getLastMessagesRoom()
        .get(groupId);
    chat.setLastMessage(StringUtils.isNotEmpty(roomsLastMessage.getMessage()) ? sessionListAnalyser
        .prepareMessageForSessionList(roomsLastMessage.getMessage(), groupId) : null);
    chat.setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
    chatDTO.setLatestMessage(roomsLastMessage.getTimestamp());
    chat.setAttachment(sessionListAnalyser
        .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
  }

  private boolean isRocketChatRoomSubscribedByUser(List<String> userRoomsList,
      String groupId) {
    return nonNull(userRoomsList) && userRoomsList.contains(groupId);
  }

}

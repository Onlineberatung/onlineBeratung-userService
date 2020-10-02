package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListHelper;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSessionListService {

  private final SessionService sessionService;
  private final ChatService chatService;
  private final RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  private final SessionListHelper sessionListHelper;

  @Autowired
  public UserSessionListService(
      SessionService sessionService, ChatService chatService,
      RocketChatRoomInformationProvider rocketChatRoomInformationProvider,
      SessionListHelper sessionListHelper) {
    this.sessionService = requireNonNull(sessionService);
    this.chatService = requireNonNull(chatService);
    this.rocketChatRoomInformationProvider = requireNonNull(rocketChatRoomInformationProvider);
    this.sessionListHelper = requireNonNull(sessionListHelper);
  }

  /**
   * Returns a list of {@link UserSessionResponseDTO} for the specified user ID
   *
   * @param userId                Keycloak/MariaDB user ID
   * @param rocketChatCredentials the rocket chat credentials
   * @return {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> retrieveSessionsForAuthenticatedUser(String userId,
      RocketChatCredentials rocketChatCredentials) {

    List<UserSessionResponseDTO> sessions = sessionService.getSessionsForUserId(userId);
    List<UserSessionResponseDTO> chats = chatService.getChatsForUserId(userId);

    return retrieveMergedListOfUserSessionsAndChats(
        sessions, chats, rocketChatCredentials);

  }

  private List<UserSessionResponseDTO> retrieveMergedListOfUserSessionsAndChats(
      List<UserSessionResponseDTO> sessions,
      List<UserSessionResponseDTO> chats, RocketChatCredentials rocketChatCredentials) {

    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(rocketChatCredentials);

    List<UserSessionResponseDTO> allSessions = new ArrayList<>();
    allSessions.addAll(updateUserSessionValues(sessions, rocketChatRoomInformation,
        rocketChatCredentials.getRocketChatUserId()));
    allSessions.addAll(updateUserChatValues(chats, rocketChatRoomInformation,
        rocketChatCredentials.getRocketChatUserId()));

    return allSessions;
  }

  private List<UserSessionResponseDTO> updateUserSessionValues(List<UserSessionResponseDTO> sessions,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    for (UserSessionResponseDTO session : sessions) {

      String groupId = session.getSession().getGroupId();

      session.getSession().setMessagesRead(
          sessionListHelper.isMessagesForRocketChatGroupReadByUser(
              rocketChatRoomInformation.getMessagesReadMap(),
              groupId));
      if (sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(
          rocketChatRoomInformation.getRoomLastMessageMap(),
          groupId)) {
        RoomsLastMessageDTO roomsLastMessage =
            rocketChatRoomInformation.getRoomLastMessageMap()
                .get(groupId);
        session.getSession().setLastMessage(
            StringUtils.isNotEmpty(roomsLastMessage.getMessage()) ? sessionListHelper
                .prepareMessageForSessionList(roomsLastMessage.getMessage(),
                    groupId) : null);
        session.getSession()
            .setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
        session.setLatestMessage(roomsLastMessage.getTimestamp());
        session.getSession()
            .setAttachment(
                sessionListHelper
                    .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        session.setLatestMessage(Helper.UNIXTIME_0);
      }

    }

    return sessions;
  }

  private List<UserSessionResponseDTO> updateUserChatValues(List<UserSessionResponseDTO> chats,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    for (UserSessionResponseDTO chat : chats) {

      String groupId = chat.getChat().getGroupId();

      chat.getChat().setSubscribed(
          isRocketChatRoomSubscribedByUser(rocketChatRoomInformation.getUserRoomList(), groupId));
      chat.getChat().setMessagesRead(
          sessionListHelper.isMessagesForRocketChatGroupReadByUser(
              rocketChatRoomInformation.getMessagesReadMap(),
              groupId));

      if (sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(
          rocketChatRoomInformation.getRoomLastMessageMap(),
          groupId)) {
        RoomsLastMessageDTO roomsLastMessage = rocketChatRoomInformation.getRoomLastMessageMap()
            .get(groupId);
        chat.getChat().setLastMessage(StringUtils.isNotEmpty(roomsLastMessage.getMessage())
            ? sessionListHelper
            .prepareMessageForSessionList(roomsLastMessage.getMessage(),
                groupId)
            : null);
        chat.getChat()
            .setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
        chat.setLatestMessage(roomsLastMessage.getTimestamp());
        chat.getChat()
            .setAttachment(
                sessionListHelper
                    .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        chat.setLatestMessage(Timestamp.valueOf(chat.getChat().getStartDateWithTime()));
      }

    }

    return chats;
  }

  private boolean isRocketChatRoomSubscribedByUser(List<String> userRoomsList,
      String groupId) {
    return nonNull(userRoomsList) && userRoomsList.contains(groupId);
  }

}

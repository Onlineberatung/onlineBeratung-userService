package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserChatDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

  /**
   * Returns a list of {@link UserSessionResponseDTO} for given user ID and session IDs.
   *
   * @param userId                the ID of an user
   * @param sessionIds            the session IDs
   * @param rocketChatCredentials the credentials for accessing rocket chat
   * @param roles                 the roles of given user
   * @return {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> retrieveSessionsForAuthenticatedUserAndSessionIds(
      String userId, List<Long> sessionIds, RocketChatCredentials rocketChatCredentials,
      Set<String> roles) {

    var uniqueSessionIds = new HashSet<>(sessionIds);
    var sessions = sessionService.getSessionsByUserAndSessionIds(userId, uniqueSessionIds, roles);
    var groupIds = sessions.stream()
        .map(sessionResponse -> sessionResponse.getSession().getGroupId())
        .collect(Collectors.toSet());
    var chats = chatService.getChatSessionsByGroupIds(groupIds);
    return mergeUserSessionsAndChats(sessions, chats, rocketChatCredentials);
  }

  public List<UserSessionResponseDTO> retrieveChatsForUserAndChatIds(List<Long> chatIds, RocketChatCredentials rocketChatCredentials) {
    var uniqueChatIds = new HashSet<>(chatIds);
    var chats = chatService.getChatSessionsByIds(uniqueChatIds);
    var rocketChatRoomInformation = rocketChatRoomInformationProvider.retrieveRocketChatInformation(
        rocketChatCredentials);
    return updateUserChatValues(chats, rocketChatRoomInformation,
        rocketChatCredentials.getRocketChatUserId());
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
    var messageUpdater = new AvailableLastMessageUpdater(this.sessionListAnalyser);
    messageUpdater.updateSessionWithAvailableLastMessage(userSessionDTO.getSession(),
        userSessionDTO::setLatestMessage, rocketChatRoomInformation, rcUserId);
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
      UserSessionResponseDTO sessionResponse) {
    UserChatDTO chat = sessionResponse.getChat();
    String groupId = chat.getGroupId();

    chat.setSubscribed(
        isRocketChatRoomSubscribedByUser(rocketChatRoomInformation.getUserRooms(), groupId));
    chat.setMessagesRead(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
        rocketChatRoomInformation.getReadMessages(), groupId));
    updateUserChatValuesForAvailableLastMessage(rocketChatRoomInformation, rcUserId, sessionResponse, chat);
    return sessionResponse;
  }

  private void updateUserChatValuesForAvailableLastMessage(
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId,
      UserSessionResponseDTO sessionResponse, UserChatDTO chat) {
    new AvailableLastMessageUpdater(sessionListAnalyser).updateChatWithAvailableLastMessage(chat,
        sessionResponse::setLatestMessage, rocketChatRoomInformation, rcUserId);
  }

  private boolean isRocketChatRoomSubscribedByUser(List<String> userRoomsList,
      String groupId) {
    return nonNull(userRoomsList) && userRoomsList.contains(groupId);
  }

}

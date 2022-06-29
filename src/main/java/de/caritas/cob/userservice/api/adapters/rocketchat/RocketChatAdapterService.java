package de.caritas.cob.userservice.api.adapters.rocketchat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.rocketchat.config.RocketChatConfig;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.MessageResponse;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomResponse;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
@RequiredArgsConstructor
public class RocketChatAdapterService implements MessageClient {

  private static final String ENDPOINT_ROOM_INFO = "/rooms.info?roomId=";

  private static final String ENDPOINT_USER_MUTE = "/method.call/muteUserInRoom";

  private static final String ENDPOINT_USER_UNMUTE = "/method.call/unmuteUserInRoom";

  private static final String ENDPOINT_USER_INFO = "/users.info?userId=";

  private static final String ENDPOINT_USER_UPDATE = "/users.update";

  private final RocketChatClient rocketChatClient;

  private final RocketChatConfig rocketChatConfig;

  private final RocketChatMapper mapper;

  @Override
  public boolean muteUserInChat(String username, String roomId) {
    var url = rocketChatConfig.getApiUrl(ENDPOINT_USER_MUTE);
    var muteUser = mapper.muteUserOf(username, roomId);

    try {
      var response = rocketChatClient.postForEntity(url, muteUser, MessageResponse.class);
      return userWasInRoom(response) && response.getStatusCode().is2xxSuccessful();
    } catch (HttpClientErrorException exception) {
      log.error("Muting failed.", exception);
      return false;
    }
  }

  @Override
  public boolean unmuteUserInChat(String username, String roomId) {
    var url = rocketChatConfig.getApiUrl(ENDPOINT_USER_UNMUTE);
    var unmuteUser = mapper.unmuteUserOf(username, roomId);

    try {
      var response = rocketChatClient.postForEntity(url, unmuteUser, MessageResponse.class);
      return response.getStatusCode().is2xxSuccessful();
    } catch (HttpClientErrorException exception) {
      log.error("Un-muting failed.", exception);
      return false;
    }
  }

  @Override
  public boolean updateUser(String chatUserId, String displayName) {
    var url = rocketChatConfig.getApiUrl(ENDPOINT_USER_UPDATE);
    var updateUser = mapper.updateUserOf(chatUserId, displayName);

    try {
      var response = rocketChatClient.postForEntity(url, chatUserId, updateUser, Void.class);
      return response.getStatusCode().is2xxSuccessful();
    } catch (HttpClientErrorException exception) {
      log.error("Setting display failed.", exception);
      return false;
    }
  }

  @Override
  public Optional<Map<String, Object>> findUser(String chatUserId) {
    var url = rocketChatConfig.getApiUrl(ENDPOINT_USER_INFO + chatUserId);

    try {
      var response = rocketChatClient.getForEntity(url, UserInfoResponseDTO.class);
      return mapper.mapOfUserResponse(response);
    } catch (HttpClientErrorException exception) {
      log.error("User Info failed.", exception);
      return Optional.empty();
    }
  }

  @Override
  public Optional<Map<String, Object>> getChatInfo(String roomId) {
    var url = rocketChatConfig.getApiUrl(ENDPOINT_ROOM_INFO + roomId);

    try {
      var response = rocketChatClient.getForEntity(url, RoomResponse.class);
      return mapper.mapOfRoomResponse(response);
    } catch (HttpClientErrorException exception) {
      log.error("Chat Info failed.", exception);
      return Optional.empty();
    }
  }

  private boolean userWasInRoom(ResponseEntity<MessageResponse> response) {
    var body = response.getBody();
    if (nonNull(body)) {
      var message = body.getMessage();
      return isNull(message) || !message.contains("error-user-not-in-room");
    } else {
      return true;
    }
  }
}

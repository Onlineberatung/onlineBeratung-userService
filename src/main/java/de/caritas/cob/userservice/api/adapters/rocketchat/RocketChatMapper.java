package de.caritas.cob.userservice.api.adapters.rocketchat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.Message;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.MuteUnmuteUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomResponse;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UpdateUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.User;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RocketChatMapper {

  private final ObjectMapper mapper;

  public MuteUnmuteUser muteUserOf(@NonNull String username, @NonNull String roomId) {
    return muteOrUnmuteUserOf(username, roomId, true);
  }

  public MuteUnmuteUser unmuteUserOf(@NonNull String username, @NonNull String roomId) {
    return muteOrUnmuteUserOf(username, roomId, false);
  }

  private MuteUnmuteUser muteOrUnmuteUserOf(String username, String roomId, boolean mute) {
    var params = Map.of(
        "rid", roomId,
        "username", username.toLowerCase()
    );

    var message = new Message();
    message.setParams(List.of(params));
    message.setId(new Random().nextInt(100));
    message.setMsg("method");
    var methodName = mute ? "muteUserInRoom" : "unmuteUserInRoom";
    message.setMethod(methodName);

    var muteUnmuteUser = new MuteUnmuteUser();
    try {
      var messageString = mapper.writeValueAsString(message);
      muteUnmuteUser.setMessage(messageString);
    } catch (JsonProcessingException e) {
      log.error("Serializing {} did not work.", message);
    }

    return muteUnmuteUser;
  }

  public Optional<Map<String, Object>> mapOfRoomResponse(ResponseEntity<RoomResponse> response) {
    var body = response.getBody();
    if (nonNull(body)) {
      var room = body.getRoom();
      var mutedUsers = isNull(room.getMuted()) ? List.of() : room.getMuted();
      var map = Map.of(
          "id", room.getId(),
          "mutedUsers", mutedUsers
      );
      return Optional.of(map);
    }

    return Optional.empty();
  }

  public UpdateUser updateUserOf(String chatUserId, String displayName) {
    var updateUser = new UpdateUser();
    updateUser.setUserId(chatUserId);

    var user = new User();
    user.setName(displayName);
    updateUser.setData(user);

    return updateUser;
  }

  public Optional<Map<String, Object>> mapOfUserResponse(
      ResponseEntity<UserInfoResponseDTO> userResponse) {
    var body = userResponse.getBody();
    if (nonNull(body)) {
      var user = body.getUser();
      var map = new HashMap<String, Object>();
      map.put("id", user.getId());
      if (nonNull(user.getUsername())) {
        map.put("username", user.getUsername());
      }
      if (nonNull(user.getName())) {
        map.put("displayName", user.getName());
      }

      return Optional.of(map);
    }

    return Optional.empty();
  }
}

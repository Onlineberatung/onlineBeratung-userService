package de.caritas.cob.userservice.api.adapters.rocketchat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.Message;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.MuteUnmuteUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.RoomResponse;
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
        "username", username
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

  public Optional<Map<String, Object>> mapOf(ResponseEntity<RoomResponse> roomResponse) {
    var body = roomResponse.getBody();
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
}

package de.caritas.cob.userservice.api.adapters.rocketchat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupUpdateKeyDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceDTO.PresenceStatus;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceListDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceOtherDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.Message;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.MethodCall;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.MethodMessageWithParamList;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomResponse;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomSettingsDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.MuteUnmuteUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UpdateUser;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.User;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RocketChatMapper {

  @SuppressWarnings("java:S2245")
  // Using pseudorandom number generators (PRNGs) is security-sensitive
  private static final Random random = new Random();

  private final ObjectMapper mapper;

  public MuteUnmuteUser muteUserOf(@NonNull String username, @NonNull String roomId) {
    return muteOrUnmuteUserOf(username, roomId, true);
  }

  public MuteUnmuteUser unmuteUserOf(@NonNull String username, @NonNull String roomId) {
    return muteOrUnmuteUserOf(username, roomId, false);
  }

  private MuteUnmuteUser muteOrUnmuteUserOf(String username, String roomId, boolean mute) {
    var params = Map.of("rid", roomId, "username", username.toLowerCase());

    var message = new Message();
    message.setParams(List.of(params));
    message.setId(random.nextInt(100));
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

  public MethodCall setUserPresenceOf(String status) {
    PresenceStatus.valueOf(status.toUpperCase());

    var message = new MethodMessageWithParamList();
    message.setParams(List.of(status));
    message.setId(random.nextInt(100));
    message.setMethod("UserPresence:setDefaultStatus");

    var setUserPresence = new MethodCall();
    try {
      var messageString = mapper.writeValueAsString(message);
      setUserPresence.setMessage(messageString);
    } catch (JsonProcessingException e) {
      log.error("Serializing {} did not work.", message);
    }

    return setUserPresence;
  }

  public Optional<Map<String, Object>> mapOfRoomResponse(ResponseEntity<RoomResponse> response) {
    var body = response.getBody();
    if (nonNull(body)) {
      var room = body.getRoom();
      var mutedUsers = isNull(room.getMuted()) ? List.of() : room.getMuted();
      var map = Map.of("id", room.getId(), "mutedUsers", mutedUsers);
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
    if (nonNull(userResponse)) {
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
    }
    return Optional.empty();
  }

  public Optional<List<Map<String, String>>> mapOfSubscriptionsResponse(
      ResponseEntity<SubscriptionsGetDTO> subscriptionsResponse) {
    var body = subscriptionsResponse.getBody();
    if (nonNull(body)) {
      var updates = body.getUpdate();
      var list = new ArrayList<Map<String, String>>(updates.length);
      for (var update : updates) {
        var map = new HashMap<String, String>();
        var e2eKey = update.getE2eKey();
        if (nonNull(e2eKey)) {
          map.put("e2eKey", e2eKey);
        }
        map.put("userId", update.getUser().getId());
        map.put("roomId", update.getRoomId());
        list.add(map);
      }

      return Optional.of(list);
    }

    return Optional.empty();
  }

  public GroupUpdateKeyDTO updateGroupKeyOf(String chatUserId, String roomId, String key) {
    var updateGroupKey = new GroupUpdateKeyDTO();
    updateGroupKey.setUid(chatUserId);
    updateGroupKey.setRid(roomId);
    updateGroupKey.setKey(key);

    return updateGroupKey;
  }

  public List<Map<String, String>> mapOf(List<GroupMemberDTO> members) {
    return members.stream()
        .map(member -> Map.of("chatUserId", member.get_id()))
        .collect(Collectors.toList());
  }

  public Map<String, Object> mapOfRoomSettings(String chatId, boolean encrypted) {
    var roomSettings = new RoomSettingsDTO(chatId, encrypted);
    var map = new HashMap<String, Object>();

    map.put("rid", roomSettings.getRid());
    map.put("encrypted", roomSettings.isEncrypted());

    return map;
  }

  public Set<String> mapAvailableOf(PresenceListDTO presenceList) {
    return presenceList.getUsers().stream()
        .filter(PresenceOtherDTO::isAvailable)
        .map(PresenceOtherDTO::getId)
        .collect(Collectors.toSet());
  }
}

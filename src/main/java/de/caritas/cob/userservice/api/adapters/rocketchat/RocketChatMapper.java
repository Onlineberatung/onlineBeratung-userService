package de.caritas.cob.userservice.api.adapters.rocketchat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.Message;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.MuteUser;
import java.util.Map;
import java.util.Random;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RocketChatMapper {

  private final ObjectMapper mapper;

  public MuteUser muteUserOf(@NonNull String username, @NonNull String roomId) {
    var params = Map.of(
        "rid", roomId,
        "username", username
    );

    var message = new Message();
    message.setParams(params);
    message.setId(new Random().nextInt(100));
    message.setMsg("method");
    message.setMethod("muteUserInRoom");

    var muteUser = new MuteUser();
    try {
      var messageString = mapper.writeValueAsString(message);
      muteUser.setMessage(messageString);
    } catch (JsonProcessingException e) {
      log.error("Serializing {} did not work.", message);
    }

    return muteUser;
  }
}

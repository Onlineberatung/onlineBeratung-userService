package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.port.in.Messaging;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Messenger implements Messaging {

  private final MessageClient messageClient;

  private final UserRepository userRepository;

  private final ConsultantRepository consultantRepository;

  private final ChatRepository chatRepository;

  @Override
  public boolean banUserFromChat(String consultantId, String adviceSeekerId, long chatId) {
    var adviceSeeker = userRepository.findByUserIdAndDeleteDateIsNull(adviceSeekerId).orElseThrow();
    var consultant = consultantRepository.findByIdAndDeleteDateIsNull(consultantId).orElseThrow();
    var chat = chatRepository.findById(chatId).orElseThrow();

    return messageClient.muteUserInRoom(
        consultant.getRocketChatId(), adviceSeeker.getUsername(), chat.getGroupId()
    );
  }
}

package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.port.in.Messaging;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Messenger implements Messaging {

  private final MessageClient messageClient;

  private final UserRepository userRepository;

  private final ChatRepository chatRepository;

  private final UserServiceMapper mapper;

  @Override
  public boolean banUserFromChat(String adviceSeekerId, long chatId) {
    var adviceSeeker = userRepository.findByUserIdAndDeleteDateIsNull(adviceSeekerId).orElseThrow();
    var chat = chatRepository.findById(chatId).orElseThrow();

    return messageClient.muteUserInChat(adviceSeeker.getUsername(), chat.getGroupId());
  }

  @Override
  public void unbanUsersInChat(Long chatId, String consultantId) {
    findChatMetaInfo(chatId, consultantId).ifPresent(chatMetaInfoMap -> {
      var chat = chatRepository.findById(chatId).orElseThrow();
      mapper.bannedUsernamesOfMap(chatMetaInfoMap).forEach(username ->
          messageClient.unmuteUserInChat(username, chat.getGroupId())
      );
    });
  }

  @Override
  public boolean existsChat(long chatId) {
    return findChat(chatId).isPresent();
  }

  @Override
  public Optional<Chat> findChat(long chatId) {
    return chatRepository.findById(chatId);
  }

  @Override
  public Optional<Map<String, Object>> findChatMetaInfo(long chatId, String userId) {
    var chat = findChat(chatId).orElseThrow();

    return messageClient.getChatInfo(chat.getGroupId());
  }
}

package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.port.in.Messaging;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Messenger implements Messaging {

  private final MessageClient messageClient;

  private final UserRepository userRepository;

  private final ConsultantRepository consultantRepository;

  private final ChatRepository chatRepository;

  private final UserServiceMapper mapper;

  @Override
  public boolean banUserFromChat(String consultantId, String adviceSeekerId, long chatId) {
    var adviceSeeker = userRepository.findByUserIdAndDeleteDateIsNull(adviceSeekerId).orElseThrow();
    var chat = chatRepository.findById(chatId).orElseThrow();

    return messageClient.muteUserInChat(adviceSeeker.getUsername(), chat.getGroupId());
  }

  @Override
  public void unbanUsersInChat(Long chatId, String consultantId) {
    findChatMetaInfo(chatId, consultantId).ifPresent(chatMetaInfoMap -> {
      var chatUserIds = mapper.bannedChatUserIdsOfMap(chatMetaInfoMap);
      var chat = chatRepository.findById(chatId).orElseThrow();

      chatUserIds.forEach(chatUserId -> {
        var username = getUsername(chatUserId);
        messageClient.unmuteUserInChat(username, chat.getGroupId());
      });
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
    var chatUserId = getChatUserId(userId);

    return messageClient.getChatInfo(chat.getGroupId(), chatUserId);
  }

  private String getChatUserId(String userId) {
    var chatUserId = new AtomicReference<String>();
    userRepository.findByUserIdAndDeleteDateIsNull(userId).ifPresentOrElse(
        user -> chatUserId.set(user.getRcUserId()),
        () -> consultantRepository.findByIdAndDeleteDateIsNull(userId).ifPresentOrElse(
            consultant -> chatUserId.set(consultant.getRocketChatId()),
            () -> {
              throw new NoSuchElementException();
            }));

    return chatUserId.toString();
  }

  private String getUsername(String chatUserId) {
    var username = new AtomicReference<String>();
    userRepository.findByRcUserIdAndDeleteDateIsNull(chatUserId).ifPresentOrElse(
        user -> username.set(user.getUsername()),
        () -> consultantRepository.findByRocketChatIdAndDeleteDateIsNull(chatUserId)
            .ifPresentOrElse(consultant -> username.set(consultant.getUsername()),
                () -> {
                  throw new NoSuchElementException();
                }));

    return username.toString();
  }
}

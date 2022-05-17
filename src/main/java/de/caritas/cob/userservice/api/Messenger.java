package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.port.in.Messaging;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.StringConverter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Messenger implements Messaging {

  private final MessageClient messageClient;

  private final UserRepository userRepository;

  private final ChatRepository chatRepository;

  private final UserServiceMapper mapper;

  private final StringConverter stringConverter;

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
  public boolean updateE2eKeys(String chatUserId, String publicKey) {
    var allUpdated = new AtomicBoolean(true);

    messageClient.findAllChats(chatUserId).ifPresent(chats -> {
      if (allChatsAreTmpEncrypted(chats)) {
        var masterKey = stringConverter.hashOf(chatUserId);
        for (var chat : chats) {
          var roomKeyId = chat.get("e2eKey");
          var keyId = roomKeyId.substring(4, 16);
          var encryptedRoomKey = roomKeyId.substring(16);
          var roomKey = stringConverter.aesDecrypt(encryptedRoomKey, masterKey);
          var rsaEncrypted = stringConverter.rsaBcEncrypt(roomKey, publicKey);
          var intArray = stringConverter.int8Array(rsaEncrypted);
          var jsonStringified = stringConverter.jsonStringify(intArray);
          var updatedE2eKey = keyId + stringConverter.encodeBase64Ascii(jsonStringified);
          var userId = chat.get("userId");
          var roomId = chat.get("roomId");
          if (!messageClient.updateChatE2eKey(userId, roomId, updatedE2eKey)) {
            allUpdated.set(false);
            break;
          }
        }
      } else {
        allUpdated.set(false);
      }
    });

    return allUpdated.get();
  }

  private boolean allChatsAreTmpEncrypted(List<Map<String, String>> chatMaps) {
    return chatMaps.stream().allMatch(chatMap ->
        chatMap.containsKey("e2eKey") && chatMap.get("e2eKey").matches("tmp\\..{12,}")
    );
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

package de.caritas.cob.userservice.api;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.in.IdentityManaging;
import de.caritas.cob.userservice.api.port.in.Messaging;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.StringConverter;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class Messenger implements Messaging {

  private final MessageClient messageClient;
  private final UserRepository userRepository;
  private final ConsultantRepository consultantRepository;
  private final ChatRepository chatRepository;
  private final SessionRepository sessionRepository;
  private final UserServiceMapper mapper;
  private final StringConverter stringConverter;
  private final IdentityManaging identityManager;
  private final AgencyService agencyService;

  @Override
  public boolean banUserFromChat(String adviceSeekerId, long chatId) {
    var adviceSeeker = userRepository.findByUserIdAndDeleteDateIsNull(adviceSeekerId).orElseThrow();
    var chat = chatRepository.findById(chatId).orElseThrow();

    return messageClient.muteUserInChat(adviceSeeker.getUsername(), chat.getGroupId());
  }

  @Override
  public void unbanUsersInChat(Long chatId, String consultantId) {
    findChatMetaInfo(chatId, consultantId)
        .ifPresent(
            chatMetaInfoMap -> {
              var chat = chatRepository.findById(chatId).orElseThrow();
              mapper
                  .bannedUsernamesOfMap(chatMetaInfoMap)
                  .forEach(username -> messageClient.unmuteUserInChat(username, chat.getGroupId()));
            });
  }

  @Override
  public void setAvailability(String consultantId, boolean available) {
    var consultant = consultantRepository.findByIdAndDeleteDateIsNull(consultantId).orElseThrow();
    var status = mapper.statusOf(available);
    var userChatId = consultant.getRocketChatId();

    messageClient.setUserPresence(userChatId, status);
  }

  @Override
  public boolean getAvailability(String consultantId) {
    var consultant = consultantRepository.findByIdAndDeleteDateIsNull(consultantId).orElseThrow();
    var chatUserId = consultant.getRocketChatId();

    return messageClient.isAvailable(chatUserId).orElse(false);
  }

  @Override
  public Set<String> findAvailableConsultants(int consultingTypeId) {
    var presentUserIds = messageClient.findAllAvailableUserIds();

    if (!presentUserIds.isEmpty()) {
      var agencyIds =
          agencyService.getAgenciesByConsultingType(consultingTypeId).stream()
              .map(AgencyDTO::getId)
              .collect(Collectors.toSet());

      var consultantIdsInType = consultantRepository.findAllByAgencyIds(agencyIds);
      presentUserIds.retainAll(consultantIdsInType);
    }

    return presentUserIds;
  }

  @Override
  public Boolean updateE2eKeys(String chatUserId, String publicKey) {
    var allUpdated = new AtomicReference<>(true);

    messageClient
        .findAllChats(chatUserId)
        .ifPresent(
            chats -> {
              var masterKey = stringConverter.hashOf(chatUserId);
              for (var chat : chats) {
                var userId = mapper.userIdOf(chat);
                var roomId = mapper.roomIdOf(chat);
                if (mapper.e2eKeyOf(chat).isPresent()) {
                  var roomKeyId = mapper.e2eKeyOf(chat).orElseThrow();
                  var updatedE2eKey = createE2eKey(publicKey, masterKey, roomKeyId);
                  if (!messageClient.updateChatE2eKey(userId, roomId, updatedE2eKey)) {
                    allUpdated.set(false);
                    break;
                  }
                } else {
                  log.info("Ignoring non-temp chat ({}) of user ({})", roomId, userId);
                }
              }
            });

    return allUpdated.get();
  }

  private String createE2eKey(String publicKey, String masterKey, String roomKeyId) {
    var keyId = roomKeyId.substring(4, 16);
    var encryptedRoomKey = roomKeyId.substring(16);
    var roomKey = stringConverter.aesDecrypt(encryptedRoomKey, masterKey);
    var rsaEncrypted = stringConverter.rsaEncrypt(roomKey, publicKey);
    var intArray = stringConverter.int8Array(rsaEncrypted);
    var jsonStringified = stringConverter.jsonStringify(intArray);

    return keyId + stringConverter.base64AsciiEncode(jsonStringified);
  }

  @Override
  public boolean removeUserFromSession(String chatUserId, String chatId) {
    var session = sessionRepository.findByGroupId(chatId).orElseThrow();
    var consultant =
        consultantRepository.findByRocketChatIdAndDeleteDateIsNull(chatUserId).orElseThrow();
    var removedOrIgnored = new AtomicBoolean(true);

    if (!session.isAdvisedBy(consultant) && !isResponsible(session, consultant)) {
      if (isInChat(chatId, chatUserId)
          && !isTeaming(session, consultant)
          && !isPeering(session, consultant)) {
        removedOrIgnored.set(messageClient.removeUserFromSession(chatUserId, chatId));
      }

      var feedbackChatId = session.getFeedbackGroupId();
      if (isInChat(feedbackChatId, chatUserId) && !isMain(consultant)) {
        removedOrIgnored.compareAndExchange(
            true, messageClient.removeUserFromSession(chatUserId, feedbackChatId));
      }
    }

    return removedOrIgnored.get();
  }

  private boolean isResponsible(Session session, Consultant consultant) {
    return session.isTeamSession() && consultant.isInAgency(session.getAgencyId());
  }

  private boolean isTeaming(Session session, Consultant consultant) {
    return !session.hasFeedbackChat() && consultant.isTeamConsultant();
  }

  private boolean isPeering(Session session, Consultant consultant) {
    return session.hasFeedbackChat() && identityManager.canViewPeerSessions(consultant.getId());
  }

  private boolean isMain(Consultant consultant) {
    return identityManager.canViewFeedbackSessions(consultant.getId());
  }

  public boolean isInChat(String chatId, String chatUserId) {
    if (isNull(chatId)) {
      return false;
    }

    var groupMembers = messageClient.findMembers(chatId).orElseThrow();
    var chatUserIds = mapper.chatUserIdOf(groupMembers);

    return chatUserIds.contains(chatUserId);
  }

  @Override
  public boolean markAsDirectConsultant(Long sessionId) {
    return sessionRepository
        .findById(sessionId)
        .map(
            session -> {
              session.setIsConsultantDirectlySet(true);
              var updatedSession = sessionRepository.save(session);
              return updatedSession.getIsConsultantDirectlySet();
            })
        .orElse(false);
  }

  @Override
  public Optional<Map<String, Object>> findSession(Long sessionId) {
    var session = sessionRepository.findById(sessionId);

    return mapper.mapOf(session);
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

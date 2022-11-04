package de.caritas.cob.userservice.api.service;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.web.dto.ChatDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateChatResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserChatDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Chat.ChatInterval;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.UserChat;
import de.caritas.cob.userservice.api.port.out.ChatAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.UserChatRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Chat service class */
@Service
@RequiredArgsConstructor
public class ChatService {

  private final @NonNull ChatRepository chatRepository;
  private final @NonNull ChatAgencyRepository chatAgencyRepository;
  private final @NonNull UserChatRepository userChatRepository;
  private final @NonNull ConsultantService consultantService;

  /**
   * Returns a list of current chats for the provided {@link Consultant}
   *
   * @return list of chats as {@link ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> getChatsForConsultant(Consultant consultant) {
    Set<Long> agencyIds =
        consultant.getConsultantAgencies().stream()
            .map(ConsultantAgency::getAgencyId)
            .collect(Collectors.toSet());
    return chatRepository.findByAgencyIds(agencyIds).stream()
        .map(this::convertChatToConsultantSessionResponseDTO)
        .collect(Collectors.toList());
  }

  private ConsultantSessionResponseDTO convertChatToConsultantSessionResponseDTO(Chat chat) {
    return new ConsultantSessionResponseDTO()
        .chat(createUserChat(chat))
        .consultant(
            new SessionConsultantForConsultantDTO()
                .id(chat.getChatOwner().getId())
                .firstName(chat.getChatOwner().getFirstName())
                .lastName(chat.getChatOwner().getLastName()));
  }

  private String[] getChatModerators(Set<ChatAgency> chatAgencies) {
    return consultantService.findConsultantsByAgencyIds(chatAgencies).stream()
        .map(Consultant::getRocketChatId)
        .toArray(String[]::new);
  }

  /**
   * Saves a {@link Chat} to MariaDB
   *
   * @param chat {@link Chat}
   * @return {@link Chat} (will never be null)
   */
  public Chat saveChat(Chat chat) {
    return chatRepository.save(chat);
  }

  /**
   * Saves a {@link ChatAgency} to MariaDB
   *
   * @param chatAgency {@link ChatAgency}
   * @return {@link ChatAgency} (will never be null)
   */
  public ChatAgency saveChatAgencyRelation(ChatAgency chatAgency) {
    return chatAgencyRepository.save(chatAgency);
  }

  /**
   * Saves a {@link UserChat} relation
   *
   * @param userChat {@link UserChat}
   * @return saved {@link UserChat}
   */
  public UserChat saveUserChatRelation(UserChat userChat) {
    return userChatRepository.save(userChat);
  }

  /**
   * Returns the list of current chats for the provided userId.
   *
   * <p>The chats are collected from the user_agency relation (V1) and the user_chat relation (V2).
   *
   * @param userId the id of the user
   * @return list of user chats as {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> getChatsForUserId(String userId) {
    List<Chat> chats = chatRepository.findByUserId(userId);
    List<Chat> assignedChats = chatRepository.findAssignedByUserId(userId);
    return Stream.concat(chats.stream(), assignedChats.stream())
        .map(this::convertChatToUserSessionResponseDTO)
        .collect(Collectors.toList());
  }

  public List<UserSessionResponseDTO> getChatSessionsByIds(Set<Long> chatIds) {
    return StreamSupport.stream(chatRepository.findAllById(chatIds).spliterator(), false)
        .map(this::convertChatToUserSessionResponseDTO)
        .collect(Collectors.toList());
  }

  private UserSessionResponseDTO convertChatToUserSessionResponseDTO(Chat chat) {
    return new UserSessionResponseDTO().chat(createUserChat(chat));
  }

  private UserChatDTO createUserChat(Chat chat) {
    return new UserChatDTO(
        chat.getId(),
        chat.getTopic(),
        LocalDate.of(
            chat.getStartDate().getYear(),
            chat.getStartDate().getMonth(),
            chat.getStartDate().getDayOfMonth()),
        LocalTime.of(
            chat.getStartDate().getHour(),
            chat.getStartDate().getMinute(),
            chat.getStartDate().getSecond()),
        chat.getDuration(),
        isTrue(chat.isRepetitive()),
        isTrue(chat.isActive()),
        chat.getConsultingTypeId(),
        null,
        null,
        false,
        chat.getGroupId(),
        null,
        false,
        getChatModerators(chat.getChatAgencies()),
        chat.getStartDate(),
        null);
  }

  /**
   * Returns an {@link Optional} of {@link Chat} for the provided chat ID.
   *
   * @param chatId chat ID
   * @return {@link Optional} of {@link Chat}
   */
  public Optional<Chat> getChat(Long chatId) {
    return chatRepository.findById(chatId);
  }

  /**
   * Returns an {@link Optional} of {@link Chat} for the provided group ID.
   *
   * @param groupId rocket chat group ID
   * @return {@link Optional} of {@link Chat}
   */
  public Optional<Chat> getChatByGroupId(String groupId) {
    return chatRepository.findByGroupId(groupId);
  }

  public List<ConsultantSessionResponseDTO> getChatSessionsForConsultantByIds(Set<Long> chatIds) {
    return StreamSupport.stream(chatRepository.findAllById(chatIds).spliterator(), false)
        .map(this::convertChatToConsultantSessionResponseDTO)
        .collect(Collectors.toList());
  }

  /**
   * Returns an {@link List} of {@link UserSessionResponseDTO} for the provided group IDs.
   *
   * @param groupIds a list of rocket chat group or feedback group IDs
   * @return {@link List<UserSessionResponseDTO>}
   */
  public List<UserSessionResponseDTO> getChatSessionsByGroupIds(Set<String> groupIds) {
    return chatRepository.findByGroupIds(groupIds).stream()
        .map(this::convertChatToUserSessionResponseDTO)
        .collect(Collectors.toList());
  }

  public List<ConsultantSessionResponseDTO> getChatSessionsForConsultantByGroupIds(
      Set<String> groupIds) {
    return chatRepository.findByGroupIds(groupIds).stream()
        .map(this::convertChatToConsultantSessionResponseDTO)
        .collect(Collectors.toList());
  }

  /**
   * Delete a {@link Chat}
   *
   * @param chat the {@link Chat}
   */
  public void deleteChat(Chat chat) {
    chatRepository.delete(chat);
  }

  /**
   * Updates topic, duration, repetitive and start date of the provided {@link Chat}.
   *
   * @param chatId chat ID
   * @param chatDTO {@link ChatDTO}
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link UpdateChatResponseDTO}
   */
  public UpdateChatResponseDTO updateChat(
      Long chatId, ChatDTO chatDTO, AuthenticatedUser authenticatedUser) {

    Chat chat =
        getChat(chatId)
            .orElseThrow(
                () ->
                    new BadRequestException(
                        String.format("Chat with id %s does not exist", chatId)));

    if (!authenticatedUser.getUserId().equals(chat.getChatOwner().getId())) {
      throw new ForbiddenException("Only the chat owner is allowed to change chat settings");
    }
    if (isTrue(chat.isActive())) {
      throw new ConflictException(
          String.format(
              "Chat with id %s is active. Therefore changing the chat settings is not supported.",
              chatId));
    }

    LocalDateTime startDate = LocalDateTime.of(chatDTO.getStartDate(), chatDTO.getStartTime());
    chat.setTopic(chatDTO.getTopic());
    chat.setDuration(chatDTO.getDuration());
    chat.setRepetitive(isTrue(chatDTO.isRepetitive()));
    chat.setChatInterval(isTrue(chatDTO.isRepetitive()) ? ChatInterval.WEEKLY : null);
    chat.setStartDate(startDate);
    chat.setInitialStartDate(startDate);

    this.saveChat(chat);

    return new UpdateChatResponseDTO().groupId(chat.getGroupId());
  }
}

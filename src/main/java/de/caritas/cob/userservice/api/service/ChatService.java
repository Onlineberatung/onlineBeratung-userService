package de.caritas.cob.userservice.api.service;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.SaveChatAgencyException;
import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.chat.ChatDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateChatResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.model.chat.UserChatDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chat.ChatInterval;
import de.caritas.cob.userservice.api.repository.chat.ChatRepository;
import de.caritas.cob.userservice.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.userservice.api.repository.chatAgency.ChatAgencyRepository;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Chat service class
 */

@Service
@RequiredArgsConstructor
public class ChatService {

  private final @NonNull ChatRepository chatRepository;
  private final @NonNull ChatAgencyRepository chatAgencyRepository;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserHelper userHelper;

  /**
   * Returns a list of current chats for the provided {@link Consultant}
   *
   * @return list of chats as {@link ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> getChatsForConsultant(Consultant consultant) {

    List<ConsultantSessionResponseDTO> sessionResponseDTOs = null;
    List<Chat> chats;

    try {
      chats = chatRepository.findByAgencyIds(consultant.getConsultantAgencies().stream()
          .map(ConsultantAgency::getAgencyId).collect(Collectors.toSet()));

    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          String.format("Database error while retrieving the chats for the consultant with id %s",
              consultant.getId()), LogService::logDatabaseError);
    }

    if (isNotEmpty(chats)) {
      sessionResponseDTOs =
          chats.stream().map(this::convertChatToConsultantSessionResponseDTO)
              .collect(Collectors.toList());
    }

    return sessionResponseDTOs;
  }

  private ConsultantSessionResponseDTO convertChatToConsultantSessionResponseDTO(Chat chat) {
    return new ConsultantSessionResponseDTO()
        .chat(new UserChatDTO(chat.getId(), chat.getTopic(),
            LocalDate.of(chat.getStartDate().getYear(), chat.getStartDate().getMonth(),
                chat.getStartDate().getDayOfMonth()),
            LocalTime.of(chat.getStartDate().getHour(), chat.getStartDate().getMinute(),
                chat.getStartDate().getSecond()),
            chat.getDuration(), isTrue(chat.isRepetitive()), isTrue(chat.isActive()),
            chat.getConsultingType().getValue(), null, null, false, chat.getGroupId(), null, false,
            getChatModerators(chat.getChatAgencies()), chat.getStartDate()))
        .consultant(new SessionConsultantForConsultantDTO()
            .id(chat.getChatOwner().getId())
            .firstName(chat.getChatOwner().getFirstName())
            .lastName(chat.getChatOwner().getLastName()));
  }

  private String[] getChatModerators(Set<ChatAgency> chatAgencies) {

    List<Consultant> consultantList = consultantService.findConsultantsByAgencyIds(chatAgencies);

    if (isNotEmpty(consultantList)) {
      return consultantList.stream().map(Consultant::getRocketChatId).toArray(String[]::new);
    }

    return new String[0];
  }

  /**
   * Saves a {@link Chat} to MariaDB
   *
   * @param chat {@link Chat}
   * @return {@link Chat} (will never be null)
   */
  public Chat saveChat(Chat chat) throws SaveChatException {
    try {
      return chatRepository.save(chat);
    } catch (DataAccessException ex) {
      throw new SaveChatException(String.format("Creation of chat failed for: %s", chat.toString()),
          ex);
    }
  }

  /**
   * Saves a {@link ChatAgency} to MariaDB
   *
   * @param chatAgency {@link ChatAgency}
   * @return {@link ChatAgency} (will never be null)
   */
  public ChatAgency saveChatAgencyRelation(ChatAgency chatAgency) throws SaveChatAgencyException {
    try {
      return chatAgencyRepository.save(chatAgency);
    } catch (DataAccessException ex) {
      throw new SaveChatAgencyException(
          String.format("Creation of chat - user relation failed for: %s", chatAgency.toString()),
          ex);
    }
  }

  /**
   * Returns the list of current chats for the provided user (Id).
   *
   * @param userId the id of the user
   * @return list of user chats as {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> getChatsForUserId(String userId) {

    try {
      List<Chat> chats = chatRepository.findByUserId(userId);
      return chats.stream().map(this::convertChatToUserSessionResponseDTO)
          .collect(Collectors.toList());
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(String
          .format("Database error while retrieving the chats for the user with id %s", userId),
          LogService::logDatabaseError);
    }

  }

  private UserSessionResponseDTO convertChatToUserSessionResponseDTO(Chat chat) {
    return new UserSessionResponseDTO().chat(new UserChatDTO(chat.getId(), chat.getTopic(),
        LocalDate.of(chat.getStartDate().getYear(), chat.getStartDate().getMonth(),
            chat.getStartDate().getDayOfMonth()),
        LocalTime.of(chat.getStartDate().getHour(), chat.getStartDate().getMinute(),
            chat.getStartDate().getSecond()),
        chat.getDuration(), isTrue(chat.isRepetitive()), isTrue(chat.isActive()),
        chat.getConsultingType().getValue(), null, null, false, chat.getGroupId(), null, false,
        getChatModerators(chat.getChatAgencies()), chat.getStartDate()));
  }

  /**
   * Returns an {@link Optional} of {@link Chat} for the provided chat ID.
   *
   * @param chatId chat ID
   * @return {@link Optional} of {@link Chat}
   */
  public Optional<Chat> getChat(Long chatId) {
    Optional<Chat> chat;

    try {
      chat = chatRepository.findById(chatId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          String.format("Database error while retrieving chat with id %s", chatId),
          LogService::logDatabaseError);
    }

    return chat;
  }

  /**
   * Delete a {@link Chat}
   *
   * @param chat the {@link Chat}
   */
  public void deleteChat(Chat chat) {
    try {
      chatRepository.delete(chat);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          String.format("Deletion of chat with id %s failed", chat.getId()),
          LogService::logDatabaseError);
    }
  }

  /**
   * Updates topic, duration, repetitive and start date of the provided {@link Chat}.
   *
   * @param chatId            chat ID
   * @param chatDTO           {@link ChatDTO}
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link UpdateChatResponseDTO}
   */
  public UpdateChatResponseDTO updateChat(Long chatId, ChatDTO chatDTO,
      AuthenticatedUser authenticatedUser) {

    Optional<Chat> chat = getChat(chatId);

    if (!chat.isPresent()) {
      throw new BadRequestException(String.format("Chat with id %s does not exist", chatId));
    }
    if (!authenticatedUser.getUserId().equals(chat.get().getChatOwner().getId())) {
      throw new ForbiddenException("Only the chat owner is allowed to change chat settings");
    }
    if (isTrue(chat.get().isActive())) {
      throw new ConflictException(String.format(
          "Chat with id %s is active. Therefore changing the chat settings is not supported.",
          chatId));
    }

    LocalDateTime startDate = LocalDateTime.of(chatDTO.getStartDate(), chatDTO.getStartTime());
    chat.get().setTopic(chatDTO.getTopic());
    chat.get().setDuration(chatDTO.getDuration());
    chat.get().setRepetitive(isTrue(chatDTO.isRepetitive()));
    chat.get().setChatInterval(isTrue(chatDTO.isRepetitive()) ? ChatInterval.WEEKLY : null);
    chat.get().setStartDate(startDate);
    chat.get().setInitialStartDate(startDate);

    try {
      this.saveChat(chat.get());
    } catch (SaveChatException e) {
      throw new InternalServerErrorException(e.getMessage());
    }

    return new UpdateChatResponseDTO()
        .groupId(chat.get().getGroupId())
        .chatLink(userHelper.generateChatUrl(chat.get().getId(), chat.get().getConsultingType()));
  }

}

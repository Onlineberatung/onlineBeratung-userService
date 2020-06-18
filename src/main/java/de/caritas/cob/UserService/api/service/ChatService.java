package de.caritas.cob.UserService.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.exception.SaveChatAgencyException;
import de.caritas.cob.UserService.api.exception.SaveChatException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.responses.BadRequestException;
import de.caritas.cob.UserService.api.exception.responses.ConflictException;
import de.caritas.cob.UserService.api.exception.responses.ForbiddenException;
import de.caritas.cob.UserService.api.helper.AuthenticatedUser;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.model.ChatDTO;
import de.caritas.cob.UserService.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.UserService.api.model.SessionConsultantForConsultantDTO;
import de.caritas.cob.UserService.api.model.UpdateChatResponseDTO;
import de.caritas.cob.UserService.api.model.UserSessionResponseDTO;
import de.caritas.cob.UserService.api.model.chat.UserChatDTO;
import de.caritas.cob.UserService.api.repository.chat.Chat;
import de.caritas.cob.UserService.api.repository.chat.ChatInterval;
import de.caritas.cob.UserService.api.repository.chat.ChatRepository;
import de.caritas.cob.UserService.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.UserService.api.repository.chatAgency.ChatAgencyRepository;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;

/**
 * Chat service class
 *
 */

@Service
public class ChatService {

  private final ChatRepository chatRepository;
  private final ChatAgencyRepository chatAgencyRepository;
  private final LogService logService;
  private final ConsultantService consultantService;
  private final UserHelper userHelper;

  @Autowired
  public ChatService(ChatRepository chatRepository, ChatAgencyRepository chatAgencyRepository,
      LogService logService, ConsultantService consultantService, UserHelper userHelper) {
    this.chatRepository = chatRepository;
    this.chatAgencyRepository = chatAgencyRepository;
    this.logService = logService;
    this.consultantService = consultantService;
    this.userHelper = userHelper;
  }

  /**
   * Returns a list of current chats for the provided {@link Consultant}
   * 
   * @return list of chats as {@link ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> getChatsForConsultant(Consultant consultant) {

    List<ConsultantSessionResponseDTO> sessionResponseDTOs = null;
    List<Chat> chats = null;

    try {
      chats = chatRepository.findByAgencyIds(consultant.getConsultantAgencies().stream()
          .map(ConsultantAgency::getAgencyId).collect(Collectors.toSet()));

    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException(
          String.format("Database error while retrieving the chats for the consultant with id %s",
              consultant.getId()));
    }

    if (chats != null && chats.size() > 0) {
      sessionResponseDTOs =
          chats.stream().map(chat -> convertChatToConsultantSessionResponseDTO(chat))
              .collect(Collectors.toList());
    }

    return sessionResponseDTOs;
  }

  /**
   * Converts a {@link Chat} to a {@link ConsultantSessionResponseDTO}
   * 
   * @param {@link Chat}
   * @return {@link ConsultantSessionResponseDTO}
   */
  private ConsultantSessionResponseDTO convertChatToConsultantSessionResponseDTO(Chat chat) {
    return new ConsultantSessionResponseDTO(
        new UserChatDTO(chat.getId(), chat.getTopic(),
            LocalDate.of(chat.getStartDate().getYear(), chat.getStartDate().getMonth(),
                chat.getStartDate().getDayOfMonth()),
            LocalTime.of(chat.getStartDate().getHour(), chat.getStartDate().getMinute(),
                chat.getStartDate().getSecond()),
            chat.getDuration(), chat.isRepetitive(), chat.isActive(),
            chat.getConsultingType().getValue(), null, null, false, chat.getGroupId(), null, false,
            getChatModerators(chat.getChatAgencies()), chat.getStartDate()),
        new SessionConsultantForConsultantDTO(chat.getChatOwner().getId(),
            chat.getChatOwner().getFirstName(), chat.getChatOwner().getLastName()));
  }

  /**
   * Get an array with rc user ids of the moderators of a chat
   * 
   * @param chatAgencies
   * @return
   */
  private String[] getChatModerators(Set<ChatAgency> chatAgencies) {

    List<Consultant> consultantList = consultantService.findConsultantsByAgencyIds(chatAgencies);

    if (consultantList != null && consultantList.size() > 0) {
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
  public Chat saveChat(Chat chat) {
    try {
      return chatRepository.save(chat);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
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
  public ChatAgency saveChatAgencyRelation(ChatAgency chatAgency) {
    try {
      return chatAgencyRepository.save(chatAgency);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new SaveChatAgencyException(
          String.format("Creation of chat - user relation failed for: ", chatAgency.toString()),
          ex);
    }
  }

  /**
   * Returns the list of current chats for the provided user (Id).
   * 
   * @return list of user chats as {@link UserSessionResponseDTO}
   */
  public List<UserSessionResponseDTO> getChatsForUserId(String userId) {

    List<UserSessionResponseDTO> sessionResponseDTOs = null;
    List<Chat> chats = null;

    try {
      chats = chatRepository.findByUserId(userId);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException(String
          .format("Database error while retrieving the chats for the user with id %s", userId));
    }

    if (chats != null && chats.size() > 0) {
      sessionResponseDTOs = chats.stream().map(chat -> convertChatToUserSessionResponseDTO(chat))
          .collect(Collectors.toList());
    }

    return sessionResponseDTOs;
  }

  /**
   * Converts a {@link Chat} to a {@link UserSessionResponseDTO}
   * 
   * @param session
   * @return
   */
  private UserSessionResponseDTO convertChatToUserSessionResponseDTO(Chat chat) {
    return new UserSessionResponseDTO(null, new UserChatDTO(chat.getId(), chat.getTopic(),
        LocalDate.of(chat.getStartDate().getYear(), chat.getStartDate().getMonth(),
            chat.getStartDate().getDayOfMonth()),
        LocalTime.of(chat.getStartDate().getHour(), chat.getStartDate().getMinute(),
            chat.getStartDate().getSecond()),
        chat.getDuration(), chat.isRepetitive(), chat.isActive(),
        chat.getConsultingType().getValue(), null, null, false, chat.getGroupId(), null, false,
        getChatModerators(chat.getChatAgencies()), chat.getStartDate()), null, null, null);
  }

  public Optional<Chat> getChat(Long chatId) {
    Optional<Chat> chat;

    try {
      chat = chatRepository.findById(chatId);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException(
          String.format("Database error while retrieving chat with id %s", chatId));
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
      logService.logDatabaseError(ex);
      throw new ServiceException(String.format("Deletion of chat with id %s failed", chat.getId()));
    }
  }

  /**
   * Updates topic, duration, repetitive and start date of the provided {@link Chat}
   * 
   * @param chatId
   * @param chatDTO
   * @param authenticatedUser
   * @return
   * 
   * @throws {@link BadRequestException}, {@link ForbiddenException}, {@link ConflictException}
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
    if (chat.get().isActive()) {
      throw new ConflictException(String.format(
          "Chat with id %s is active. Therefore changing the chat settings is not supported.",
          chatId));
    }

    LocalDateTime startDate = LocalDateTime.of(chatDTO.getStartDate(), chatDTO.getStartTime());
    chat.get().setTopic(chatDTO.getTopic());
    chat.get().setDuration(chatDTO.getDuration());
    chat.get().setRepetitive(chatDTO.isRepetitive());
    chat.get().setChatInterval(chatDTO.isRepetitive() ? ChatInterval.WEEKLY : null);
    chat.get().setStartDate(startDate);
    chat.get().setInitialStartDate(startDate);

    this.saveChat(chat.get());

    return new UpdateChatResponseDTO(chat.get().getGroupId(),
        userHelper.generateChatUrl(chat.get().getId(), chat.get().getConsultingType()));
  }

}

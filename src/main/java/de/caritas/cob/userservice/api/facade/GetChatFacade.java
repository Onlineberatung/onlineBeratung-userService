package de.caritas.cob.userservice.api.facade;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.web.dto.ChatInfoResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade for capsuling to get a chat. */
@Service
@RequiredArgsConstructor
public class GetChatFacade {

  private final @NonNull ChatService chatService;
  private final @NonNull ChatPermissionVerifier chatPermissionVerifier;

  /**
   * Get chat info.
   *
   * @param chatId chat ID
   * @return {@link ChatInfoResponseDTO}
   */
  public ChatInfoResponseDTO getChat(Long chatId) {

    Chat chat =
        chatService
            .getChat(chatId)
            .orElseThrow(() -> new NotFoundException("Chat with id %s not found.", chatId));

    this.chatPermissionVerifier.verifyPermissionForChat(chat);

    return new ChatInfoResponseDTO()
        .id(chat.getId())
        .groupId(chat.getGroupId())
        .active(isTrue(chat.isActive()));
  }
}

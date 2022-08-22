package de.caritas.cob.userservice.api.workflow.deactivate.service;

import de.caritas.cob.userservice.api.actions.chat.StopChatActionCommand;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import java.time.LocalDateTime;
import java.util.function.Predicate;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service to trigger stopping of group chats. */
@Service
@RequiredArgsConstructor
public class DeactivateGroupChatService {

  private final @NonNull ChatRepository chatRepository;
  private final @NonNull ActionsRegistry actionsRegistry;

  @Value("${group.chat.deactivateworkflow.periodMinutes}")
  private long deactivatePeriodMinutes;

  /** Stops all still open group chats with special constraints. */
  @Transactional
  public void deactivateStaleGroupChats() {
    var deactivationTime = LocalDateTime.now().minusMinutes(deactivatePeriodMinutes);
    this.chatRepository.findAllByActiveIsTrue().stream()
        .filter(isChatOutsideOfDeactivationTime(deactivationTime))
        .forEach(this::deactivateStaleActiveChat);
  }

  private Predicate<Chat> isChatOutsideOfDeactivationTime(LocalDateTime deactivationTime) {
    return chat -> chat.getUpdateDate().isBefore(deactivationTime.minusMinutes(chat.getDuration()));
  }

  private void deactivateStaleActiveChat(Chat staleChat) {
    this.actionsRegistry
        .buildContainerForType(Chat.class)
        .addActionToExecute(StopChatActionCommand.class)
        .executeActions(staleChat);
  }
}

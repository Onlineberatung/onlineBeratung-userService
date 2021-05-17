package de.caritas.cob.userservice.api.deleteworkflow.action.consultant;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.THIRD;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ROCKET_CHAT;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chat.ChatRepository;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to delete chats owned by a {@link Consultant}.
 */
@Component
@RequiredArgsConstructor
public class DeleteChatAction implements DeleteConsultantAction {

  private final @NonNull ChatRepository chatRepository;
  private final @NonNull RocketChatService rocketChatService;

  /**
   * Deletes all chats in database and Rocket.Chat owned by given {@link Consultant}.
   *
   * @param consultant the {@link Consultant}
   * @return possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(Consultant consultant) {
    List<Chat> chatsByChatOwner = this.chatRepository.findByChatOwner(consultant);

    List<DeletionWorkflowError> workflowErrors = chatsByChatOwner.stream()
        .map(Chat::getGroupId)
        .map(this::deleteRocketChatRoom)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    deleteDatabaseChat(chatsByChatOwner, workflowErrors);

    return workflowErrors;
  }

  private List<DeletionWorkflowError> deleteRocketChatRoom(String rcGroupId) {
    try {
      this.rocketChatService.deleteGroupAsTechnicalUser(rcGroupId);
    } catch (RocketChatDeleteGroupException e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(ROCKET_CHAT)
              .identifier(rcGroupId)
              .reason("Deletion of Rocket.Chat group failed")
              .timestamp(nowInUtc())
              .build()
      );
    }
    return emptyList();
  }

  private void deleteDatabaseChat(List<Chat> chatsByChatOwner,
      List<DeletionWorkflowError> workflowErrors) {
    if (isNotEmpty(chatsByChatOwner)) {
      try {
        this.chatRepository.deleteAll(chatsByChatOwner);
      } catch (Exception e) {
        LogService.logDeleteWorkflowError(e);
        workflowErrors.add(
            DeletionWorkflowError.builder()
                .deletionSourceType(CONSULTANT)
                .deletionTargetType(DATABASE)
                .identifier(chatsByChatOwner.iterator().next().getChatOwner().getId())
                .reason("Unable to delete chats in database")
                .timestamp(nowInUtc())
                .build()
        );
      }
    }
  }

  @Override
  public int getOrder() {
    return THIRD.getOrder();
  }
}

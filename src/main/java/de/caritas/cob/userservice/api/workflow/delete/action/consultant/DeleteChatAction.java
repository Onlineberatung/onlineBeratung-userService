package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Action to delete chats owned by a {@link Consultant}. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteChatAction implements ActionCommand<ConsultantDeletionWorkflowDTO> {

  private final @NonNull ChatRepository chatRepository;
  private final @NonNull RocketChatService rocketChatService;

  /**
   * Deletes all chats in database and Rocket.Chat owned by given {@link Consultant}.
   *
   * @param actionTarget the {@link ConsultantDeletionWorkflowDTO} containing the {@link Consultant}
   */
  @Override
  public void execute(ConsultantDeletionWorkflowDTO actionTarget) {
    var chatsByChatOwner = this.chatRepository.findByChatOwner(actionTarget.getConsultant());

    var workflowErrors =
        chatsByChatOwner.stream()
            .map(Chat::getGroupId)
            .map(this::deleteRocketChatRoom)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    deleteDatabaseChat(chatsByChatOwner, workflowErrors);
    actionTarget.getDeletionWorkflowErrors().addAll(workflowErrors);
  }

  private List<DeletionWorkflowError> deleteRocketChatRoom(String rcGroupId) {
    try {
      this.rocketChatService.deleteGroupAsTechnicalUser(rcGroupId);
    } catch (RocketChatDeleteGroupException e) {
      log.error("UserService delete workflow error: ", e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(DeletionTargetType.ROCKET_CHAT)
              .identifier(rcGroupId)
              .reason("Deletion of Rocket.Chat group failed")
              .timestamp(nowInUtc())
              .build());
    }
    return emptyList();
  }

  private void deleteDatabaseChat(
      List<Chat> chatsByChatOwner, List<DeletionWorkflowError> workflowErrors) {
    if (isNotEmpty(chatsByChatOwner)) {
      try {
        this.chatRepository.deleteAll(chatsByChatOwner);
      } catch (Exception e) {
        log.error("UserService delete workflow error: ", e);
        workflowErrors.add(
            DeletionWorkflowError.builder()
                .deletionSourceType(CONSULTANT)
                .deletionTargetType(DeletionTargetType.DATABASE)
                .identifier(chatsByChatOwner.iterator().next().getChatOwner().getId())
                .reason("Unable to delete chats in database")
                .timestamp(nowInUtc())
                .build());
      }
    }
  }
}

package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.ROCKET_CHAT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class DeleteChatActionTest {

  @InjectMocks private DeleteChatAction deleteChatAction;

  @Mock private ChatRepository chatRepository;

  @Mock private RocketChatService rocketChatService;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(DeleteChatAction.class, "log", logger);
  }

  @Test
  public void execute_Should_returnEmptyListAndPerformNoDeletion_When_consultantIsNoChatOwner() {
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(new Consultant(), emptyList());

    this.deleteChatAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verifyNoMoreInteractions(this.logger);
    verifyNoMoreInteractions(this.rocketChatService);
    verify(this.chatRepository, times(1)).findByChatOwner(any());
    verifyNoMoreInteractions(this.chatRepository);
  }

  @Test
  public void execute_Should_returnEmptyListAndPerformDeletion_When_consultantIsChatOwner()
      throws RocketChatDeleteGroupException {
    List<Chat> chats = new EasyRandom().objects(Chat.class, 5).collect(Collectors.toList());
    when(this.chatRepository.findByChatOwner(any())).thenReturn(chats);
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(new Consultant(), emptyList());

    this.deleteChatAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verifyNoMoreInteractions(this.logger);
    verify(this.rocketChatService, times(5)).deleteGroupAsTechnicalUser(any());
    verify(this.chatRepository, times(1)).findByChatOwner(any());
    verify(this.chatRepository, times(1)).deleteAll(chats);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorsAndLogErrors_When_deletionOfChatsFailes()
      throws RocketChatDeleteGroupException {
    List<Chat> chats = new EasyRandom().objects(Chat.class, 5).collect(Collectors.toList());
    when(this.chatRepository.findByChatOwner(any())).thenReturn(chats);
    doThrow(new RocketChatDeleteGroupException(new RuntimeException()))
        .when(this.rocketChatService)
        .deleteGroupAsTechnicalUser(any());
    doThrow(new RuntimeException()).when(this.chatRepository).deleteAll(any());
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(new Consultant(), new ArrayList<>());

    this.deleteChatAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(6));
    verify(logger, times(6)).error(anyString(), any(Exception.class));
  }

  @Test
  public void
      execute_Should_returnExpectedWorkflowErrorsAndLogErrors_When_deletionOfSingleChatFailes()
          throws RocketChatDeleteGroupException {
    Chat chat = new EasyRandom().nextObject(Chat.class);
    when(this.chatRepository.findByChatOwner(any())).thenReturn(singletonList(chat));
    doThrow(new RocketChatDeleteGroupException(new RuntimeException()))
        .when(this.rocketChatService)
        .deleteGroupAsTechnicalUser(any());
    doThrow(new RuntimeException()).when(this.chatRepository).deleteAll(any());
    ConsultantDeletionWorkflowDTO workflowDTO =
        new ConsultantDeletionWorkflowDTO(new Consultant(), new ArrayList<>());

    this.deleteChatAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(2));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(CONSULTANT));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(ROCKET_CHAT));
    assertThat(workflowErrors.get(0).getIdentifier(), is(chat.getGroupId()));
    assertThat(workflowErrors.get(0).getReason(), is("Deletion of Rocket.Chat group failed"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    assertThat(workflowErrors.get(1).getDeletionSourceType(), is(CONSULTANT));
    assertThat(workflowErrors.get(1).getDeletionTargetType(), is(DATABASE));
    assertThat(workflowErrors.get(1).getIdentifier(), is(chat.getChatOwner().getId()));
    assertThat(workflowErrors.get(1).getReason(), is("Unable to delete chats in database"));
    assertThat(workflowErrors.get(1).getTimestamp(), notNullValue());
    verify(logger).error(anyString(), any(RuntimeException.class));
  }
}

package de.caritas.cob.userservice.api.workflow.deactivate.service;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.actions.ActionCommandMockProvider;
import de.caritas.cob.userservice.api.actions.chat.StopChatActionCommand;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeactivateGroupChatServiceTest {

  private static final int DEACTIVATE_PERIOD_MINUTES = 180;

  @InjectMocks private DeactivateGroupChatService deactivateGroupChatService;

  @Mock private ChatRepository chatRepository;

  @Mock private ActionsRegistry actionsRegistry;

  private final ActionCommandMockProvider commandMockProvider = new ActionCommandMockProvider();

  @BeforeEach
  public void setUp() {
    setField(deactivateGroupChatService, "deactivatePeriodMinutes", DEACTIVATE_PERIOD_MINUTES);
  }

  @Test
  void deactivateStaleGroupChats_Should_notUseServices_When_noChatIsAvailable() {
    this.deactivateGroupChatService.deactivateStaleGroupChats();

    verifyNoMoreInteractions(this.actionsRegistry);
  }

  @Test
  void deactivateStaleGroupChats_Should_notPerformAnyDeactivation_When_noChatIsActive() {
    when(this.chatRepository.findAllByActiveIsTrue()).thenReturn(emptyList());

    this.deactivateGroupChatService.deactivateStaleGroupChats();

    verifyNoMoreInteractions(this.actionsRegistry);
  }

  @ParameterizedTest
  @MethodSource("createUpdateDatesWithinDeactivationPeriod")
  void
      deactivateStaleGroupChats_Should_notPerformAnyDeactivation_When_chatsAreActiveWithinDeactivatePeriod(
          LocalDateTime updateDate) {
    var chat = new Chat();
    chat.setDuration(120);
    chat.setActive(true);
    chat.setUpdateDate(updateDate);
    when(this.chatRepository.findAllByActiveIsTrue()).thenReturn(List.of(chat));

    this.deactivateGroupChatService.deactivateStaleGroupChats();

    verifyNoMoreInteractions(
        this.actionsRegistry, this.commandMockProvider.getActionMock(StopChatActionCommand.class));
  }

  private static List<LocalDateTime> createUpdateDatesWithinDeactivationPeriod() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneSecondWithinDeletionPeriod =
        now.minusMinutes(DEACTIVATE_PERIOD_MINUTES).plusSeconds(10);
    LocalDateTime timeInTheFuture = now.plusSeconds(20);

    return List.of(now, oneSecondWithinDeletionPeriod, timeInTheFuture);
  }

  @ParameterizedTest
  @MethodSource("createOverdueUpdateDates")
  void deactivateStaleGroupChats_Should_callStopChatAction_When_chatIsActiveTooLong(
      LocalDateTime overdueUpdateDate) {
    var chat = new Chat();
    chat.setDuration(120);
    chat.setActive(true);
    chat.setUpdateDate(overdueUpdateDate);
    when(this.chatRepository.findAllByActiveIsTrue()).thenReturn(List.of(chat));
    when(this.actionsRegistry.buildContainerForType(Chat.class))
        .thenReturn(commandMockProvider.getActionContainer(Chat.class));

    this.deactivateGroupChatService.deactivateStaleGroupChats();

    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(Chat.class);
    verify(this.commandMockProvider.getActionMock(StopChatActionCommand.class), times(1))
        .execute(chat);
  }

  private static List<LocalDateTime> createOverdueUpdateDates() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneDeletionPeriodAgo =
        now.minusMinutes(DEACTIVATE_PERIOD_MINUTES).minusMinutes(120);
    LocalDateTime timeLongInThePast = oneDeletionPeriodAgo.minusMinutes(10);

    return List.of(oneDeletionPeriodAgo, timeLongInThePast);
  }
}

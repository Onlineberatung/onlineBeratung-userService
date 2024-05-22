package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.ActionCommandMockProvider;
import de.caritas.cob.userservice.api.actions.chat.StopChatActionCommand;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.model.Chat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StopChatFacadeTest {

  @InjectMocks private StopChatFacade stopChatFacade;

  @Mock private ChatPermissionVerifier chatPermissionVerifier;

  @Mock private ActionsRegistry actionsRegistry;

  private final ActionCommandMockProvider mockProvider = new ActionCommandMockProvider();

  @BeforeEach
  void setup() {
    when(this.actionsRegistry.buildContainerForType(Chat.class))
        .thenReturn(mockProvider.getActionContainer(Chat.class));
  }

  @Test
  void stopChat_Should_ThrowRequestForbiddenException_When_ConsultantHasNoPermissionToStopChat() {
    when(chatPermissionVerifier.hasSameAgencyAssigned(ACTIVE_CHAT, CONSULTANT)).thenReturn(false);

    try {
      stopChatFacade.stopChat(ACTIVE_CHAT, CONSULTANT);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException sequestForbiddenException) {
      assertTrue(true, "Excepted RequestForbiddenException thrown");
    }
  }

  @Test
  void stopChat_Should_executeExpectedAction_When_ConsultantHasPermissionToStopChat() {
    when(chatPermissionVerifier.hasSameAgencyAssigned(ACTIVE_CHAT, CONSULTANT)).thenReturn(true);

    stopChatFacade.stopChat(ACTIVE_CHAT, CONSULTANT);

    verify(this.mockProvider.getActionMock(StopChatActionCommand.class), times(1))
        .execute(ACTIVE_CHAT);
  }
}

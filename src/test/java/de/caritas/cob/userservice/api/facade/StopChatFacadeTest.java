package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.ActionCommandMockProvider;
import de.caritas.cob.userservice.api.actions.chat.StopChatActionCommand;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.model.Chat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StopChatFacadeTest {

  @InjectMocks private StopChatFacade stopChatFacade;

  @Mock private ChatPermissionVerifier chatPermissionVerifier;

  @Mock private ActionsRegistry actionsRegistry;

  private final ActionCommandMockProvider mockProvider = new ActionCommandMockProvider();

  @Before
  public void setup() {
    when(this.actionsRegistry.buildContainerForType(Chat.class))
        .thenReturn(mockProvider.getActionContainer(Chat.class));
  }

  @Test
  public void
      stopChat_Should_ThrowRequestForbiddenException_When_ConsultantHasNoPermissionToStopChat() {
    when(chatPermissionVerifier.hasSameAgencyAssigned(ACTIVE_CHAT, CONSULTANT)).thenReturn(false);

    try {
      stopChatFacade.stopChat(ACTIVE_CHAT, CONSULTANT);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException sequestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }
  }

  @Test
  public void stopChat_Should_executeExpectedAction_When_ConsultantHasPermissionToStopChat() {
    when(chatPermissionVerifier.hasSameAgencyAssigned(ACTIVE_CHAT, CONSULTANT)).thenReturn(true);

    stopChatFacade.stopChat(ACTIVE_CHAT, CONSULTANT);

    verify(this.mockProvider.getActionMock(StopChatActionCommand.class), times(1))
        .execute(ACTIVE_CHAT);
  }
}

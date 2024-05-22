package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.INACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StartChatFacadeTest {

  @InjectMocks private StartChatFacade startChatFacade;

  @Mock private ChatPermissionVerifier chatPermissionVerifier;

  @Mock private RocketChatService rocketChatService;

  @Mock private ChatService chatService;

  @Mock private Chat chat;

  @Test
  public void
      startChat_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {
    when(chatPermissionVerifier.hasSameAgencyAssigned(ACTIVE_CHAT, CONSULTANT)).thenReturn(false);

    try {
      startChatFacade.startChat(ACTIVE_CHAT, CONSULTANT);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException sequestForbiddenException) {
      assertTrue(true, "Excepted RequestForbiddenException thrown");
    }
  }

  @Test
  public void startChat_Should_ThrowConflictException_WhenChatIsAlreadyStarted() {
    when(chatPermissionVerifier.hasSameAgencyAssigned(ACTIVE_CHAT, CONSULTANT)).thenReturn(true);

    try {
      startChatFacade.startChat(ACTIVE_CHAT, CONSULTANT);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue(true, "Excepted ConflictException thrown");
    }
  }

  @Test
  public void startChat_Should_ThrowInternalServerError_WhenChatHasNoGroupId() {
    when(chat.isActive()).thenReturn(false);
    when(chat.getGroupId()).thenReturn(null);

    when(chatPermissionVerifier.hasSameAgencyAssigned(chat, CONSULTANT)).thenReturn(true);

    try {
      startChatFacade.startChat(chat, CONSULTANT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  public void startChat_Should_AddConsultantToRocketChatGroup()
      throws RocketChatAddUserToGroupException {
    when(chatPermissionVerifier.hasSameAgencyAssigned(INACTIVE_CHAT, CONSULTANT)).thenReturn(true);

    startChatFacade.startChat(INACTIVE_CHAT, CONSULTANT);

    verify(rocketChatService, times(1))
        .addUserToGroup(CONSULTANT.getRocketChatId(), INACTIVE_CHAT.getGroupId());
  }

  @Test
  public void startChat_Should_SetChatActiveAndSaveChat() {
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(chatPermissionVerifier.hasSameAgencyAssigned(chat, CONSULTANT)).thenReturn(true);

    startChatFacade.startChat(chat, CONSULTANT);

    verify(chat, times(1)).setActive(true);
    verify(chatService, times(1)).saveChat(chat);
  }

  @Test
  public void
      startChat_Should_throwInternalServerErrorException_When_userCanNotBeAddedToGroupInRocketChat()
          throws RocketChatAddUserToGroupException {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(chat.getGroupId()).thenReturn(RC_GROUP_ID);
          when(chatPermissionVerifier.hasSameAgencyAssigned(chat, CONSULTANT)).thenReturn(true);
          doThrow(new RocketChatAddUserToGroupException(""))
              .when(rocketChatService)
              .addUserToGroup(any(), any());

          startChatFacade.startChat(chat, CONSULTANT);

          verify(chat, times(1)).setActive(true);
          verify(chatService, times(1)).saveChat(chat);
        });
  }
}

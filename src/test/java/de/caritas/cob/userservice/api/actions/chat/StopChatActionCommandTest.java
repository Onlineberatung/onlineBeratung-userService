package de.caritas.cob.userservice.api.actions.chat;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_VALUE_WEEKLY_PLUS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_INTERVAL_WEEKLY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_START_DATETIME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.IS_REPETITIVE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StopChatActionCommandTest {

  @InjectMocks
  private StopChatActionCommand stopChatActionCommand;

  @Mock
  private ChatService chatService;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private Chat chat;

  @Test
  void stopChat_Should_ThrowConflictException_When_ChatIsAlreadyStopped() {
    when(chat.isActive()).thenReturn(false);

    try {
      stopChatActionCommand.execute(chat);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue(true, "Excepted ConflictException thrown");
    }
  }

  @Test
  void stopChat_Should_ThrowInternalServerError_When_ChatHasNoGroupId() {
    when(chat.isActive()).thenReturn(true);
    when(chat.getGroupId()).thenReturn(null);

    try {
      stopChatActionCommand.execute(chat);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }
  }

  @Test
  void stopChat_Should_ThrowInternalServerError_When_RemoveAllMessagesFails()
      throws RocketChatRemoveSystemMessagesException {
    doThrow(new RocketChatRemoveSystemMessagesException("error")).when(rocketChatService)
        .removeAllMessages(Mockito.any());

    try {
      stopChatActionCommand.execute(ACTIVE_CHAT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }

    verify(rocketChatService, times(1)).removeAllMessages(ACTIVE_CHAT.getGroupId());
  }

  @Test
  void stopChat_Should_ThrowInternalServerError_When_DeleteRcGroupFails()
      throws RocketChatRemoveSystemMessagesException {
    when(chat.isActive()).thenReturn(true);
    when(chat.isRepetitive()).thenReturn(false);
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(rocketChatService.deleteGroupAsSystemUser(chat.getGroupId())).thenReturn(false);

    try {
      stopChatActionCommand.execute(chat);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }

    verify(rocketChatService, times(0)).removeAllMessages(Mockito.any());
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(Mockito.any());
  }

  @Test
  void stopChat_Should_ThrowInternalServerError_When_ChatIntervallIsNullOnRepetitiveChats() {
    when(chat.isActive()).thenReturn(true);
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(chat.isRepetitive()).thenReturn(true);
    when(chat.getChatInterval()).thenReturn(null);

    try {
      stopChatActionCommand.execute(chat);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue(true, "Excepted InternalServerErrorException thrown");
    }

    verify(rocketChatService, times(0)).deleteGroupAsSystemUser(Mockito.any());
  }

  @Test
  void stopChat_Should_RemoveAllMessagesAndUsersAndSetStatusAndStartDateOfChat_When_ChatIsRepetitive()
      throws Exception {
    when(chat.isActive()).thenReturn(true);
    when(chat.isRepetitive()).thenReturn(true);
    when(chat.getChatInterval()).thenReturn(CHAT_INTERVAL_WEEKLY);
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(chat.getStartDate()).thenReturn(nowInUtc());

    stopChatActionCommand.execute(chat);

    verify(rocketChatService, times(1)).removeAllMessages(chat.getGroupId());
    verify(rocketChatService, times(1)).removeAllStandardUsersFromGroup(chat.getGroupId());
    verify(chat, times(1)).setStartDate(Mockito.any());
    verify(chat, times(1)).setActive(false);
    verify(chatService, times(1)).saveChat(chat);
  }

  @Test
  void stopChat_Should_ReturnCorrectNextStartDate_When_ChatIsRepetitive() {
    Chat chatWithDate = new Chat("topic", 15, CHAT_START_DATETIME, CHAT_START_DATETIME,
        1, IS_REPETITIVE, CHAT_INTERVAL_WEEKLY, CONSULTANT);
    chatWithDate.setActive(true);
    chatWithDate.setGroupId("groupId");

    stopChatActionCommand.execute(chatWithDate);

    assertEquals(CHAT_START_DATETIME.plusWeeks(FIELD_VALUE_WEEKLY_PLUS),
        chatWithDate.getStartDate());
  }

  @Test
  void stopChat_Should_throwInternalServerErrorException_When_ChatResetCanNotBePerformedOnRocketChat()
      throws RocketChatUserNotInitializedException, RocketChatGetGroupMembersException, RocketChatRemoveUserFromGroupException {
    Chat chatWithDate = new Chat("topic", 15, CHAT_START_DATETIME, CHAT_START_DATETIME,
        1, IS_REPETITIVE, CHAT_INTERVAL_WEEKLY, CONSULTANT);
    chatWithDate.setActive(true);
    chatWithDate.setGroupId("groupId");
    doThrow(new RocketChatRemoveUserFromGroupException("")).when(rocketChatService)
        .removeAllStandardUsersFromGroup(any());

    assertThrows(InternalServerErrorException.class,
        () -> stopChatActionCommand.execute(chatWithDate));
  }

  @Test
  void stopChat_Should_RemoveRocketChatGroupAndChatFromDb_When_ChatIsNotRepetitive() {
    when(chat.isActive()).thenReturn(true);
    when(chat.isRepetitive()).thenReturn(false);
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);

    when(rocketChatService.deleteGroupAsSystemUser(RC_GROUP_ID)).thenReturn(true);

    stopChatActionCommand.execute(chat);

    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(chat.getGroupId());
    verify(chatService, times(1)).deleteChat(chat);
  }

}

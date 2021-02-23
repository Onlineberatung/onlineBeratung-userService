package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.repository.session.ConsultingType.KREUZBUND;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_WEEKLY_PLUS;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_VALUE_WEEKLY_PLUS;
import static de.caritas.cob.userservice.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_INTERVAL_WEEKLY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_START_DATETIME;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_REPETITIVE;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StopChatFacadeTest {

  @InjectMocks
  private StopChatFacade stopChatFacade;
  @Mock
  private ChatService chatService;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private ChatHelper chatHelper;
  @Mock
  private Chat chat;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(stopChatFacade,
        stopChatFacade.getClass().getDeclaredField(FIELD_NAME_WEEKLY_PLUS),
        FIELD_VALUE_WEEKLY_PLUS);
  }

  /**
   * Method: startChat
   */
  @Test
  public void stopChat_Should_ThrowRequestForbiddenException_When_ConsultantHasNoPermissionToStopChat() {

    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(false);

    try {
      stopChatFacade.stopChat(ACTIVE_CHAT, CONSULTANT);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException sequestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }
  }

  @Test
  public void stopChat_Should_ThrowConflictException_When_ChatIsAlreadyStopped() {

    Chat inactiveChat = Mockito.mock(Chat.class);

    when(chatHelper.isChatAgenciesContainConsultantAgency(inactiveChat, CONSULTANT))
        .thenReturn(true);
    when(inactiveChat.isActive()).thenReturn(false);

    try {
      stopChatFacade.stopChat(inactiveChat, CONSULTANT);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue("Excepted ConflictException thrown", true);
    }
  }

  @Test
  public void stopChat_Should_ThrowInternalServerError_When_ChatHasNoGroupId() {

    when(chatHelper.isChatAgenciesContainConsultantAgency(chat, CONSULTANT)).thenReturn(true);
    when(chat.isActive()).thenReturn(true);
    when(chat.getGroupId()).thenReturn(null);

    try {
      stopChatFacade.stopChat(chat, CONSULTANT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void stopChat_Should_ThrowInternalServerError_When_RemoveAllMessagesFails()
      throws RocketChatRemoveSystemMessagesException {

    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(true);
    doThrow(new RocketChatRemoveSystemMessagesException("error")).when(rocketChatService)
        .removeAllMessages(Mockito.any());

    try {
      stopChatFacade.stopChat(ACTIVE_CHAT, CONSULTANT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

    verify(rocketChatService, times(1)).removeAllMessages(ACTIVE_CHAT.getGroupId());
  }

  @Test
  public void stopChat_Should_ThrowInternalServerError_When_DeleteRcGroupFails()
      throws RocketChatRemoveSystemMessagesException {

    when(chatHelper.isChatAgenciesContainConsultantAgency(chat, CONSULTANT)).thenReturn(true);
    when(chat.isActive()).thenReturn(true);
    when(chat.isRepetitive()).thenReturn(false);
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(rocketChatService.deleteGroupAsSystemUser(chat.getGroupId())).thenReturn(false);

    try {
      stopChatFacade.stopChat(chat, CONSULTANT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

    verify(rocketChatService, times(0)).removeAllMessages(Mockito.any());
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(Mockito.any());
  }

  @Test
  public void stopChat_Should_ThrowInternalServerError_When_ChatIntervallIsNullOnRepetitiveChats() {

    when(chatHelper.isChatAgenciesContainConsultantAgency(chat, CONSULTANT)).thenReturn(true);
    when(chat.isActive()).thenReturn(true);
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(chat.isRepetitive()).thenReturn(true);
    when(chat.getChatInterval()).thenReturn(null);

    try {
      stopChatFacade.stopChat(chat, CONSULTANT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

    verify(rocketChatService, times(0)).deleteGroupAsSystemUser(Mockito.any());
  }

  @Test
  public void stopChat_Should_RemoveAllMessagesAndUsersAndSetStatusAndStartDateOfChat_When_ChatIsRepetitive()
      throws Exception {

    when(chatHelper.isChatAgenciesContainConsultantAgency(chat, CONSULTANT)).thenReturn(true);
    when(chat.isActive()).thenReturn(true);
    when(chat.isRepetitive()).thenReturn(true);
    when(chat.getChatInterval()).thenReturn(CHAT_INTERVAL_WEEKLY);
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(chat.getStartDate()).thenReturn(nowInUtc());

    stopChatFacade.stopChat(chat, CONSULTANT);

    verify(rocketChatService, times(1)).removeAllMessages(chat.getGroupId());
    verify(rocketChatService, times(1)).removeAllStandardUsersFromGroup(chat.getGroupId());
    verify(chat, times(1)).setStartDate(Mockito.any());
    verify(chat, times(1)).setActive(false);
    verify(chatService, times(1)).saveChat(chat);
  }

  @Test
  public void stopChat_Should_ReturnCorrectNextStartDate_When_ChatIsRepetitive() {
    Chat chatWithDate = new Chat("topic", KREUZBUND, CHAT_START_DATETIME, CHAT_START_DATETIME,
        1, IS_REPETITIVE, CHAT_INTERVAL_WEEKLY, CONSULTANT);
    chatWithDate.setActive(true);
    chatWithDate.setGroupId("groupId");

    when(chatHelper.isChatAgenciesContainConsultantAgency(chatWithDate, CONSULTANT))
        .thenReturn(true);

    stopChatFacade.stopChat(chatWithDate, CONSULTANT);

    assertEquals(CHAT_START_DATETIME.plusWeeks(FIELD_VALUE_WEEKLY_PLUS),
        chatWithDate.getStartDate());
  }

  @Test
  public void stopChat_Should_RemoveRocketChatGroupAndChatFromDb_When_ChatIsNotRepetitive() {

    when(chatHelper.isChatAgenciesContainConsultantAgency(chat, CONSULTANT)).thenReturn(true);
    when(chat.isActive()).thenReturn(true);
    when(chat.isRepetitive()).thenReturn(false);
    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);

    when(rocketChatService.deleteGroupAsSystemUser(RC_GROUP_ID)).thenReturn(true);

    stopChatFacade.stopChat(chat, CONSULTANT);

    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(chat.getGroupId());
    verify(chatService, times(1)).deleteChat(chat);
  }
}

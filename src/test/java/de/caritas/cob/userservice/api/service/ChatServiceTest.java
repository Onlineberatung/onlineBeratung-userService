package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.AUTHENTICATED_USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.AUTHENTICATED_USER_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.AUTHENTICATED_USER_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.INACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.UpdateChatResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chat.ChatRepository;
import de.caritas.cob.userservice.api.repository.chatagency.ChatAgency;
import de.caritas.cob.userservice.api.repository.chatagency.ChatAgencyRepository;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ChatServiceTest {

  @InjectMocks
  private ChatService chatService;

  @Mock
  private ChatRepository chatRepository;

  @Mock
  private ChatAgencyRepository chatAgencyRepository;

  @Mock
  private Logger logger;

  @Mock
  private UserHelper userHelper;

  @Mock
  private ConsultantService consultantService;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void getChatsForUserId_Should_ReturnListOfUserSessionResponseDTOWithChats() {
    when(chatRepository.findByUserId(USER_ID)).thenReturn(singletonList(ACTIVE_CHAT));
    when(consultantService.findConsultantsByAgencyIds(Mockito.any()))
        .thenReturn(singletonList(CONSULTANT));

    List<UserSessionResponseDTO> resultList = chatService.getChatsForUserId(USER_ID);

    assertNull(resultList.get(0).getSession());
    assertNotNull(resultList.get(0).getChat());
    assertEquals(ACTIVE_CHAT.getId(), resultList.get(0).getChat().getId());
    assertEquals(ACTIVE_CHAT.getTopic(), resultList.get(0).getChat().getTopic());
    assertThat(ACTIVE_CHAT.getConsultingTypeId(),
        is(resultList.get(0).getChat().getConsultingType()));
    assertEquals(
        LocalDate.of(ACTIVE_CHAT.getStartDate().getYear(), ACTIVE_CHAT.getStartDate().getMonth(),
            ACTIVE_CHAT.getStartDate().getDayOfMonth()),
        resultList.get(0).getChat().getStartDate());
    assertEquals(
        LocalTime.of(ACTIVE_CHAT.getInitialStartDate().getHour(),
            ACTIVE_CHAT.getInitialStartDate().getMinute()),
        resultList.get(0).getChat().getStartTime());
    assertEquals(ACTIVE_CHAT.getDuration(), resultList.get(0).getChat().getDuration());
    assertEquals(ACTIVE_CHAT.isRepetitive(), resultList.get(0).getChat().isRepetitive());
    assertEquals(ACTIVE_CHAT.isActive(), resultList.get(0).getChat().isActive());
    assertEquals(ACTIVE_CHAT.getGroupId(), resultList.get(0).getChat().getGroupId());
    assertNotNull(resultList.get(0).getChat().getModerators());
    assertEquals(1, resultList.get(0).getChat().getModerators().length);
    assertEquals(CONSULTANT.getRocketChatId(), resultList.get(0).getChat().getModerators()[0]);
  }

  @Test
  public void getChatsForConsultant_Should_ReturnListOfConsultantSessionResponseDTOWithChats() {
    Consultant consultant = Mockito.mock(Consultant.class);

    when(chatRepository.findByAgencyIds(Mockito.any())).thenReturn(singletonList(ACTIVE_CHAT));
    when(consultantService.findConsultantsByAgencyIds(Mockito.any()))
        .thenReturn(singletonList(CONSULTANT));

    List<ConsultantSessionResponseDTO> resultList = chatService.getChatsForConsultant(consultant);

    assertNull(resultList.get(0).getSession());
    assertNotNull(resultList.get(0).getChat());
    assertEquals(ACTIVE_CHAT.getId(), resultList.get(0).getChat().getId());
    assertEquals(ACTIVE_CHAT.getTopic(), resultList.get(0).getChat().getTopic());
    assertThat(ACTIVE_CHAT.getConsultingTypeId(),
        is(resultList.get(0).getChat().getConsultingType()));
    assertEquals(
        LocalDate.of(ACTIVE_CHAT.getStartDate().getYear(), ACTIVE_CHAT.getStartDate().getMonth(),
            ACTIVE_CHAT.getStartDate().getDayOfMonth()),
        resultList.get(0).getChat().getStartDate());
    assertEquals(
        LocalTime.of(ACTIVE_CHAT.getInitialStartDate().getHour(),
            ACTIVE_CHAT.getInitialStartDate().getMinute()),
        resultList.get(0).getChat().getStartTime());
    assertEquals(ACTIVE_CHAT.getDuration(), resultList.get(0).getChat().getDuration());
    assertEquals(ACTIVE_CHAT.isRepetitive(), resultList.get(0).getChat().isRepetitive());
    assertEquals(ACTIVE_CHAT.isActive(), resultList.get(0).getChat().isActive());
    assertEquals(ACTIVE_CHAT.getGroupId(), resultList.get(0).getChat().getGroupId());
    assertNotNull(resultList.get(0).getChat().getModerators());
    assertEquals(1, resultList.get(0).getChat().getModerators().length);
    assertEquals(CONSULTANT.getRocketChatId(), resultList.get(0).getChat().getModerators()[0]);
  }

  @Test
  public void getChatsForConsultant_Should_ReturnEmptyListWhenListOfChatsIsEmpty() {
    Consultant consultant = Mockito.mock(Consultant.class);

    List<ConsultantSessionResponseDTO> resultList = chatService.getChatsForConsultant(consultant);

    assertThat(resultList, hasSize(0));
  }

  @Test
  public void getChat_Should_ReturnChatObject() {
    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));

    Optional<Chat> result = chatService.getChat(CHAT_ID);

    assertThat(result, instanceOf(Optional.class));
    assertTrue(result.isPresent());
    assertThat(result.get(), instanceOf(Chat.class));
  }

  @Test
  public void updateChat_Should_ThrowBadRequestException_WhenChatDoesNotExist() {
    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  @Test
  public void updateChat_Should_ThrowForbiddenException_WhenCallingConsultantNotOwnerOfChat() {
    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(INACTIVE_CHAT));

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_3);
      fail("Expected exception: ForbiddenException");
    } catch (ForbiddenException forbiddenException) {
      assertTrue("Excepted ForbiddenException thrown", true);
    }
  }

  @Test
  public void updateChat_Should_ThrowConflictException_WhenChatIsActive() {
    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_CONSULTANT);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue("Excepted ConflictException thrown", true);
    }
  }

  @Test
  public void updateChat_Should_SaveNewChatSettings() {
    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);
    when(inactiveChat.getChatOwner()).thenReturn(CONSULTANT);

    when(chatRepository.findById(Mockito.any())).thenReturn(Optional.of(inactiveChat));

    chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_CONSULTANT);

    verify(chatRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void updateChat_Should_ReturnCorrectGroupIdAndChatLinkObject() {
    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);
    when(inactiveChat.getChatOwner()).thenReturn(CONSULTANT);
    when(inactiveChat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(inactiveChat.getConsultingTypeId()).thenReturn(15);

    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(inactiveChat));

    UpdateChatResponseDTO result =
        chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_CONSULTANT);
    String chatLink =
        userHelper.generateChatUrl(inactiveChat.getId(), inactiveChat.getConsultingTypeId());

    assertEquals(result.getGroupId(), inactiveChat.getGroupId());
    assertEquals(result.getChatLink(), chatLink);
  }

  @Test
  public void saveChatAgencyRelation_Should_saveChatAgencyInRepository() {
    ChatAgency chatAgency = new ChatAgency();

    this.chatService.saveChatAgencyRelation(chatAgency);

    verify(this.chatAgencyRepository, times(1)).save(chatAgency);
  }

  @Test
  public void deleteChat_Should_deleteChatInRepository() {
    Chat chat = new Chat();

    this.chatService.deleteChat(chat);

    verify(this.chatRepository, times(1)).delete(chat);
  }

}

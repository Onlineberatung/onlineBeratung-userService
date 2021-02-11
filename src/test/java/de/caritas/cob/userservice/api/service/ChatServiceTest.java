package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.AUTHENTICATED_USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.AUTHENTICATED_USER_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.AUTHENTICATED_USER_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.INACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
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

import de.caritas.cob.userservice.api.exception.SaveChatAgencyException;
import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.UpdateChatResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chat.ChatRepository;
import de.caritas.cob.userservice.api.repository.chatagency.ChatAgencyRepository;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.dao.DataAccessException;

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

  /**
   * Method getChatsForUserId
   */

  @Test
  public void getChatsForUserId_Should_ThrowInternalServerErrorExceptionOnDatabaseError() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("Database error") {};
    when(chatRepository.findByUserId(USER_ID)).thenThrow(ex);

    try {
      chatService.getChatsForUserId(USER_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getChatsForUserId_Should_ReturnListOfUserSessionResponseDTOWithChats() {

    when(chatRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(ACTIVE_CHAT));
    when(consultantService.findConsultantsByAgencyIds(Mockito.any()))
        .thenReturn(Arrays.asList(CONSULTANT));

    List<UserSessionResponseDTO> resultList = chatService.getChatsForUserId(USER_ID);

    assertNull(resultList.get(0).getSession());
    assertNotNull(resultList.get(0).getChat());
    assertEquals(ACTIVE_CHAT.getId(), resultList.get(0).getChat().getId());
    assertEquals(ACTIVE_CHAT.getTopic(), resultList.get(0).getChat().getTopic());
    assertEquals(ACTIVE_CHAT.getConsultingType().getValue(),
        resultList.get(0).getChat().getConsultingType());
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

  /**
   * Method getChatsForConsultant
   */

  @Test
  public void getChatsForConsultant_Should_ThrowInternalServerErrorExceptionOnDatabaseError() {

    Consultant consultant = Mockito.mock(Consultant.class);
    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("Database error") {};
    when(chatRepository.findByAgencyIds(Mockito.any())).thenThrow(ex);

    try {
      chatService.getChatsForConsultant(consultant);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getChatsForConsultant_Should_ReturnListOfConsultantSessionResponseDTOWithChats() {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(chatRepository.findByAgencyIds(Mockito.any())).thenReturn(Arrays.asList(ACTIVE_CHAT));
    when(consultantService.findConsultantsByAgencyIds(Mockito.any()))
        .thenReturn(Arrays.asList(CONSULTANT));

    List<ConsultantSessionResponseDTO> resultList = chatService.getChatsForConsultant(consultant);

    assertNull(resultList.get(0).getSession());
    assertNotNull(resultList.get(0).getChat());
    assertEquals(ACTIVE_CHAT.getId(), resultList.get(0).getChat().getId());
    assertEquals(ACTIVE_CHAT.getTopic(), resultList.get(0).getChat().getTopic());
    assertEquals(ACTIVE_CHAT.getConsultingType().getValue(),
        resultList.get(0).getChat().getConsultingType());
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
  public void getChatsForConsultant_Should_ReturnNullWhenListOfChatsIsNull() {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(chatRepository.findByAgencyIds(Mockito.any())).thenReturn(null);

    List<ConsultantSessionResponseDTO> resultList = chatService.getChatsForConsultant(consultant);

    assertNull(resultList);
  }

  @Test
  public void getChatsForConsultant_Should_ReturnNullWhenListOfChatsIsEmpty() {

    Consultant consultant = Mockito.mock(Consultant.class);

    when(chatRepository.findByAgencyIds(Mockito.any())).thenReturn(new ArrayList<Chat>());

    List<ConsultantSessionResponseDTO> resultList = chatService.getChatsForConsultant(consultant);

    assertNull(resultList);
  }

  /**
   * Method saveChat
   */

  @Test
  public void saveChat_Should_ThrowSaveChatException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("database error") {};
    when(chatRepository.save(Mockito.any())).thenThrow(ex);

    try {
      chatService.saveChat(ACTIVE_CHAT);
      fail("Expected exception: SaveChatException");
    } catch (SaveChatException saveChatException) {
      assertTrue("Excepted SaveChatException thrown", true);
    }

  }

  /**
   * Method saveChatAgencyRelation
   */

  @Test
  public void saveChatAgencyRelation_Should_ThrowSaveChatAgencyException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("database error") {};
    when(chatAgencyRepository.save(Mockito.any())).thenThrow(ex);

    try {
      chatService.saveChatAgencyRelation(CHAT_AGENCY);
      fail("Expected exception: SaveChatAgencyException");
    } catch (SaveChatAgencyException saveChatAgencyException) {
      assertTrue("Excepted SaveChatAgencyException thrown", true);
    }

  }

  /**
   * Method getChat
   */

  @Test
  public void getChat_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("database error") {};
    when(chatRepository.findById(Mockito.any())).thenThrow(ex);

    try {
      chatService.getChat(CHAT_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getChat_Should_ReturnChatObject() {

    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));

    Optional<Chat> result = chatService.getChat(CHAT_ID);

    assertThat(result, instanceOf(Optional.class));
    assertTrue(result.isPresent());
    assertThat(result.get(), instanceOf(Chat.class));

  }

  /**
   * Method deleteChat
   */

  @Test
  public void deleteChat_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException("database error") {};
    Mockito.doThrow(ex).when(chatRepository).delete(Mockito.any());

    try {
      chatService.deleteChat(ACTIVE_CHAT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  /**
   * Method updateChat
   */

  @Test
  public void updateChat_Should_ThrowBadRequestException_WhenChatDoesNotExist()
      throws SaveChatException {

    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  @Test
  public void updateChat_Should_ThrowForbiddenException_WhenCallingConsultantNotOwnerOfChat()
      throws SaveChatException {

    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(INACTIVE_CHAT));

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_3);
      fail("Expected exception: ForbiddenException");
    } catch (ForbiddenException forbiddenException) {
      assertTrue("Excepted ForbiddenException thrown", true);
    }
  }

  @Test
  public void updateChat_Should_ThrowConflictException_WhenChatIsActive() throws SaveChatException {

    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_CONSULTANT);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue("Excepted ConflictException thrown", true);
    }
  }

  @Test
  public void updateChat_Should_SaveNewChatSettings() throws SaveChatException {

    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);
    when(inactiveChat.getChatOwner()).thenReturn(CONSULTANT);

    when(chatRepository.findById(Mockito.any())).thenReturn(Optional.of(inactiveChat));

    chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_CONSULTANT);

    verify(chatRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void updateChat_Should_ReturnCorrectGroupIdAndChatLinkObject() throws SaveChatException {

    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);
    when(inactiveChat.getChatOwner()).thenReturn(CONSULTANT);
    when(inactiveChat.getGroupId()).thenReturn(RC_GROUP_ID);
    when(inactiveChat.getConsultingType()).thenReturn(CONSULTING_TYPE_KREUZBUND);

    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(inactiveChat));

    UpdateChatResponseDTO result =
        chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_CONSULTANT);
    String chatLink =
        userHelper.generateChatUrl(inactiveChat.getId(), inactiveChat.getConsultingType());

    assertEquals(result.getGroupId(), inactiveChat.getGroupId());
    assertEquals(result.getChatLink(), chatLink);
  }

}

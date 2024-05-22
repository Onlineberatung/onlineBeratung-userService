package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AUTHENTICATED_USER;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AUTHENTICATED_USER_3;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AUTHENTICATED_USER_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_HINT_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_V2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.INACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.UserChat;
import de.caritas.cob.userservice.api.port.out.ChatAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ChatRepository;
import de.caritas.cob.userservice.api.port.out.UserChatRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @InjectMocks private ChatService chatService;

  @Mock private ChatRepository chatRepository;

  @Mock private ChatAgencyRepository chatAgencyRepository;

  @Mock private UserChatRepository chatUserRepository;

  @Mock private Logger logger;

  @Mock private ConsultantService consultantService;

  @Mock private AgencyService agencyService;

  @BeforeEach
  void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  void getChatsForUserId_Should_CallFindByUserIdAndFindAssignedByUserIdOnChatRepository() {
    chatService.getChatsForUserId(USER_ID);

    verify(chatRepository).findByUserId(USER_ID);
    verify(chatRepository).findAssignedByUserId(USER_ID);
  }

  @Test
  void getChatsForUserId_Should_ConcatChatsAndAssignedChats() {
    when(chatRepository.findByUserId(USER_ID)).thenReturn(singletonList(ACTIVE_CHAT));
    when(chatRepository.findAssignedByUserId(USER_ID)).thenReturn(singletonList(CHAT_V2));

    List<UserSessionResponseDTO> resultList = chatService.getChatsForUserId(USER_ID);

    assertEquals(2, resultList.size());
  }

  @Test
  void getChatsForUserId_Should_ReturnListOfUserSessionResponseDTOWithChats() {
    when(chatRepository.findByUserId(USER_ID)).thenReturn(singletonList(ACTIVE_CHAT));
    when(consultantService.findConsultantsByAgencyIds(Mockito.any()))
        .thenReturn(singletonList(CONSULTANT));

    List<UserSessionResponseDTO> resultList = chatService.getChatsForUserId(USER_ID);

    assertNull(resultList.get(0).getSession());
    assertNotNull(resultList.get(0).getChat());
    assertEquals(ACTIVE_CHAT.getId(), resultList.get(0).getChat().getId());
    assertEquals(ACTIVE_CHAT.getTopic(), resultList.get(0).getChat().getTopic());
    assertThat(
        ACTIVE_CHAT.getConsultingTypeId(), is(resultList.get(0).getChat().getConsultingType()));
    assertEquals(
        LocalDate.of(
            ACTIVE_CHAT.getStartDate().getYear(),
            ACTIVE_CHAT.getStartDate().getMonth(),
            ACTIVE_CHAT.getStartDate().getDayOfMonth()),
        resultList.get(0).getChat().getStartDate());
    assertEquals(
        LocalTime.of(
            ACTIVE_CHAT.getInitialStartDate().getHour(),
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
  void
      getChatsForUserId_Should_ReturnListOfUserSessionResponseDTOWithChats_When_AssignedChatIsFound() {
    when(chatRepository.findAssignedByUserId(USER_ID)).thenReturn(singletonList(CHAT_V2));
    when(consultantService.findConsultantsByAgencyIds(Mockito.any()))
        .thenReturn(singletonList(CONSULTANT));
    when(agencyService.getAgency(
            CHAT_V2.getChatAgencies().stream().findFirst().orElseThrow().getAgencyId()))
        .thenReturn(new AgencyDTO().name("agency name"));

    List<UserSessionResponseDTO> resultList = chatService.getChatsForUserId(USER_ID);

    UserSessionResponseDTO resultUserSessionResponseDTO = resultList.get(0);
    assertNull(resultUserSessionResponseDTO.getSession());
    assertNotNull(resultUserSessionResponseDTO.getChat());
    assertEquals(CHAT_V2.getId(), resultUserSessionResponseDTO.getChat().getId());
    assertEquals(CHAT_V2.getTopic(), resultUserSessionResponseDTO.getChat().getTopic());
    assertThat(
        CHAT_V2.getConsultingTypeId(),
        is(resultUserSessionResponseDTO.getChat().getConsultingType()));
    assertEquals(
        LocalDate.of(
            CHAT_V2.getStartDate().getYear(),
            CHAT_V2.getStartDate().getMonth(),
            CHAT_V2.getStartDate().getDayOfMonth()),
        resultUserSessionResponseDTO.getChat().getStartDate());
    assertEquals(
        LocalTime.of(
            CHAT_V2.getInitialStartDate().getHour(), CHAT_V2.getInitialStartDate().getMinute()),
        resultUserSessionResponseDTO.getChat().getStartTime());
    assertEquals(CHAT_V2.getDuration(), resultUserSessionResponseDTO.getChat().getDuration());
    assertEquals(CHAT_V2.isRepetitive(), resultUserSessionResponseDTO.getChat().isRepetitive());
    assertEquals(CHAT_V2.isActive(), resultUserSessionResponseDTO.getChat().isActive());
    assertEquals(CHAT_V2.getGroupId(), resultUserSessionResponseDTO.getChat().getGroupId());

    assertNotNull(resultUserSessionResponseDTO.getChat().getModerators());
    assertEquals(1, resultUserSessionResponseDTO.getChat().getModerators().length);
    assertEquals(
        CONSULTANT.getRocketChatId(), resultUserSessionResponseDTO.getChat().getModerators()[0]);
    assertEquals(1, resultUserSessionResponseDTO.getChat().getAssignedAgencies().size());
    assertEquals(
        "agency name",
        resultUserSessionResponseDTO.getChat().getAssignedAgencies().get(0).getName());
    assertNotNull(resultUserSessionResponseDTO.getChat().getCreatedAt());
    assertEquals(CHAT_HINT_MESSAGE, resultUserSessionResponseDTO.getChat().getHintMessage());
  }

  @Test
  void getChatsForConsultant_Should_ReturnListOfConsultantSessionResponseDTOWithChats() {
    Consultant consultant = Mockito.mock(Consultant.class);

    when(chatRepository.findByAgencyIds(Mockito.any())).thenReturn(singletonList(ACTIVE_CHAT));
    when(consultantService.findConsultantsByAgencyIds(Mockito.any()))
        .thenReturn(singletonList(CONSULTANT));

    List<ConsultantSessionResponseDTO> resultList = chatService.getChatsForConsultant(consultant);

    assertNull(resultList.get(0).getSession());
    assertNotNull(resultList.get(0).getChat());
    assertEquals(ACTIVE_CHAT.getId(), resultList.get(0).getChat().getId());
    assertEquals(ACTIVE_CHAT.getTopic(), resultList.get(0).getChat().getTopic());
    assertThat(
        ACTIVE_CHAT.getConsultingTypeId(), is(resultList.get(0).getChat().getConsultingType()));
    assertEquals(
        LocalDate.of(
            ACTIVE_CHAT.getStartDate().getYear(),
            ACTIVE_CHAT.getStartDate().getMonth(),
            ACTIVE_CHAT.getStartDate().getDayOfMonth()),
        resultList.get(0).getChat().getStartDate());
    assertEquals(
        LocalTime.of(
            ACTIVE_CHAT.getInitialStartDate().getHour(),
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
  void getChatsForConsultant_Should_ReturnEmptyListWhenListOfChatsIsEmpty() {
    Consultant consultant = Mockito.mock(Consultant.class);

    List<ConsultantSessionResponseDTO> resultList = chatService.getChatsForConsultant(consultant);

    assertThat(resultList, hasSize(0));
  }

  @Test
  void getChat_Should_ReturnChatObject() {
    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));

    Optional<Chat> result = chatService.getChat(CHAT_ID);

    assertThat(result, instanceOf(Optional.class));
    assertTrue(result.isPresent());
    assertThat(result.get(), instanceOf(Chat.class));
  }

  @Test
  void getChatByGroupId_Should_ReturnChatObject() {
    when(chatRepository.findByGroupId(RC_GROUP_ID)).thenReturn(Optional.of(ACTIVE_CHAT));

    Optional<Chat> result = chatService.getChatByGroupId(RC_GROUP_ID);

    assertThat(result, instanceOf(Optional.class));
    assertTrue(result.isPresent());
    assertThat(result.get(), instanceOf(Chat.class));
  }

  @Test
  void updateChat_Should_ThrowBadRequestException_WhenChatDoesNotExist() {
    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue(true, "Excepted BadRequestException thrown");
    }
  }

  @Test
  void updateChat_Should_ThrowForbiddenException_WhenCallingConsultantNotOwnerOfChat() {
    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(INACTIVE_CHAT));

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_3);
      fail("Expected exception: ForbiddenException");
    } catch (ForbiddenException forbiddenException) {
      assertTrue(true, "Excepted ForbiddenException thrown");
    }
  }

  @Test
  void updateChat_Should_ThrowConflictException_WhenChatIsActive() {
    when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));

    try {
      chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_CONSULTANT);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue(true, "Excepted ConflictException thrown");
    }
  }

  @Test
  void updateChat_Should_SaveNewChatSettings() {
    // given
    Chat inactiveChat = new Chat();
    inactiveChat.setActive(false);
    inactiveChat.setChatOwner(CONSULTANT);

    when(chatRepository.findById(Mockito.any())).thenReturn(Optional.of(inactiveChat));

    // when
    chatService.updateChat(CHAT_ID, CHAT_DTO, AUTHENTICATED_USER_CONSULTANT);

    // then
    ArgumentCaptor<Chat> chatArgumentCaptor = ArgumentCaptor.forClass(Chat.class);
    verify(chatRepository, times(1)).save(chatArgumentCaptor.capture());
    assertEquals(CHAT_HINT_MESSAGE, chatArgumentCaptor.getValue().getHintMessage());
  }

  @Test
  void saveChatAgencyRelation_Should_saveChatAgencyInRepository() {
    ChatAgency chatAgency = new ChatAgency();

    chatService.saveChatAgencyRelation(chatAgency);

    verify(chatAgencyRepository).save(chatAgency);
  }

  @Test
  void saveUserChatRelation_Should_saveUserChatInRepository() {
    UserChat chatUser = new UserChat();

    chatService.saveUserChatRelation(chatUser);

    verify(chatUserRepository).save(chatUser);
  }

  @Test
  void deleteChat_Should_deleteChatInRepository() {
    Chat chat = new Chat();

    chatService.deleteChat(chat);

    verify(chatRepository).delete(chat);
  }
}

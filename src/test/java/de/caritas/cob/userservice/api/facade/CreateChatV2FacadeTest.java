package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_DURATION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_REPETITIVE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_START_DATE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_START_TIME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_TOPIC;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_AGENCIES_SET;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_CHAT_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ChatDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class CreateChatV2FacadeTest {

  @InjectMocks private CreateChatFacade createChatFacade;

  @Mock private AgencyService agencyService;

  @Mock private ChatService chatService;

  @Mock private Consultant consultant;

  @Mock private RocketChatService rocketChatService;

  @Mock private Chat chat;

  @Mock private ChatAgency chatAgency;

  @Mock private GroupResponseDTO groupResponseDTO;

  @Mock private GroupDTO groupDTO;

  @SuppressWarnings("unused")
  @Spy
  private ChatConverter chatConverter;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    when(chat.getId()).thenReturn(CHAT_ID);
    when(chat.getCreateDate()).thenReturn(LocalDateTime.now());
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void
      createChatV2_Should_ThrowBadRequestException_When_ConsultantAgencyDoesNotMatchChatAgency()
          throws Exception {
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
    String consultantId = UUID.randomUUID().toString();
    when(consultant.getId()).thenReturn(consultantId);

    var chatWithNonMatchingAgency =
        new ChatDTO(
            CHAT_TOPIC,
            CHAT_START_DATE,
            CHAT_START_TIME,
            CHAT_DURATION,
            CHAT_REPETITIVE,
            999L,
            "hint");

    try {
      createChatFacade.createChatV2(chatWithNonMatchingAgency, consultant);
    } catch (BadRequestException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "Consultant with id " + consultantId + " is not assigned to agency with id 999");
    }
  }

  @Test
  public void createChatV2_Should_ReturnValidCreateChatResponseDTO_When_DataAccessSucceeds()
      throws Exception {
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);

    CreateChatResponseDTO result = createChatFacade.createChatV2(CHAT_DTO, consultant);

    assertThat(result).isInstanceOf(CreateChatResponseDTO.class);
    assertThat(result.getGroupId()).isEqualTo(RC_GROUP_ID);
  }

  @Test
  public void createChatV2_Should_SaveConsultantAgency_From_CharDTOAsChatAgencyRelations()
      throws RocketChatCreateGroupException {
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(ACTIVE_CHAT);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verifyNoInteractions(agencyService);
    var captor = ArgumentCaptor.forClass(ChatAgency.class);
    verify(chatService, times(1)).saveChatAgencyRelation(captor.capture());

    assertThat(captor.getValue().getAgencyId()).isEqualTo(CHAT_DTO.getAgencyId());
  }

  @Test
  public void createChatV2_Should_SaveChatToDatabase() throws RocketChatCreateGroupException {
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(ACTIVE_CHAT);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verify(chatService, times(2)).saveChat(any());
  }

  @Test
  public void createChatV2_Should_SaveChatGroupId() throws RocketChatCreateGroupException {
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);

    createChatFacade.createChatV1(CHAT_DTO, consultant);

    InOrder inOrder = Mockito.inOrder(chat, chatService);
    inOrder.verify(chat, times(1)).setGroupId(RC_GROUP_ID);
    inOrder.verify(chatService, times(1)).saveChat(chat);
  }

  @Test
  public void
      createChatV2_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_RocketChatGroupCouldNotBeCreated()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
              .thenThrow(new RocketChatCreateGroupException(ERROR));
          when(chatService.saveChat(Mockito.any())).thenReturn(chat);
          when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

          createChatFacade.createChatV2(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());
        });
  }

  @Test
  public void
      createChatV2_Should_ThrowInternalServerErrorExceptionAndRollback_When_RocketChatGroupIsNotPresent()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(rocketChatService.createPrivateGroupWithSystemUser(GROUP_CHAT_NAME))
              .thenReturn(Optional.empty());
          when(chatService.saveChat(Mockito.any())).thenReturn(chat);
          when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

          createChatFacade.createChatV2(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());
        });
  }

  @Test
  public void
      createChatV2_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_TechnicalUserCouldNotBeAddedToRocketChatGroup()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
              .thenReturn(Optional.of(groupResponseDTO));
          when(chatService.saveChat(Mockito.any())).thenReturn(chat);
          when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);
          when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
          when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
          doThrow(new RocketChatAddUserToGroupException(ERROR))
              .when(rocketChatService)
              .addTechnicalUserToGroup(RC_GROUP_ID);

          createChatFacade.createChatV2(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
        });
  }

  @Test
  public void
      createChatV2_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_RocketChatUserIsNotInitialized()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
              .thenReturn(Optional.of(groupResponseDTO));
          when(chatService.saveChat(Mockito.any())).thenReturn(chat);
          when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);
          when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
          when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
          doThrow(new RocketChatUserNotInitializedException(ERROR))
              .when(rocketChatService)
              .addTechnicalUserToGroup(RC_GROUP_ID);

          createChatFacade.createChatV2(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
        });
  }

  @Test
  public void
      createChatV2_Should_ThrowInternalServerErrorExceptionAndDoRollback_WhenChatCouldNotBeSavedWithGroupId()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(chatService.saveChat(Mockito.any()))
              .thenReturn(chat)
              .thenThrow(new InternalServerErrorException(""));
          when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
              .thenReturn(Optional.of(groupResponseDTO));
          when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
          when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
          when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

          createChatFacade.createChatV2(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
        });
  }

  @Test
  public void createChatV2_Should_ThrowInternalServerErrorException_WhenChatCouldNotBeSaved() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(chatService.saveChat(Mockito.any())).thenThrow(new InternalServerErrorException(""));

          createChatFacade.createChatV2(CHAT_DTO, consultant);

          verify(chatService, never()).deleteChat(chat);
          verify(rocketChatService, never()).deleteGroupAsSystemUser(RC_GROUP_ID);
        });
  }
}

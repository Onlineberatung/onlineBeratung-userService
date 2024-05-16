package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_DTO_KREUZBUND;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_AGENCIES_SET;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_CHAT_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateChatResponseDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class CreateChatV1FacadeTest {

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
    when(chat.getConsultingTypeId()).thenReturn(15);
    when(chat.getCreateDate()).thenReturn(LocalDateTime.now());
    when(agencyService.getAgency(any())).thenReturn(AGENCY_DTO_KREUZBUND);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void createChatV1_Should_ThrowInternalServerErrorException_When_ConsultantHasNoAgency() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          createChatFacade.createChatV1(CHAT_DTO, consultant);

          verify(consultant, atLeastOnce()).getConsultantAgencies();
        });
  }

  @Test
  public void createChatV1_Should_ReturnValidCreateChatResponseDTO_When_DataAccessSucceeds()
      throws Exception {
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);

    CreateChatResponseDTO result = createChatFacade.createChatV1(CHAT_DTO, consultant);

    assertThat(result, instanceOf(CreateChatResponseDTO.class));
    assertEquals(RC_GROUP_ID, result.getGroupId());
  }

  @Test
  public void createChatV1_Should_SaveChatToDatabase() throws RocketChatCreateGroupException {
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(ACTIVE_CHAT);

    createChatFacade.createChatV1(CHAT_DTO, consultant);

    verify(chatService, times(2)).saveChat(any());
  }

  @Test
  public void createChatV1_Should_SaveChatAgencyRelations() throws RocketChatCreateGroupException {
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(ACTIVE_CHAT);

    createChatFacade.createChatV1(CHAT_DTO, consultant);

    verify(chatService).saveChatAgencyRelation(any());
  }

  @Test
  public void createChatV1_Should_SaveChatGroupId() throws RocketChatCreateGroupException {
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
      createChatV1_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_RocketChatGroupCouldNotBeCreated()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
              .thenThrow(new RocketChatCreateGroupException(ERROR));
          when(chatService.saveChat(Mockito.any())).thenReturn(chat);
          when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

          createChatFacade.createChatV1(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());
        });
  }

  @Test
  public void
      createChatV1_Should_ThrowInternalServerErrorExceptionAndRollback_When_RocketChatGroupIsNotPresent()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(rocketChatService.createPrivateGroupWithSystemUser(GROUP_CHAT_NAME))
              .thenReturn(Optional.empty());
          when(chatService.saveChat(Mockito.any())).thenReturn(chat);
          when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

          createChatFacade.createChatV1(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());
        });
  }

  @Test
  public void
      createChatV1_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_TechnicalUserCouldNotBeAddedToRocketChatGroup()
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

          createChatFacade.createChatV1(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
        });
  }

  @Test
  public void
      createChatV1_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_RocketChatUSerIsNotInitialized()
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

          createChatFacade.createChatV1(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
        });
  }

  @Test
  public void
      createChatV1_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_ChatAgencyRelationCouldNotBeSaved()
          throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
              .thenReturn(Optional.of(groupResponseDTO));
          when(chatService.saveChat(Mockito.any())).thenReturn(chat);
          when(chatService.saveChatAgencyRelation(Mockito.any()))
              .thenThrow(new InternalServerErrorException(""));

          createChatFacade.createChatV1(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
        });
  }

  @Test
  public void
      createChatV1_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_ChatCouldNotBeSavedWithGroupId()
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

          createChatFacade.createChatV1(CHAT_DTO, consultant);

          verify(chatService, times(1)).deleteChat(chat);
          verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
        });
  }

  @Test
  public void createChatV1_Should_ThrowInternalServerErrorException_When_ChatCouldNotBeSaved() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCIES_SET);
          when(chatService.saveChat(Mockito.any())).thenThrow(new InternalServerErrorException(""));

          createChatFacade.createChatV1(CHAT_DTO, consultant);

          verify(chatService, never()).deleteChat(chat);
          verify(rocketChatService, never()).deleteGroupAsSystemUser(RC_GROUP_ID);
        });
  }
}

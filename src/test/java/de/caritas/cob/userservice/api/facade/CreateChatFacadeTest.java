package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_AGENCY_SET;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.GROUP_CHAT_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.SaveChatAgencyException;
import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatLoginException;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CreateChatFacadeTest {

  @InjectMocks
  private CreateChatFacade createChatFacade;
  @Mock
  private ChatService chatService;
  @Mock
  private Consultant consultant;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private RocketChatHelper rocketChatHelper;
  @Mock
  private Chat chat;
  @Mock
  private ChatAgency chatAgency;
  @Mock
  private ChatHelper chatHelper;
  @Mock
  private UserHelper userHelper;
  @Mock
  private GroupResponseDTO groupResponseDTO;
  @Mock
  private GroupDTO groupDTO;
  @Mock
  private Logger logger;

  @Before
  public void setup() {
    when(chat.getId()).thenReturn(CHAT_ID);
    when(chat.getConsultingType()).thenReturn(ConsultingType.KREUZBUND);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  /**
   * Method: createChat
   */

  @Test
  public void createChat_Should_ReturnNull_When_ConsultantHasNoAgency() {

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertNull(result);

    verify(consultant, atLeastOnce()).getConsultantAgencies();
    verify(logger, times(1)).error(anyString(), anyString(), anyString());

  }

  @Test
  public void createChat_Should_ReturnValidCreateChatResponseDTO_When_DataAccessSucceeds()
      throws Exception {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatHelper.generateGroupChatName(chat)).thenReturn(GROUP_CHAT_NAME);
    when(rocketChatService.createPrivateGroupWithSystemUser(GROUP_CHAT_NAME))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertThat(result, instanceOf(CreateChatResponseDTO.class));
    assertEquals(result.getGroupId(), RC_GROUP_ID);

  }

  @Test
  public void createChat_Should_SaveChatToDatabase() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatHelper.generateGroupChatName(Mockito.any())).thenReturn(GROUP_CHAT_NAME);
    when(rocketChatService.createPrivateGroupWithSystemUser(GROUP_CHAT_NAME))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(ACTIVE_CHAT);
    when(chatHelper.convertChatDTOtoChat(CHAT_DTO, consultant)).thenReturn(ACTIVE_CHAT);

    createChatFacade.createChat(CHAT_DTO, consultant);

    verify(chatService, times(2)).saveChat(ACTIVE_CHAT);

  }

  @Test
  public void createChat_Should_SaveChatGroupId() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatHelper.generateGroupChatName(chat)).thenReturn(GROUP_CHAT_NAME);
    when(rocketChatService.createPrivateGroupWithSystemUser(GROUP_CHAT_NAME))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatHelper.convertChatDTOtoChat(CHAT_DTO, consultant)).thenReturn(chat);

    createChatFacade.createChat(CHAT_DTO, consultant);

    InOrder inOrder = Mockito.inOrder(chat, chatService);
    inOrder.verify(chat, times(1)).setGroupId(RC_GROUP_ID);
    inOrder.verify(chatService, times(1)).saveChat(chat);

  }

  @Test
  public void createChat_Should_ReturnNullAndDoRollback_WhenRocketChatGroupCouldNotBeCreated() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenThrow(new RocketChatCreateGroupException(ERROR));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertNull(result);

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());

  }

  @Test
  public void createChat_Should_ReturnNullAndRollback_WhenRocketChatGroupIsNotPresent() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(GROUP_CHAT_NAME))
        .thenReturn(Optional.empty());
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertNull(result);

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());

  }

  @Test
  public void createChat_Should_ReturnNullAndDoRollback_WhenRocketChatLoginException() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenThrow(new RocketChatLoginException(ERROR));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertNull(result);

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());

  }

  @Test
  public void createChat_Should_ReturnNullAndDoRollback_WhenTechnicalUserCouldNotBeAddedToRocketChatGroup() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(rocketChatService.addTechnicalUserToGroup(RC_GROUP_ID))
        .thenThrow(new RocketChatAddUserToGroupException(ERROR));

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertNull(result);

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);

  }

  @Test
  public void createChat_Should_ReturnNullAndDoRollback_WhenChatAgencyRelationCouldNotBeSaved() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any()))
        .thenThrow(new SaveChatAgencyException(ERROR, new RuntimeException()));

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertNull(result);

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
    verify(chatService, times(1)).deleteChat(chat);
  }

  @Test
  public void createChat_Should_ReturnNullAndDoRollback_WhenChatCouldNotBeSavedWithGroupId() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat)
        .thenThrow(new SaveChatException(ERROR, new RuntimeException()));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertNull(result);

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

  @Test
  public void createChat_Should_ReturnNull_WhenChatCouldNotBeSaved() {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(chatService.saveChat(Mockito.any()))
        .thenThrow(new SaveChatException(ERROR, new RuntimeException()));

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    assertNull(result);

    verify(logger, times(1)).error(anyString(), anyString(), anyString());
    verify(chatService, never()).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

}

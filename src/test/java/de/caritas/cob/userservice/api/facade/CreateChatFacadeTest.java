package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_AGENCY_SET;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.GROUP_CHAT_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.SaveChatAgencyException;
import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chatagency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
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

  @Test(expected = InternalServerErrorException.class)
  public void createChat_Should_ThrowInternalServerErrorException_When_ConsultantHasNoAgency() {

    CreateChatResponseDTO result = createChatFacade.createChat(CHAT_DTO, consultant);

    verify(consultant, atLeastOnce()).getConsultantAgencies();
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
    assertEquals(RC_GROUP_ID, result.getGroupId());

  }

  @Test
  public void createChat_Should_SaveChatToDatabase()
      throws RocketChatCreateGroupException, SaveChatException {

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
  public void createChat_Should_SaveChatGroupId()
      throws RocketChatCreateGroupException, SaveChatException {

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

  @Test(expected = InternalServerErrorException.class)
  public void createChat_Should_ThrowInternalServerErrorExceptionAndDoRollback_WhenRocketChatGroupCouldNotBeCreated()
      throws Exception {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenThrow(new RocketChatCreateGroupException(ERROR));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    createChatFacade.createChat(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());

  }

  @Test(expected = InternalServerErrorException.class)
  public void createChat_Should_ThrowInternalServerErrorExceptionAndRollback_WhenRocketChatGroupIsNotPresent()
      throws Exception {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(GROUP_CHAT_NAME))
        .thenReturn(Optional.empty());
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    createChatFacade.createChat(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());

  }

  @Test(expected = InternalServerErrorException.class)
  public void createChat_Should_ThrowInternalServerErrorExceptionAndDoRollback_WhenTechnicalUserCouldNotBeAddedToRocketChatGroup()
      throws Exception {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    doThrow(new RocketChatAddUserToGroupException(ERROR)).when(rocketChatService)
        .addTechnicalUserToGroup(RC_GROUP_ID);

    createChatFacade.createChat(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createChat_Should_ThrowInternalServerErrorExceptionAndDoRollback_WhenChatAgencyRelationCouldNotBeSaved()
      throws Exception {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any()))
        .thenThrow(new SaveChatAgencyException(ERROR, new RuntimeException()));

    createChatFacade.createChat(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createChat_Should_ThrowInternalServerErrorExceptionAndDoRollback_WhenChatCouldNotBeSavedWithGroupId()
      throws Exception {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat)
        .thenThrow(new SaveChatException(ERROR, new RuntimeException()));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    createChatFacade.createChat(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createChat_Should_ThrowInternalServerErrorException_WhenChatCouldNotBeSaved() throws SaveChatException {

    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(chatService.saveChat(Mockito.any()))
        .thenThrow(new SaveChatException(ERROR, new RuntimeException()));

    createChatFacade.createChat(CHAT_DTO, consultant);

    verify(chatService, never()).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

}

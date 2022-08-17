package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_AGENCY_SET;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_CHAT_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
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
import de.caritas.cob.userservice.api.adapters.web.dto.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CreateChatV2FacadeTest {

  @InjectMocks
  private CreateChatFacade createChatFacade;

  @Mock
  private AgencyService agencyService;

  @Mock
  private ChatService chatService;

  @Mock
  private Consultant consultant;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private Chat chat;

  @Mock
  private ChatAgency chatAgency;

  @Mock
  private UserHelper userHelper;

  @Mock
  private GroupResponseDTO groupResponseDTO;

  @Mock
  private GroupDTO groupDTO;

  @Spy
  private ChatConverter chatConverter;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    when(chat.getId()).thenReturn(CHAT_ID);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void createChatV2_Should_ReturnValidCreateChatResponseDTO_When_DataAccessSucceeds()
      throws Exception {
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);

    CreateChatResponseDTO result = createChatFacade.createChatV2(CHAT_DTO, consultant);

    assertThat(result, instanceOf(CreateChatResponseDTO.class));
    assertEquals(RC_GROUP_ID, result.getGroupId());

  }

  @Test
  public void createChatV2_Should_NotSaveChatAgencyRelations()
      throws RocketChatCreateGroupException {
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(ACTIVE_CHAT);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verifyNoInteractions(agencyService);
    verify(chatService, never()).saveChatAgencyRelation(any());
  }

  @Test
  public void createChatV2_Should_SaveChatToDatabase() throws RocketChatCreateGroupException {
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChat(Mockito.any())).thenReturn(ACTIVE_CHAT);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verify(chatService, times(2)).saveChat(any());
  }

  @Test
  public void createChatV1_Should_SaveChatGroupId() throws RocketChatCreateGroupException {
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
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

  @Test(expected = InternalServerErrorException.class)
  public void createChatV2_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_RocketChatGroupCouldNotBeCreated()
      throws Exception {
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenThrow(new RocketChatCreateGroupException(ERROR));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createChatV2_Should_ThrowInternalServerErrorExceptionAndRollback_When_RocketChatGroupIsNotPresent()
      throws Exception {
    when(rocketChatService.createPrivateGroupWithSystemUser(GROUP_CHAT_NAME))
        .thenReturn(Optional.empty());
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(Mockito.any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createChatV2_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_TechnicalUserCouldNotBeAddedToRocketChatGroup()
      throws Exception {
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    doThrow(new RocketChatAddUserToGroupException(ERROR)).when(rocketChatService)
        .addTechnicalUserToGroup(RC_GROUP_ID);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createChatV2_Should_ThrowInternalServerErrorExceptionAndDoRollback_When_RocketChatUserIsNotInitialized()
      throws Exception {
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(chatService.saveChat(Mockito.any())).thenReturn(chat);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    doThrow(new RocketChatUserNotInitializedException(ERROR)).when(rocketChatService)
        .addTechnicalUserToGroup(RC_GROUP_ID);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createChatV2_Should_ThrowInternalServerErrorExceptionAndDoRollback_WhenChatCouldNotBeSavedWithGroupId()
      throws Exception {
    when(chatService.saveChat(Mockito.any())).thenReturn(chat)
        .thenThrow(new InternalServerErrorException(""));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(groupResponseDTO.getGroup()).thenReturn(groupDTO);
    when(groupDTO.getId()).thenReturn(RC_GROUP_ID);
    when(chatService.saveChatAgencyRelation(Mockito.any())).thenReturn(chatAgency);

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verify(chatService, times(1)).deleteChat(chat);
    verify(rocketChatService, times(1)).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createChatV2_Should_ThrowInternalServerErrorException_WhenChatCouldNotBeSaved() {
    when(consultant.getConsultantAgencies()).thenReturn(CONSULTANT_AGENCY_SET);
    when(chatService.saveChat(Mockito.any())).thenThrow(new InternalServerErrorException(""));

    createChatFacade.createChatV2(CHAT_DTO, consultant);

    verify(chatService, never()).deleteChat(chat);
    verify(rocketChatService, never()).deleteGroupAsSystemUser(RC_GROUP_ID);
  }

}

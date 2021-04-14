package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.GROUP_MEMBER_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.ChatMembersResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class GetChatMembersFacadeTest {

  @InjectMocks
  private GetChatMembersFacade getChatMembersFacade;

  @Mock
  private ChatService chatService;

  @Mock
  private ChatPermissionVerifier chatPermissionVerifier;

  @Mock
  private User user;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private UserHelper userHelper;

  @Test
  public void getChatMembers_Should_ThrowNotFoundException_WhenChatDoesNotExist() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.empty());

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID);
      fail("Expected exception: NotFoundException");
    } catch (NotFoundException notFoundException) {
      assertTrue("Excepted NotFoundException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
  }

  @Test
  public void getChatMembers_Should_ThrowConflictException_WhenChatIsNotActive() {
    Chat inactiveChat = mock(Chat.class);
    when(inactiveChat.isActive()).thenReturn(false);
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(inactiveChat));

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue("Excepted ConflictException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
  }

  @Test
  public void getChatMembers_Should_ThrowRequestForbiddenException_WhenUserHasNoPermissionForChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    doThrow(new ForbiddenException("")).when(chatPermissionVerifier)
        .verifyPermissionForChat(ACTIVE_CHAT);

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  public void getChatMembers_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    doThrow(new ForbiddenException("")).when(chatPermissionVerifier)
        .verifyPermissionForChat(ACTIVE_CHAT);

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  public void getChatMembers_Should_ReturnValidChatMembersResponseDTOForUser()
      throws Exception {
    when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(ACTIVE_CHAT));
    when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
        .thenReturn(GROUP_MEMBER_DTO_LIST);
    when(userHelper.decodeUsername(Mockito.anyString())).thenReturn(USERNAME);

    assertThat(getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId()),
        instanceOf(ChatMembersResponseDTO.class));

    verify(rocketChatService, times(1)).getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId());
    verify(chatService, times(1)).getChat(ACTIVE_CHAT.getId());
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  public void getChatMembers_Should_ReturnValidChatMembersResponseDTOForConsultant()
      throws Exception {
    when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(ACTIVE_CHAT));
    when(chatPermissionVerifier.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(true);
    when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
        .thenReturn(GROUP_MEMBER_DTO_LIST);
    when(userHelper.decodeUsername(Mockito.anyString())).thenReturn(USERNAME);

    assertThat(getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId()),
        instanceOf(ChatMembersResponseDTO.class));

    verify(rocketChatService, times(1)).getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId());
    verify(chatService, times(1)).getChat(ACTIVE_CHAT.getId());
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test(expected = InternalServerErrorException.class)
  public void getChatMembers_Should_throwInternalServerErrorException_When_rocketChatAccessFails()
      throws Exception {
    when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(ACTIVE_CHAT));
    when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
        .thenThrow(new RocketChatGetGroupMembersException(""));
    when(userHelper.decodeUsername(Mockito.anyString())).thenReturn(USERNAME);

    getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId());
  }

  @Test(expected = InternalServerErrorException.class)
  public void getChatMembers_Should_throwInternalServerErrorException_When_rocketChatGroupHasNoId()
      throws Exception {
    Chat activeChatWithoutId = new Chat();
    activeChatWithoutId.setActive(true);
    when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(activeChatWithoutId));
    when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
        .thenThrow(new RocketChatGetGroupMembersException(""));

    getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId());
  }
}

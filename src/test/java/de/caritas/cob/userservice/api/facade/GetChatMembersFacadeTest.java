package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ROLES;
import static de.caritas.cob.userservice.testHelper.TestConstants.GROUP_MEMBER_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ROLES;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.ChatMembersResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.UserService;
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
  private AuthenticatedUser authenticatedUser;
  @Mock
  private ChatHelper chatHelper;
  @Mock
  private ConsultantService consultantService;
  @Mock
  private User user;
  @Mock
  private UserService userService;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private UserHelper userHelper;

  /**
   * Method: getChatMembers
   */
  @Test
  public void getChatMembers_Should_ThrowNotFoundException_WhenChatDoesNotExist() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.empty());

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID, authenticatedUser);
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
      getChatMembersFacade.getChatMembers(CHAT_ID, authenticatedUser);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue("Excepted ConflictException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);

  }

  @Test
  public void getChatMembers_Should_ThrowRequestForbiddenException_WhenUserHasNoPermissionForChat() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(USER_ROLES);
    when(chatHelper.isChatAgenciesContainUserAgency(ACTIVE_CHAT, user)).thenReturn(false);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainUserAgency(ACTIVE_CHAT, user);
    verify(userService, times(1)).getUserViaAuthenticatedUser(authenticatedUser);

  }

  @Test
  public void getChatMembers_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {

    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(CONSULTANT_ROLES);
    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(false);
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(CONSULTANT));

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID, authenticatedUser);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT);
    verify(consultantService, times(1)).getConsultantViaAuthenticatedUser(authenticatedUser);

  }

  @Test
  public void getChatMembers_Should_ReturnValidChatMembersResponseDTOForUser()
      throws Exception {

    when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(USER_ROLES);
    when(chatHelper.isChatAgenciesContainUserAgency(ACTIVE_CHAT, user)).thenReturn(true);
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));
    when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
        .thenReturn(GROUP_MEMBER_DTO_LIST);
    when(userHelper.decodeUsername(Mockito.anyString())).thenReturn(USERNAME);

    assertTrue(getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId(),
        authenticatedUser) instanceof ChatMembersResponseDTO);

    verify(rocketChatService, times(1)).getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId());
    verify(chatService, times(1)).getChat(ACTIVE_CHAT.getId());
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainUserAgency(ACTIVE_CHAT, user);
    verify(userService, times(1)).getUserViaAuthenticatedUser(authenticatedUser);

  }

  @Test
  public void getChatMembers_Should_ReturnValidChatMembersResponseDTOForConsultant()
      throws Exception {

    when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(ACTIVE_CHAT));
    when(authenticatedUser.getRoles()).thenReturn(CONSULTANT_ROLES);
    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(true);
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
        .thenReturn(GROUP_MEMBER_DTO_LIST);
    when(userHelper.decodeUsername(Mockito.anyString())).thenReturn(USERNAME);

    assertTrue(getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId(),
        authenticatedUser) instanceof ChatMembersResponseDTO);

    verify(rocketChatService, times(1)).getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId());
    verify(chatService, times(1)).getChat(ACTIVE_CHAT.getId());
    verify(authenticatedUser, times(1)).getRoles();
    verify(chatHelper, times(1)).isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT);
    verify(consultantService, times(1)).getConsultantViaAuthenticatedUser(authenticatedUser);

  }
}

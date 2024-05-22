package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_MEMBER_DTO_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.dto.ChatMembersResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.helper.ChatPermissionVerifier;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.ChatService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class GetChatMembersFacadeTest {

  @InjectMocks private GetChatMembersFacade getChatMembersFacade;

  @Mock private ChatService chatService;

  @Mock private ChatPermissionVerifier chatPermissionVerifier;

  @Mock private User user;

  @Mock private RocketChatService rocketChatService;

  @Mock private UserRepository userRepository;
  @Mock private ConsultantRepository consultantRepository;

  @Mock private UserHelper userHelper;

  @Test
  public void getChatMembers_Should_ThrowNotFoundException_WhenChatDoesNotExist() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.empty());

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID);
      fail("Expected exception: NotFoundException");
    } catch (NotFoundException notFoundException) {
      assertTrue(true, "Excepted NotFoundException thrown");
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
      assertTrue(true, "Excepted ConflictException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
  }

  @Test
  public void
      getChatMembers_Should_ThrowRequestForbiddenException_WhenUserHasNoPermissionForChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    doThrow(new ForbiddenException(""))
        .when(chatPermissionVerifier)
        .verifyPermissionForChat(ACTIVE_CHAT);

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue(true, "Excepted RequestForbiddenException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  public void
      getChatMembers_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {
    when(chatService.getChat(CHAT_ID)).thenReturn(Optional.of(ACTIVE_CHAT));
    doThrow(new ForbiddenException(""))
        .when(chatPermissionVerifier)
        .verifyPermissionForChat(ACTIVE_CHAT);

    try {
      getChatMembersFacade.getChatMembers(CHAT_ID);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException requestForbiddenException) {
      assertTrue(true, "Excepted RequestForbiddenException thrown");
    }

    verify(chatService, times(1)).getChat(CHAT_ID);
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  public void getChatMembers_Should_ReturnValidChatMembersResponseDTOForUser() throws Exception {
    when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(ACTIVE_CHAT));
    when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
        .thenReturn(GROUP_MEMBER_DTO_LIST);

    assertThat(
        getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId()),
        instanceOf(ChatMembersResponseDTO.class));

    verify(rocketChatService, times(1)).getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId());
    verify(chatService, times(1)).getChat(ACTIVE_CHAT.getId());
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  public void getChatMembers_Should_ReturnValidChatMembersResponseDTOForConsultant()
      throws Exception {
    when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(ACTIVE_CHAT));
    when(chatPermissionVerifier.hasSameAgencyAssigned(ACTIVE_CHAT, CONSULTANT)).thenReturn(true);
    when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
        .thenReturn(GROUP_MEMBER_DTO_LIST);

    assertThat(
        getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId()),
        instanceOf(ChatMembersResponseDTO.class));

    verify(rocketChatService, times(1)).getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId());
    verify(chatService, times(1)).getChat(ACTIVE_CHAT.getId());
    verify(chatPermissionVerifier, times(1)).verifyPermissionForChat(ACTIVE_CHAT);
  }

  @Test
  public void getChatMembers_Should_throwInternalServerErrorException_When_rocketChatAccessFails()
      throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          when(chatService.getChat(ACTIVE_CHAT.getId())).thenReturn(Optional.of(ACTIVE_CHAT));
          when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
              .thenThrow(new RocketChatGetGroupMembersException(""));

          getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId());
        });
  }

  @Test
  public void getChatMembers_Should_throwInternalServerErrorException_When_rocketChatGroupHasNoId()
      throws Exception {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          Chat activeChatWithoutId = new Chat();
          activeChatWithoutId.setActive(true);
          when(chatService.getChat(ACTIVE_CHAT.getId()))
              .thenReturn(Optional.of(activeChatWithoutId));
          when(rocketChatService.getStandardMembersOfGroup(ACTIVE_CHAT.getGroupId()))
              .thenThrow(new RocketChatGetGroupMembersException(""));

          getChatMembersFacade.getChatMembers(ACTIVE_CHAT.getId());
        });
  }
}

package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID_3;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.ChatAgency;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.model.UserChat;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChatPermissionVerifierTest {

  @InjectMocks private ChatPermissionVerifier chatPermissionVerifier;

  @Mock private ConsultantService consultantService;

  @Mock private UserService userService;

  @Mock private AuthenticatedUser authenticatedUser;

  @Mock private Chat chat;

  @Mock private Consultant consultant;

  @Mock private User user;

  @Test
  public void hasSameAgencyAssigned_Should_ReturnTrue_When_ChatAgenciesContainConsultantAgency() {
    ChatAgency[] chatAgencyArray = new ChatAgency[] {new ChatAgency(chat, AGENCY_ID_2)};
    Set<ChatAgency> chatAgencySet = new HashSet<>(Arrays.asList(chatAgencyArray));

    when(chat.getChatAgencies()).thenReturn(chatAgencySet);

    ConsultantAgency[] consultantAgencyArray =
        new ConsultantAgency[] {
          new ConsultantAgency(
              AGENCY_ID, consultant, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc(), null, null),
          new ConsultantAgency(
              AGENCY_ID_2, consultant, AGENCY_ID_2, nowInUtc(), nowInUtc(), nowInUtc(), null, null)
        };
    Set<ConsultantAgency> consultantAgencySet = new HashSet<>(Arrays.asList(consultantAgencyArray));

    when(consultant.getConsultantAgencies()).thenReturn(consultantAgencySet);

    assertTrue(chatPermissionVerifier.hasSameAgencyAssigned(chat, consultant));
  }

  @Test
  public void
      hasSameAgencyAssigned_Should_ReturnFalse_When_ChatAgenciesNotContainConsultantAgency() {
    ConsultantAgency[] consultantAgencyArray =
        new ConsultantAgency[] {
          new ConsultantAgency(
              AGENCY_ID, consultant, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc(), null, null),
          new ConsultantAgency(
              AGENCY_ID_3, consultant, AGENCY_ID_3, nowInUtc(), nowInUtc(), nowInUtc(), null, null)
        };
    Set<ConsultantAgency> consultantAgencySet = new HashSet<>(Arrays.asList(consultantAgencyArray));

    when(consultant.getConsultantAgencies()).thenReturn(consultantAgencySet);

    assertFalse(chatPermissionVerifier.hasSameAgencyAssigned(chat, consultant));
  }

  @Test
  public void hasSameAgencyAssigned_Should_ReturnTrue_When_ChatAgenciesContainUserAgency() {
    ChatAgency[] chatAgencyArray = new ChatAgency[] {new ChatAgency(chat, AGENCY_ID_2)};
    Set<ChatAgency> chatAgencySet = new HashSet<>(Arrays.asList(chatAgencyArray));

    when(chat.getChatAgencies()).thenReturn(chatAgencySet);

    UserAgency[] userAgencyArray =
        new UserAgency[] {
          new UserAgency(AGENCY_ID, user, AGENCY_ID, null, null),
          new UserAgency(AGENCY_ID_2, user, AGENCY_ID_2, null, null)
        };
    Set<UserAgency> userAgencySet = new HashSet<>(Arrays.asList(userAgencyArray));

    when(user.getUserAgencies()).thenReturn(userAgencySet);

    assertTrue(chatPermissionVerifier.hasSameAgencyAssigned(chat, user));
  }

  @Test
  public void hasSameAgencyAssigned_Should_ReturnFalse_When_ChatAgenciesNotContainUserAgency() {
    UserAgency[] userAgencyArray =
        new UserAgency[] {
          new UserAgency(AGENCY_ID, user, AGENCY_ID, null, null),
          new UserAgency(AGENCY_ID_3, user, AGENCY_ID_3, null, null)
        };
    Set<UserAgency> userAgencySet = new HashSet<>(Arrays.asList(userAgencyArray));

    when(user.getUserAgencies()).thenReturn(userAgencySet);

    assertFalse(chatPermissionVerifier.hasSameAgencyAssigned(chat, user));
  }

  @Test
  public void hasSameAgencyAssigned_Should_ReturnFalse_When_UsersChatAgenciesAreNull() {
    Chat chat = new Chat();

    assertFalse(chatPermissionVerifier.hasSameAgencyAssigned(chat, user));
  }

  @Test
  public void hasChatUserAssignment_Should_ReturnTrue_When_UserHasUserChatAssignment() {
    Chat chat = new Chat();
    UserChat userChat = UserChat.builder().chat(chat).user(user).build();
    chat.setChatUsers(Set.of(userChat));

    assertTrue(chatPermissionVerifier.hasChatUserAssignment(chat, user));
  }

  @Test
  public void hasChatUserAssignment_Should_ReturnFalse_When_UserHasNoUserChatAssignment() {
    Chat chat = new Chat();
    UserChat userChat = UserChat.builder().chat(chat).user(new User()).build();
    chat.setChatUsers(Set.of(userChat));

    assertFalse(chatPermissionVerifier.hasChatUserAssignment(chat, user));
  }

  @Test
  public void hasChatUserAssignment_Should_ReturnFalse_When_UsersChatUsersAreNull() {
    Chat chat = new Chat();

    assertFalse(chatPermissionVerifier.hasChatUserAssignment(chat, user));
  }

  @Test
  public void
      verifyPermissionForChat_Should_verifyConsultantPermission_When_ChatAgenciesContainConsultantAgency() {
    Consultant consultant = new Consultant();
    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(1L);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(1L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));

    assertDoesNotThrow(() -> this.chatPermissionVerifier.verifyPermissionForChat(chat));
  }

  @Test(expected = NotFoundException.class)
  public void
      verifyPermissionForChat_Should_throwNotFoundException_When_consultantDoesNotExistInDatabase() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.empty());

    this.chatPermissionVerifier.verifyPermissionForChat(chat);
  }

  @Test(expected = ForbiddenException.class)
  public void
      verifyPermissionForChat_Should_throwForbiddenException_When_agenciesOfChatAndConsultantAreDifferent() {
    Consultant consultant = new Consultant();
    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(1L);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(2L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));

    this.chatPermissionVerifier.verifyPermissionForChat(chat);
  }

  @Test
  public void
      verifyPermissionForChat_Should_verifyUserPermission_When_ChatAgenciesContainUserAgency() {
    User user = new User();
    UserAgency userAgency = new UserAgency();
    userAgency.setAgencyId(1L);
    user.setUserAgencies(asSet(userAgency));
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(1L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));

    assertDoesNotThrow(() -> this.chatPermissionVerifier.verifyPermissionForChat(chat));
  }

  @Test
  public void verifyPermissionForChat_Should_verifyUserPermission_When_UserHasUserChatAssignment() {
    UserChat userChat = UserChat.builder().chat(chat).user(user).build();
    when(chat.getChatUsers()).thenReturn(Set.of(userChat));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));

    assertDoesNotThrow(() -> this.chatPermissionVerifier.verifyPermissionForChat(chat));
  }

  @Test
  public void
      verifyCanModerateChat_Should_AllowToModerate_When_ConsultantHasAccessToSameAgencyAsChat() {
    Consultant consultant = new Consultant();
    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(1L);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(1L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));
    assertDoesNotThrow(() -> this.chatPermissionVerifier.verifyCanModerateChat(chat));
  }

  @Test(expected = ForbiddenException.class)
  public void
      verifyCanModerateChat_Should_Not_AllowToModerate_When_ConsultantDoesNotHaveAccessToSameAgencyAsChat() {
    Consultant consultant = new Consultant();
    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(2L);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(1L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser))
        .thenReturn(Optional.of(consultant));
    this.chatPermissionVerifier.verifyCanModerateChat(chat);
  }

  @Test(expected = ForbiddenException.class)
  public void verifyCanModerateChat_Should_Not_AllowToModerate_When_AccessedAsUser() {
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(1L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    this.chatPermissionVerifier.verifyCanModerateChat(chat);
  }

  @Test(expected = NotFoundException.class)
  public void
      verifyPermissionForChat_Should_throwNotFoundException_When_UserDoesNotExistInDatabase() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.empty());

    this.chatPermissionVerifier.verifyPermissionForChat(chat);
  }

  @Test(expected = ForbiddenException.class)
  public void
      verifyPermissionForChat_Should_throwForbiddenException_When_AgenciesOfChatAndUserAreDifferent() {
    User user = new User();
    UserAgency userAgency = new UserAgency();
    userAgency.setAgencyId(1L);
    user.setUserAgencies(asSet(userAgency));
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(2L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));

    this.chatPermissionVerifier.verifyPermissionForChat(chat);
  }

  @Test(expected = ForbiddenException.class)
  public void
      verifyPermissionForChat_Should_throwForbiddenException_When_UserHasNoChatUserAssignment() {
    Chat chat = new Chat();
    UserChat userChat = UserChat.builder().user(new User()).chat(chat).build();
    chat.setChatUsers(Set.of(userChat));

    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.of(user));

    this.chatPermissionVerifier.verifyPermissionForChat(chat);
  }
}

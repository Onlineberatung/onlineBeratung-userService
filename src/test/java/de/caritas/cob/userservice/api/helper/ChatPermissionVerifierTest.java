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
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ChatPermissionVerifierTest {

  @InjectMocks
  private ChatPermissionVerifier chatPermissionVerifier;

  @Mock
  private ConsultantService consultantService;

  @Mock
  private UserService userService;

  @Mock
  private AuthenticatedUser authenticatedUser;

  @Mock
  private Chat chat;

  @Mock
  private Consultant consultant;

  @Mock
  private User user;

  @Before
  public void setup() {
    ChatAgency[] chatAgencyArray = new ChatAgency[]{new ChatAgency(chat, AGENCY_ID_2)};
    Set<ChatAgency> chatAgencySet = new HashSet<>(Arrays.asList(chatAgencyArray));
    when(chat.getChatAgencies()).thenReturn(chatAgencySet);
  }

  @Test
  public void isChatAgenciesContainConsultantAgency_Should_ReturnTrue_WhenChatAgenciesContainConsultantAgency() {
    ConsultantAgency[] consultantAgencyArray =
        new ConsultantAgency[]{
            new ConsultantAgency(AGENCY_ID, consultant, AGENCY_ID, nowInUtc(),
                nowInUtc(), nowInUtc(), null),
            new ConsultantAgency(AGENCY_ID_2, consultant, AGENCY_ID_2, nowInUtc(),
                nowInUtc(), nowInUtc(), null)};
    Set<ConsultantAgency> consultantAgencySet =
        new HashSet<>(Arrays.asList(consultantAgencyArray));

    when(consultant.getConsultantAgencies()).thenReturn(consultantAgencySet);

    assertTrue(chatPermissionVerifier.hasSameAgencyAssigned(chat, consultant));
  }

  @Test
  public void isChatAgenciesContainConsultantAgency_Should_ReturnFalse_WhenChatAgenciesNotContainConsultantAgency() {
    ConsultantAgency[] consultantAgencyArray =
        new ConsultantAgency[]{
            new ConsultantAgency(AGENCY_ID, consultant, AGENCY_ID, nowInUtc(),
                nowInUtc(), nowInUtc(), null),
            new ConsultantAgency(AGENCY_ID_3, consultant, AGENCY_ID_3, nowInUtc(),
                nowInUtc(), nowInUtc(), null)};
    Set<ConsultantAgency> consultantAgencySet =
        new HashSet<>(Arrays.asList(consultantAgencyArray));

    when(consultant.getConsultantAgencies()).thenReturn(consultantAgencySet);

    assertFalse(chatPermissionVerifier.hasSameAgencyAssigned(chat, consultant));
  }

  @Test
  public void isChatAgenciesContainUserAgency_Should_ReturnTrue_WhenChatAgenciesContainUserAgency() {
    UserAgency[] userAgencyArray = new UserAgency[]{
        new UserAgency(AGENCY_ID, user, AGENCY_ID, null, null),
        new UserAgency(AGENCY_ID_2, user, AGENCY_ID_2, null, null)};
    Set<UserAgency> userAgencySet = new HashSet<>(Arrays.asList(userAgencyArray));

    when(user.getUserAgencies()).thenReturn(userAgencySet);

    assertTrue(chatPermissionVerifier.hasSameAgencyAssigned(chat, user));
  }

  @Test
  public void isChatAgenciesContainUserAgency_Should_ReturnFalse_WhenChatAgenciesNotContainUserAgency() {
    UserAgency[] userAgencyArray = new UserAgency[]{
        new UserAgency(AGENCY_ID, user, AGENCY_ID, null, null),
        new UserAgency(AGENCY_ID_3, user, AGENCY_ID_3, null, null)};
    Set<UserAgency> userAgencySet = new HashSet<>(Arrays.asList(userAgencyArray));

    when(user.getUserAgencies()).thenReturn(userAgencySet);

    assertFalse(chatPermissionVerifier.hasSameAgencyAssigned(chat, user));
  }

  @Test
  public void verifyPermissionForChat_Should_verifyConsultantPermission_When_authenticatedUserHasRoleConsultant() {
    Consultant consultant = new Consultant();
    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(1L);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(1L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser)).thenReturn(
        Optional.of(consultant));

    assertDoesNotThrow(() -> this.chatPermissionVerifier.verifyPermissionForChat(chat));
  }

  @Test(expected = NotFoundException.class)
  public void verifyPermissionForChat_Should_throwNotFoundException_When_consultantDoesNotExistInDatabase() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser)).thenReturn(
        Optional.empty());

    this.chatPermissionVerifier.verifyPermissionForChat(chat);
  }

  @Test(expected = ForbiddenException.class)
  public void verifyPermissionForChat_Should_throwForbiddenException_When_agenciesOfChatAndConsultantAreDifferent() {
    Consultant consultant = new Consultant();
    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(1L);
    consultant.setConsultantAgencies(asSet(consultantAgency));
    ChatAgency chatAgency = new ChatAgency();
    chatAgency.setAgencyId(2L);
    Chat chat = new Chat();
    chat.setChatAgencies(asSet(chatAgency));
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.CONSULTANT.getValue()));
    when(consultantService.getConsultantViaAuthenticatedUser(authenticatedUser)).thenReturn(
        Optional.of(consultant));

    this.chatPermissionVerifier.verifyPermissionForChat(chat);
  }

  @Test
  public void verifyPermissionForChat_Should_verifyUserPermission_When_authenticatedUserHasRoleUser() {
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

  @Test(expected = NotFoundException.class)
  public void verifyPermissionForChat_Should_throwNotFoundException_When_userDoesNotExistInDatabase() {
    when(authenticatedUser.getRoles()).thenReturn(asSet(UserRole.USER.getValue()));
    when(userService.getUserViaAuthenticatedUser(authenticatedUser)).thenReturn(Optional.empty());

    this.chatPermissionVerifier.verifyPermissionForChat(chat);
  }

  @Test(expected = ForbiddenException.class)
  public void verifyPermissionForChat_Should_throwForbiddenException_When_agenciesOfChatAndUserAreDifferent() {
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

}

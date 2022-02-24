package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_ALL_PEER_SESSIONS;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.FEEDBACK_SESSION_WITH_ASKER_AND_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_WITH_ASKER_AND_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.TEAM_SESSION_WITH_ASKER_AND_CONSULTANT;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.service.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import java.util.List;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnauthorizedMembersProviderTest {

  @InjectMocks
  UnauthorizedMembersProvider unauthorizedMembersProvider;

  @Mock
  ConsultantService consultantService;

  @Mock
  RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @Mock
  KeycloakAdminClientService keycloakAdminClientService;

  EasyRandom easyRandom = new EasyRandom();
  Consultant newConsultant = easyRandom.nextObject(Consultant.class);
  Consultant normalConsultant = easyRandom.nextObject(Consultant.class);
  Consultant teamConsultant = easyRandom.nextObject(Consultant.class);
  Consultant teamConsultant2 = easyRandom.nextObject(Consultant.class);
  Consultant mainConsultant = easyRandom.nextObject(Consultant.class);
  Consultant mainConsultant2 = easyRandom.nextObject(Consultant.class);
  Consultant peerConsultant = easyRandom.nextObject(Consultant.class);
  Consultant peerConsultant2 = easyRandom.nextObject(Consultant.class);
  RocketChatCredentials techUserRcCredentials = easyRandom.nextObject(RocketChatCredentials.class);
  List<GroupMemberDTO> initialMemberList;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    setField(unauthorizedMembersProvider, FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID,
        ROCKET_CHAT_SYSTEM_USER_ID);
    newConsultant.setRocketChatId("newConsultantRcId");
    normalConsultant.setRocketChatId("normalConsultantRcId");
    normalConsultant.setTeamConsultant(false);
    teamConsultant.setRocketChatId("teamConsultantRcId");
    teamConsultant.setTeamConsultant(true);
    teamConsultant2.setRocketChatId("teamConsultantRcId2");
    teamConsultant2.setTeamConsultant(true);
    mainConsultant.setRocketChatId("mainConsultantRcId");
    mainConsultant.setTeamConsultant(true);
    mainConsultant2.setRocketChatId("mainConsultantRcId2");
    mainConsultant2.setTeamConsultant(true);
    peerConsultant.setRocketChatId("peerConsultantRcId");
    peerConsultant.setTeamConsultant(true);
    peerConsultant2.setRocketChatId("peerConsultantRcId2");
    peerConsultant2.setTeamConsultant(true);
    techUserRcCredentials.setRocketChatUserId("techUserRcId");
    initialMemberList = asList(
        new GroupMemberDTO("userRcId", null, "name", null, null),
        new GroupMemberDTO("newConsultantRcId", null, "name", null, null),
        new GroupMemberDTO("normalConsultantRcId", null, "name", null, null),
        new GroupMemberDTO("otherRcId", null, "name", null, null),
        new GroupMemberDTO("otherRcId2", null, "name", null, null),
        new GroupMemberDTO("teamConsultantRcId", null, "name", null, null),
        new GroupMemberDTO("teamConsultantRcId2", null, "name", null, null),
        new GroupMemberDTO("mainConsultantRcId", null, "name", null, null),
        new GroupMemberDTO("mainConsultantRcId2", null, "name", null, null),
        new GroupMemberDTO("peerConsultantRcId", null, "name", null, null),
        new GroupMemberDTO("peerConsultantRcId2", null, "name", null, null),
        new GroupMemberDTO("rcTechnicalRcId", null, "name", null, null),
        new GroupMemberDTO(ROCKET_CHAT_SYSTEM_USER_ID, null, "name", null, null),
        new GroupMemberDTO("techUserRcId", null, "name", null, null)
    );
    List.of(newConsultant, normalConsultant, teamConsultant, teamConsultant2, mainConsultant,
            mainConsultant2, peerConsultant, peerConsultant2)
        .forEach(consultant ->
            when(consultantService.getConsultantByRcUserId(consultant.getRocketChatId()))
                .thenReturn(Optional.of(consultant)));
  }

  @Test
  public void obtainConsultantsToRemove_Should_ReturnCorrectUnauthorizedMemberList_When_SessionIsNoTeamSession()
      throws RocketChatUserNotInitializedException {
    newConsultant.setTeamConsultant(false);
    when(rocketChatCredentialsProvider.getTechnicalUser()).thenReturn(techUserRcCredentials);

    List<Consultant> result = unauthorizedMembersProvider.obtainConsultantsToRemove(RC_GROUP_ID,
        SESSION_WITH_ASKER_AND_CONSULTANT, newConsultant, initialMemberList);

    assertThat(result.size(), is(7));
    assertThat(result, contains(normalConsultant, teamConsultant, teamConsultant2, mainConsultant,
        mainConsultant2, peerConsultant, peerConsultant2));
  }

  @Test
  public void obtainConsultantsToRemove_Should_ReturnCorrectUnauthorizedMemberList_When_SessionIsNormalTeamSession()
      throws RocketChatUserNotInitializedException {
    newConsultant.setTeamConsultant(true);
    when(rocketChatCredentialsProvider.getTechnicalUser()).thenReturn(techUserRcCredentials);
    when(consultantService
        .findConsultantsByAgencyId(TEAM_SESSION_WITH_ASKER_AND_CONSULTANT.getAgencyId()))
        .thenReturn(asList(newConsultant, normalConsultant, teamConsultant, teamConsultant2,
            mainConsultant, mainConsultant2, peerConsultant, peerConsultant2));

    var result = unauthorizedMembersProvider.obtainConsultantsToRemove(RC_GROUP_ID,
        TEAM_SESSION_WITH_ASKER_AND_CONSULTANT, newConsultant, initialMemberList);

    assertThat(result.size(), is(1));
    assertThat(result, contains(normalConsultant));
  }

  @Test
  public void obtainConsultantsToRemove_Should_ReturnCorrectUnauthorizedMemberList_When_SessionIsFeedbackSessionForNormalGroup()
      throws RocketChatUserNotInitializedException {
    newConsultant.setTeamConsultant(true);
    when(rocketChatCredentialsProvider.getTechnicalUser()).thenReturn(techUserRcCredentials);
    var consultantList = asList(newConsultant, normalConsultant, teamConsultant, teamConsultant2,
        mainConsultant, mainConsultant2, peerConsultant, peerConsultant2);
    when(consultantService
        .findConsultantsByAgencyId(FEEDBACK_SESSION_WITH_ASKER_AND_CONSULTANT.getAgencyId()))
        .thenReturn(consultantList);
    consultantList.forEach(consultant ->
        when(consultantService.getConsultantByRcUserId(consultant.getRocketChatId()))
            .thenReturn(Optional.of(consultant)));
    when(keycloakAdminClientService
        .userHasAuthority(mainConsultant.getId(), VIEW_ALL_PEER_SESSIONS)).thenReturn(true);
    when(keycloakAdminClientService
        .userHasAuthority(mainConsultant2.getId(), VIEW_ALL_PEER_SESSIONS)).thenReturn(true);

    List<Consultant> result = unauthorizedMembersProvider
        .obtainConsultantsToRemove(RC_GROUP_ID, FEEDBACK_SESSION_WITH_ASKER_AND_CONSULTANT,
            newConsultant, initialMemberList);

    assertThat(result.size(), is(5));
    assertThat(result, contains(normalConsultant, teamConsultant, teamConsultant2, peerConsultant,
        peerConsultant2));
  }

  @Test
  public void obtainConsultantsToRemove_Should_ReturnCorrectUnauthorizedMemberList_When_SessionIsFeedbackSessionForFeedbackGroup()
      throws RocketChatUserNotInitializedException {
    newConsultant.setTeamConsultant(true);
    when(rocketChatCredentialsProvider.getTechnicalUser()).thenReturn(techUserRcCredentials);
    var consultantList = asList(newConsultant, normalConsultant, teamConsultant, teamConsultant2,
        mainConsultant, mainConsultant2, peerConsultant, peerConsultant2);
    when(consultantService
        .findConsultantsByAgencyId(FEEDBACK_SESSION_WITH_ASKER_AND_CONSULTANT.getAgencyId()))
        .thenReturn(consultantList);
    when(keycloakAdminClientService
        .userHasAuthority(mainConsultant.getId(), VIEW_ALL_FEEDBACK_SESSIONS)).thenReturn(true);
    when(keycloakAdminClientService
        .userHasAuthority(mainConsultant2.getId(), VIEW_ALL_FEEDBACK_SESSIONS)).thenReturn(true);

    List<Consultant> result = unauthorizedMembersProvider
        .obtainConsultantsToRemove(RC_FEEDBACK_GROUP_ID_2,
            FEEDBACK_SESSION_WITH_ASKER_AND_CONSULTANT, newConsultant, initialMemberList);

    assertThat(result.size(), is(5));
    assertThat(result, contains(normalConsultant, teamConsultant, teamConsultant2, peerConsultant,
        peerConsultant2));
  }

  @Test(expected = InternalServerErrorException.class)
  public void obtainConsultantsToRemove_Should_ThrowInternalServerError_When_TechUserIsNotInitialized()
      throws RocketChatUserNotInitializedException {
    when(rocketChatCredentialsProvider.getTechnicalUser())
        .thenThrow(new RocketChatUserNotInitializedException(""));

    unauthorizedMembersProvider.obtainConsultantsToRemove(RC_GROUP_ID,
        FEEDBACK_SESSION_WITH_ASKER_AND_CONSULTANT, newConsultant, initialMemberList);
  }
}

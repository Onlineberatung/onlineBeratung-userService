package de.caritas.cob.userservice.api.admin.service.rocketchat;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_KREUZBUND;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_U25;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RocketChatOperationConditionProviderTest {

  @InjectMocks private RocketChatOperationConditionProvider conditionProvider;

  @Mock private KeycloakService keycloakService;

  @Mock private Session session;

  @Mock private Consultant consultant;

  @Mock private ConsultingTypeManager consultingTypeManager;

  @Test
  public void canAddToRocketChatGroup_Should_returnTrue_When_sessionIsAnEnquiry() {
    when(this.session.getStatus()).thenReturn(SessionStatus.NEW);

    boolean result = this.conditionProvider.canAddToRocketChatGroup();

    assertThat(result, is(true));
  }

  @Test
  public void canAddToRocketChatGroup_Should_returnTrue_When_sessionIsATeamSession() {
    when(this.session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(this.session.isTeamSession()).thenReturn(true);
    when(this.session.getConsultingTypeId()).thenReturn(15);
    when(consultingTypeManager.getConsultingTypeSettings(15))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);

    boolean result = this.conditionProvider.canAddToRocketChatGroup();

    assertThat(result, is(true));
  }

  @Test
  public void
      canAddToRocketChatGroup_Should_returnTrue_When_sessionIsATeamSessionAndConsultingTypeIsU25AndConsultantHasAuthority() {
    when(this.session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(this.session.isTeamSession()).thenReturn(true);
    when(this.session.getConsultingTypeId()).thenReturn(1);
    when(consultingTypeManager.getConsultingTypeSettings(1))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);

    boolean result = this.conditionProvider.canAddToRocketChatGroup();

    assertThat(result, is(true));
  }

  @Test
  public void
      canAddToRocketChatGroup_Should_returnTrue_When_sessionIsATeamSessionAndConsultingTypeIsU25AndConsultantHasRole() {
    when(this.session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(this.session.isTeamSession()).thenReturn(true);
    when(this.session.getConsultingTypeId()).thenReturn(1);
    when(this.keycloakService.userHasAuthority(any(), any())).thenReturn(false);
    when(this.keycloakService.userHasRole(any(), any())).thenReturn(true);
    when(this.consultingTypeManager.getConsultingTypeSettings(1))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);

    boolean result = this.conditionProvider.canAddToRocketChatGroup();

    assertThat(result, is(true));
  }

  @Test
  public void canAddToRocketChatGroup_Should_returnFalse_When_sessionIsInitial() {
    when(this.session.getStatus()).thenReturn(SessionStatus.INITIAL);

    boolean result = this.conditionProvider.canAddToRocketChatGroup();

    assertThat(result, is(false));
  }

  @Test
  public void
      canAddToRocketChatGroup_Should_returnFalse_When_sessionIsInProgressButNoTeamSession() {
    when(this.session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(this.session.isTeamSession()).thenReturn(false);

    boolean result = this.conditionProvider.canAddToRocketChatGroup();

    assertThat(result, is(false));
  }

  @Test
  public void
      canAddToRocketChatGroup_Should_returnFalse_When_sessionIsATeamSessionAndConsultingTypeIsU25AndConsultantHasNoAuthorityAndNoRole() {
    when(this.session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(this.session.isTeamSession()).thenReturn(true);
    when(this.session.getConsultingTypeId()).thenReturn(1);
    when(this.keycloakService.userHasAuthority(any(), any())).thenReturn(false);
    when(this.keycloakService.userHasRole(any(), any())).thenReturn(false);
    when(this.consultingTypeManager.getConsultingTypeSettings(1))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);

    boolean result = this.conditionProvider.canAddToRocketChatGroup();

    assertThat(result, is(false));
  }

  @Test
  public void
      canAddToRocketChatFeedbackGroup_Should_returnTrue_When_sessionHasFeedbackGroupAndIsEnquiry() {
    when(this.session.getFeedbackGroupId()).thenReturn("feedbackGroup");
    when(this.session.getStatus()).thenReturn(SessionStatus.NEW);

    boolean result = this.conditionProvider.canAddToRocketChatFeedbackGroup();

    assertThat(result, is(true));
  }

  @Test
  public void
      canAddToRocketChatFeedbackGroup_Should_returnTrue_When_sessionHasFeedbackGroupAndConsultantHasMainConsultantAuthority() {
    when(this.session.getFeedbackGroupId()).thenReturn("feedbackGroup");
    when(this.session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(this.keycloakService.userHasAuthority(any(), any())).thenReturn(true);

    boolean result = this.conditionProvider.canAddToRocketChatFeedbackGroup();

    assertThat(result, is(true));
  }

  @Test
  public void
      canAddToRocketChatFeedbackGroup_Should_returnTrue_When_sessionHasFeedbackGroupAndConsultantHasMainConsultantRole() {
    when(this.session.getFeedbackGroupId()).thenReturn("feedbackGroup");
    when(this.session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(this.keycloakService.userHasAuthority(any(), any())).thenReturn(false);
    when(this.keycloakService.userHasRole(any(), any())).thenReturn(true);

    boolean result = this.conditionProvider.canAddToRocketChatFeedbackGroup();

    assertThat(result, is(true));
  }

  @Test
  public void canAddToRocketChatFeedbackGroup_Should_returnFalse_When_sessionHasNoFeedbackGroup() {
    boolean result = this.conditionProvider.canAddToRocketChatFeedbackGroup();

    assertThat(result, is(false));
  }

  @Test
  public void
      canAddToRocketChatFeedbackGroup_Should_returnFalse_When_sessionHasFeedbackGroupAndConsultantHasNoRoleAndNoAuthority() {
    when(this.session.getFeedbackGroupId()).thenReturn("feedbackGroup");
    when(this.session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(this.keycloakService.userHasAuthority(any(), any())).thenReturn(false);
    when(this.keycloakService.userHasRole(any(), any())).thenReturn(false);

    boolean result = this.conditionProvider.canAddToRocketChatFeedbackGroup();

    assertThat(result, is(false));
  }
}

package de.caritas.cob.userservice.api.service.liveevents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RelevantUserAccountIdsBySessionProviderTest {

  @InjectMocks
  private RelevantUserAccountIdsBySessionProvider bySessionProvider;

  @Mock
  private AuthenticatedUser authenticatedUser;

  @Mock
  private SessionRepository sessionRepository;

  @Test
  public void collectUserIds_Should_returnEmptyList_When_sessionDoesNotExist() {
    List<String> userIds = this.bySessionProvider.collectUserIds("rcGroupId");

    assertThat(userIds, hasSize(0));
  }

  @Test
  public void collectUserIds_Should_returnListWithUserId_When_consultantIsAuthenticatedUser() {
    when(this.authenticatedUser.getUserId()).thenReturn("consultant");
    when(sessionRepository.findByGroupId(any())).thenReturn(buildSessionWithUserAndConsultant());

    List<String> userIds = this.bySessionProvider.collectUserIds("rcGroupId");

    assertThat(userIds, hasSize(1));
    assertThat(userIds.get(0), is("user"));
  }

  private Optional<Session> buildSessionWithUserAndConsultant() {
    Session session = new Session();
    Consultant consultant = new Consultant();
    consultant.setId("consultant");
    session.setConsultant(consultant);
    User user = new User("user", null, null, null);
    session.setUser(user);
    return Optional.of(session);
  }

  @Test
  public void collectUserIds_Should_returnListWithConsultantId_When_userIsAuthenticatedUser() {
    when(this.authenticatedUser.getUserId()).thenReturn("user");
    when(sessionRepository.findByGroupId(any())).thenReturn(buildSessionWithUserAndConsultant());

    List<String> userIds = this.bySessionProvider.collectUserIds("rcGroupId");

    assertThat(userIds, hasSize(1));
    assertThat(userIds.get(0), is("consultant"));
  }

  @Test
  public void collectUserIds_Should_returnListWithUserId_When_consultantIsAuthenticatedUserAndRcIdIsFeedbackGroupId() {
    when(this.authenticatedUser.getUserId()).thenReturn("consultant");
    when(sessionRepository.findByFeedbackGroupId(any()))
        .thenReturn(buildSessionWithUserAndConsultant());

    List<String> userIds = this.bySessionProvider.collectUserIds("rcGroupId");

    assertThat(userIds, hasSize(1));
    assertThat(userIds.get(0), is("user"));
  }

  @Test
  public void collectUserIds_Should_returnListWithConsultantId_When_userIsAuthenticatedUserAndRcIdIsFeedbackGroupId() {
    when(this.authenticatedUser.getUserId()).thenReturn("user");
    when(sessionRepository.findByFeedbackGroupId(any())).thenReturn(buildSessionWithUserAndConsultant());

    List<String> userIds = this.bySessionProvider.collectUserIds("rcGroupId");

    assertThat(userIds, hasSize(1));
    assertThat(userIds.get(0), is("consultant"));
  }

  @Test
  public void collectUserIds_Should_returnListWithConsultantAndUserId_When_authenticatedUserIsOther() {
    when(this.authenticatedUser.getUserId()).thenReturn("other user");
    when(sessionRepository.findByGroupId(any())).thenReturn(buildSessionWithUserAndConsultant());

    List<String> userIds = this.bySessionProvider.collectUserIds("rcGroupId");

    assertThat(userIds, hasSize(2));
    assertThat(userIds.get(0), is("user"));
    assertThat(userIds.get(1), is("consultant"));
  }

  @Test
  public void collectUserIds_Should_returnListWithConsultantAndUserId_When_authenticatedUserIsOtherAndRcIdIsFeedbackGroupId() {
    when(this.authenticatedUser.getUserId()).thenReturn("other user");
    when(sessionRepository.findByFeedbackGroupId(any()))
        .thenReturn(buildSessionWithUserAndConsultant());

    List<String> userIds = this.bySessionProvider.collectUserIds("rcGroupId");

    assertThat(userIds, hasSize(2));
    assertThat(userIds.get(0), is("user"));
    assertThat(userIds.get(1), is("consultant"));
  }

}

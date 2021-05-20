package de.caritas.cob.userservice.api.deactivateworkflow.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateSourceType;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateTargetType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class DeactivateSessionActionTest {

  @InjectMocks
  private DeactivateSessionAction deactivateSessionAction;

  @Mock
  private SessionService sessionService;

  @Mock
  private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void execute_Should_returnEmptyList_When_deactivationOfSessionIsSuccessful() {
    Session mockedSession = mock(Session.class);

    var workflowErrors = this.deactivateSessionAction.execute(mockedSession);

    assertThat(workflowErrors, hasSize(0));
    verify(mockedSession, times(1)).setStatus(SessionStatus.DONE);
    verify(this.sessionService, times(1)).saveSession(any());
    verifyNoMoreInteractions(this.logger);
  }

  @Test
  public void execute_Should_returnExpectedWorkflowErrorAndLogError_When_deactivationFails() {
    doThrow(new RuntimeException()).when(this.sessionService).saveSession(any());

    Session mockedSession = mock(Session.class);
    when(mockedSession.getId()).thenReturn(1L);

    var workflowErrors = this.deactivateSessionAction.execute(mockedSession);

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getSourceType(), is(DeactivateSourceType.ASKER));
    assertThat(workflowErrors.get(0).getTargetType(), is(DeactivateTargetType.DATABASE));
    assertThat(workflowErrors.get(0).getIdentifier(), is("1"));
    assertThat(workflowErrors.get(0).getReason(), containsString(" session "));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(this.logger, times(1)).error(anyString(), anyString());
  }

}

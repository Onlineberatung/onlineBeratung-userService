package de.caritas.cob.userservice.api.workflow.delete.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatMongoDbService;
import de.caritas.cob.userservice.api.adapters.rocketchat.model.RocketchatSession;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CloseInactiveRocketchatSessionsServiceTest {

  @InjectMocks CloseInactiveRocketchatSessionsService closeInactiveRocketchatSessionsService;

  @Mock RocketChatMongoDbService rocketChatMongoDbService;

  @Mock WorkflowErrorMailService workflowErrorMailService;

  @Mock RocketchatSession session;

  @Test
  void deleteInactiveRocketchatSessions_Should_CallDeleteInactiveRocketchatSessions() {
    // given
    when(rocketChatMongoDbService.findInactiveSessions()).thenReturn(Lists.newArrayList(session));
    // when
    closeInactiveRocketchatSessionsService.closeInactiveRocketchatSessions();
    // then
    verify(session).setClosedAt(Mockito.any(Instant.class));
    verifyNoInteractions(workflowErrorMailService);
  }

  @Test
  void
      deleteInactiveRocketchatSessions_Should_SendWorkflowErrorsIfExceptionCaughtUponSessionDeletion() {
    // given
    doThrow(IllegalStateException.class).when(rocketChatMongoDbService).findInactiveSessions();
    // when
    closeInactiveRocketchatSessionsService.closeInactiveRocketchatSessions();
    // then
    verify(workflowErrorMailService).buildAndSendErrorMail(Mockito.anyList());
  }
}

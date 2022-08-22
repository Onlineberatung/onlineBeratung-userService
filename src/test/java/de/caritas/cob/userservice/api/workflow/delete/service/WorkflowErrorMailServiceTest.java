package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.ROCKET_CHAT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowErrorMailServiceTest {

  @InjectMocks private WorkflowErrorMailService workflowErrorMailService;

  @Mock private MailService mailService;

  @Before
  public void setup() {
    setField(workflowErrorMailService, "applicationBaseUrl", "www.host.de");
  }

  @Test
  public void buildAndSendErrorMail_Should_sendNoErrorMail_When_workflowErrorsAreNull() {
    this.workflowErrorMailService.buildAndSendErrorMail(null);

    verifyNoMoreInteractions(this.mailService);
  }

  @Test
  public void buildAndSendErrorMail_Should_sendNoErrorMail_When_workflowErrorsAreEmpty() {
    this.workflowErrorMailService.buildAndSendErrorMail(emptyList());

    verifyNoMoreInteractions(this.mailService);
  }

  @Test
  public void
      buildAndSendErrorMail_Should_buildAndSendExpectedErrorMail_When_workflowErrorsExists() {
    List<DeletionWorkflowError> workflowErrors =
        asList(
            DeletionWorkflowError.builder()
                .deletionSourceType(ASKER)
                .deletionTargetType(ROCKET_CHAT)
                .timestamp(nowInUtc())
                .reason("reason")
                .identifier("id")
                .build(),
            DeletionWorkflowError.builder().build());

    this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);

    verify(this.mailService, times(1)).sendErrorEmailNotification(any());
  }
}

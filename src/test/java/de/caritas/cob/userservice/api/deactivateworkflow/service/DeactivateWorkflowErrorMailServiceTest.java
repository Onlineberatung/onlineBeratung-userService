package de.caritas.cob.userservice.api.deactivateworkflow.service;


import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateSourceType;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateTargetType;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateWorkflowError;
import de.caritas.cob.userservice.api.service.helper.MailService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeactivateWorkflowErrorMailServiceTest {

  @InjectMocks
  private DeactivateWorkflowErrorMailService workflowErrorMailService;

  @Mock
  private MailService mailService;

  @BeforeEach
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
  public void buildAndSendErrorMail_Should_buildAndSendExpectedErrorMail_When_workflowErrorsExists() {
    List<DeactivateWorkflowError> workflowErrors = asList(
        DeactivateWorkflowError.builder()
            .sourceType(DeactivateSourceType.ASKER)
            .targetType(DeactivateTargetType.ROCKET_CHAT)
            .timestamp(nowInUtc())
            .reason("reason")
            .identifier("id")
            .build(),
        DeactivateWorkflowError.builder().build()
    );

    this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);

    verify(this.mailService, times(1)).sendErrorEmailNotification(any());
  }

}

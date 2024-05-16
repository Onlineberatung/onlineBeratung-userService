package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.ROCKET_CHAT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.service.emailsupplier.TenantTemplateSupplier;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.mailservice.generated.web.model.ErrorMailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class WorkflowErrorMailServiceTest {

  @InjectMocks private WorkflowErrorMailService workflowErrorMailService;

  @Mock private MailService mailService;

  @Mock private TenantTemplateSupplier tenantTemplateSupplier;

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
  public void
      buildAndSendErrorMail_Should_buildAndSendExpectedErrorMail_When_workflowErrorsExists() {
    // given
    ReflectionTestUtils.setField(workflowErrorMailService, "multitenancyEnabled", true);
    TemplateDataDTO tenantData = new TemplateDataDTO().key("tenantData");
    when(tenantTemplateSupplier.getTemplateAttributes()).thenReturn(Lists.newArrayList(tenantData));
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

    // when
    this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);

    // then
    ArgumentCaptor<ErrorMailDTO> errorMailDTOArgumentCaptor =
        ArgumentCaptor.forClass(ErrorMailDTO.class);
    verify(this.mailService, times(1))
        .sendErrorEmailNotification(errorMailDTOArgumentCaptor.capture());

    var templateData = errorMailDTOArgumentCaptor.getValue().getTemplateData();
    assertThat(templateData).contains(tenantData);

    // clean up
    ReflectionTestUtils.setField(workflowErrorMailService, "multitenancyEnabled", false);
  }
}

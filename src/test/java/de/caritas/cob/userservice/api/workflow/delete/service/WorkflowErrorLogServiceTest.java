package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.ROCKET_CHAT;
import static java.util.Collections.emptyList;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class WorkflowErrorLogServiceTest {

  @InjectMocks private WorkflowErrorLogService workflowErrorLogService;

  private ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  public void setUp() {
    Logger logger = (Logger) LoggerFactory.getLogger(WorkflowErrorLogService.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @Test
  public void logWorkflowErrors_Should_logNoErrors_When_workflowErrorsAreNull() {

    // when
    this.workflowErrorLogService.logWorkflowErrors(null);

    // then
    Assertions.assertThat(listAppender.list).isEmpty();
  }

  @Test
  public void logWorkflowErrors_Should_logNoErrors_When_workflowErrorsAreEmpty() {

    // when
    this.workflowErrorLogService.logWorkflowErrors(emptyList());

    // then
    Assertions.assertThat(listAppender.list).isEmpty();
  }

  @Test
  public void logWorkflowErrors_Should_logErrors_When_workflowErrorsExists() {
    // given
    List<DeletionWorkflowError> workflowErrors = new ArrayList<>();
    workflowErrors.add(
        DeletionWorkflowError.builder()
            .deletionSourceType(ASKER)
            .deletionTargetType(ROCKET_CHAT)
            .timestamp(nowInUtc())
            .reason("reason")
            .identifier("id")
            .build());

    // when
    this.workflowErrorLogService.logWorkflowErrors(workflowErrors);

    // then
    Assertions.assertThat(listAppender.list).hasSize(1);
  }
}

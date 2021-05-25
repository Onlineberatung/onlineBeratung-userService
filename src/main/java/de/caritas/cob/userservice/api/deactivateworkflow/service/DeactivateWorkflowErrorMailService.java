package de.caritas.cob.userservice.api.deactivateworkflow.service;

import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_FREE_TEXT;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.deactivateworkflow.model.WorkflowError;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.mailservice.generated.web.model.ErrorMailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class to build a error mail for all deletion workflow errors.
 */
@Service
@RequiredArgsConstructor
public class DeactivateWorkflowErrorMailService {

  private final @NonNull MailService mailService;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  /**
   * Builds an {@link ErrorMailDTO} containing a text with all workflow errors and sends it to
   * the {@link MailService}.
   *
   * @param workflowErrors the {@link DeletionWorkflowError} objects
   */
  public void buildAndSendErrorMail(List<? extends WorkflowError<?, ?>> workflowErrors) {
    if (isNotEmpty(workflowErrors)) {
      var errorMailDTO = new ErrorMailDTO()
          .template(TEMPLATE_FREE_TEXT)
          .templateData(asList(
              new TemplateDataDTO().key("subject").value("Deletion workflow errors"),
              new TemplateDataDTO().key("url").value(this.applicationBaseUrl),
              new TemplateDataDTO().key("text").value(convertErrorsToHtmlText(workflowErrors))));

      this.mailService.sendErrorEmailNotification(errorMailDTO);
    }
  }

  private String convertErrorsToHtmlText(List<? extends WorkflowError<?, ?>> workflowErrors) {
    var title = String.format(
        "<h2>(%s) Errors during deactivate workflow:</h2>", workflowErrors.size());

    var body = workflowErrors.stream()
        .map(workflowError ->
            buildItemOutput("SourceType", workflowError.getSourceType())
                + buildItemOutput("TargetType", workflowError.getTargetType())
                + buildItemOutput("Identifier", workflowError.getIdentifier())
                + buildItemOutput("Reason", workflowError.getReason())
                + buildItemOutput("Timestamp", workflowError.getTimestamp())
        ).collect(Collectors.joining("<hr>"));

    return title + body;
  }

  private String buildItemOutput(String itemName, Object itemValue) {
    return String.format("<li>%s = %s</li>", itemName, itemValue);
  }
}

package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier.TEMPLATE_FREE_TEXT;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.mailservice.generated.web.model.ErrorMailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service class to build a error mail for all deletion workflow errors. */
@Service
@RequiredArgsConstructor
public class WorkflowErrorMailService {

  private final @NonNull MailService mailService;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  /**
   * Builds an {@link ErrorMailDTO} containing a text with all workflow errors and sends it to the
   * {@link MailService}.
   *
   * @param workflowErrors the {@link DeletionWorkflowError} objects
   */
  public void buildAndSendErrorMail(List<DeletionWorkflowError> workflowErrors) {
    if (isNotEmpty(workflowErrors)) {
      ErrorMailDTO errorMailDTO =
          new ErrorMailDTO()
              .template(TEMPLATE_FREE_TEXT)
              .templateData(
                  asList(
                      new TemplateDataDTO().key("subject").value("Deletion workflow errors"),
                      new TemplateDataDTO().key("url").value(this.applicationBaseUrl),
                      new TemplateDataDTO()
                          .key("text")
                          .value(convertErrorsToHtmlText(workflowErrors))));

      this.mailService.sendErrorEmailNotification(errorMailDTO);
    }
  }

  private String convertErrorsToHtmlText(List<DeletionWorkflowError> workflowErrors) {
    StringBuilder stringBuilder =
        new StringBuilder()
            .append("<h2>")
            .append("(")
            .append(workflowErrors.size())
            .append(") ")
            .append("Errors during deletion workflow:</h2>");

    workflowErrors.forEach(
        workflowError ->
            stringBuilder
                .append("<li>")
                .append("SourceType = ")
                .append(workflowError.getDeletionSourceType())
                .append("</li><li>TargetType = ")
                .append(workflowError.getDeletionTargetType())
                .append("</li><li>Identifier = ")
                .append(workflowError.getIdentifier())
                .append("</li><li>Reason = ")
                .append(workflowError.getReason())
                .append("</li><li>Timestamp = ")
                .append(workflowError.getTimestamp())
                .append("</li><hr>"));

    return stringBuilder.toString();
  }
}

package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier.TEMPLATE_FREE_TEXT;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.service.emailsupplier.TenantTemplateSupplier;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.mailservice.generated.web.model.ErrorMailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
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

  private final @NonNull TenantTemplateSupplier tenantTemplateSupplier;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  @Value("${multitenancy.enabled}")
  private Boolean multitenancyEnabled;

  /**
   * Builds an {@link ErrorMailDTO} containing a text with all workflow errors and sends it to the
   * {@link MailService}.
   *
   * @param workflowErrors the {@link DeletionWorkflowError} objects
   */
  public void buildAndSendErrorMail(List<DeletionWorkflowError> workflowErrors) {
    if (isNotEmpty(workflowErrors)) {
      var templateAttributes = new ArrayList<TemplateDataDTO>();
      templateAttributes.add(
          new TemplateDataDTO().key("subject").value("Deletion workflow errors"));
      templateAttributes.add(
          new TemplateDataDTO().key("text").value(convertErrorsToHtmlText(workflowErrors)));

      if (!multitenancyEnabled) {
        templateAttributes.add(new TemplateDataDTO().key("url").value(applicationBaseUrl));
      } else {
        templateAttributes.addAll(tenantTemplateSupplier.getTemplateAttributes());
      }

      ErrorMailDTO errorMailDTO =
          new ErrorMailDTO().template(TEMPLATE_FREE_TEXT).templateData(templateAttributes);
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

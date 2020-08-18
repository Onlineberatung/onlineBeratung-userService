package de.caritas.cob.userservice.api.model.mailService;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 
 * Builder for {@link MailDTO}
 * 
 */
@Component
public class MailDtoBuilder {

  /***
   * Build a {@link MailDTO} with the give parameter
   * 
   * @param template
   * @param email
   * @param templateData
   * @return
   */
  public MailDTO build(String template, String email,
      @SuppressWarnings("unchecked") SimpleImmutableEntry<String, String>... templateData) {

    List<TemplateDataDTO> templateDataList = null;
    if (templateData != null && templateData.length > 0) {
      templateDataList = new ArrayList<TemplateDataDTO>();
      for (SimpleImmutableEntry<String, String> templateDataKeyValue : templateData) {
        templateDataList.add(
            new TemplateDataDTO(templateDataKeyValue.getKey(), templateDataKeyValue.getValue()));
      }
    }
    return new MailDTO(template, email, templateDataList);

  }

}

package de.caritas.cob.UserService.api.model.mailService;

import static org.junit.Assert.assertEquals;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.UserService.api.helper.EmailNotificationHelper;

@RunWith(MockitoJUnitRunner.class)
public class MailDtoBuilderTest {

  private final String EMAIL = "mail@mail.de";
  private final String DATA1_NAME = "data1 name";
  private final String DATA1_VALUE = "data1 value";
  private final String DATA2_NAME = "data2 name";
  private final String DATA2_VALUE = "data2 value";

  private final MailDTO CORRECT_MAIL_DTO =
      new MailDTO(EmailNotificationHelper.TEMPLATE_NEW_ENQUIRY_NOTIFICATION, EMAIL, null);

  @SuppressWarnings("unchecked")
  @Test
  public void build_Should_ReturnCorrectMailDto() {

    List<TemplateDataDTO> templateData = new ArrayList<>();
    templateData.add(new TemplateDataDTO(DATA1_NAME, DATA1_VALUE));
    templateData.add(new TemplateDataDTO(DATA2_NAME, DATA2_VALUE));
    CORRECT_MAIL_DTO.setTemplateData(templateData);
    MailDtoBuilder mailDtoBuilder = new MailDtoBuilder();
    MailDTO result = mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_NEW_ENQUIRY_NOTIFICATION,
        EMAIL, new SimpleImmutableEntry<String, String>(DATA1_NAME, DATA1_VALUE),
        new SimpleImmutableEntry<String, String>(DATA2_NAME, DATA2_VALUE));
    assertEquals(CORRECT_MAIL_DTO.getEmail(), result.getEmail());
    assertEquals(CORRECT_MAIL_DTO.getTemplate(), result.getTemplate());
    assertEquals(2, result.getTemplateData().size());
    assertEquals(CORRECT_MAIL_DTO.getTemplateData().get(0).getKey(),
        result.getTemplateData().get(0).getKey());
    assertEquals(CORRECT_MAIL_DTO.getTemplateData().get(0).getValue(),
        result.getTemplateData().get(0).getValue());
    assertEquals(CORRECT_MAIL_DTO.getTemplateData().get(1).getKey(),
        result.getTemplateData().get(1).getKey());
    assertEquals(CORRECT_MAIL_DTO.getTemplateData().get(1).getValue(),
        result.getTemplateData().get(1).getValue());
  }
}

package de.caritas.cob.userservice.api.model.mailservice;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MailDTO {

  private String template;
  private String email;
  private List<TemplateDataDTO> templateData;

}

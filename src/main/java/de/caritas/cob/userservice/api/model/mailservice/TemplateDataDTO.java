package de.caritas.cob.userservice.api.model.mailservice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TemplateDataDTO {

  private String key;
  private String value;

}

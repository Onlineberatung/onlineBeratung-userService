package de.caritas.cob.userservice.api.model;

import com.neovisionaries.i18n.LanguageCode;
import java.io.Serializable;
import lombok.Data;

@Data
public class LanguageId implements Serializable {

  private Consultant consultant;

  private LanguageCode languageCode;
}

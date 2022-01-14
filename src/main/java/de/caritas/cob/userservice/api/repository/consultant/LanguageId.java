package de.caritas.cob.userservice.api.repository.consultant;

import com.neovisionaries.i18n.LanguageCode;
import java.io.Serializable;
import lombok.Data;

@Data
public class LanguageId implements Serializable {

  private Consultant consultant;

  private LanguageCode languageCode;
}

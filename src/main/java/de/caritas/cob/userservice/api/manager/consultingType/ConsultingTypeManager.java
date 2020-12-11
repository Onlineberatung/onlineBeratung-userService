package de.caritas.cob.userservice.api.manager.consultingType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
public class ConsultingTypeManager {

  @Value("${consulting.types.settings.json.path}")
  private String consultingTypesSettingsJsonPath;

  private Map<Integer, ConsultingTypeSettings> consultingTypeSettingsMap;

  @PostConstruct
  private void init() throws IOException {

    log.info("Start initializing consulting type settings...");

    ObjectMapper mapper = new ObjectMapper();
    TypeReference<ConsultingTypeSettings> typeReference =
        new TypeReference<ConsultingTypeSettings>() {};
    InputStream inputStream;
    ConsultingTypeSettings consultingTypeSettings;

    consultingTypeSettingsMap = new HashMap<>();

    for (ConsultingType consultingType : ConsultingType.values()) {
      inputStream =
          TypeReference.class.getResourceAsStream(getJsonFileNameWithPath(consultingType));
      consultingTypeSettings = mapper.readValue(inputStream, typeReference);
      consultingTypeSettings.setConsultingType(consultingType);
      consultingTypeSettingsMap.put(consultingType.getValue(), consultingTypeSettings);

    }

    log.info("Finished initializing consulting type settings...");
  }

  /**
   * Returns the {@link ConsultingTypeSettings} for the provided {@link ConsultingType}.
   * 
   * @param consultingType {@link ConsultingType}
   * @return {@link ConsultingTypeSettings} for the provided {@link ConsultingType}
   * @throws MissingConsultingTypeException when no settings for provided consulting type where
   *         found
   */
  public ConsultingTypeSettings getConsultingTypeSettings(ConsultingType consultingType)
      throws MissingConsultingTypeException {

    if (consultingTypeSettingsMap.containsKey(consultingType.getValue())
        && consultingTypeSettingsMap.get(consultingType.getValue()) != null) {
      return consultingTypeSettingsMap.get(consultingType.getValue());
    } else {
      throw new MissingConsultingTypeException(
          String.format("No settings for consulting type %s found.", consultingType.name()));
    }

  }

  private String getJsonFileNameWithPath(ConsultingType consultingType) {
    return consultingTypesSettingsJsonPath + "/" + consultingType.name().toLowerCase()
        + ".json";
  }

}

package de.caritas.cob.UserService.api.manager.consultingType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.UserService.api.exception.MissingConsultingTypeException;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConsultingTypeManager {

  @Value("${consulting.types.settings.json.path}")
  private String CONSULTING_TYPES_SETTINGS_JSON_PATH;

  private Map<Integer, ConsultingTypeSettings> consultingTypeSettingsMap;

  @PostConstruct
  private void init() throws JsonParseException, JsonMappingException, IOException {

    log.info("Start initializing consulting type settings...");

    ObjectMapper mapper = new ObjectMapper();
    TypeReference<ConsultingTypeSettings> typeReference =
        new TypeReference<ConsultingTypeSettings>() {};
    InputStream inputStream = null;
    ConsultingTypeSettings consultingTypeSettings;

    consultingTypeSettingsMap = new HashMap<Integer, ConsultingTypeSettings>();

    for (ConsultingType consultingType : ConsultingType.values()) {

      consultingTypeSettings = null;
      inputStream =
          TypeReference.class.getResourceAsStream(getJsonFileNameWithPath(consultingType));
      consultingTypeSettings = mapper.readValue(inputStream, typeReference);
      consultingTypeSettings.setConsultingType(consultingType);
      consultingTypeSettingsMap.put(consultingType.getValue(), consultingTypeSettings);

    }

    log.info("Finished initializing consulting type settings...");

  }

  public ConsultingTypeSettings getConsultantTypeSettings(ConsultingType consultingType) {

    if (consultingTypeSettingsMap.containsKey(consultingType.getValue())
        && consultingTypeSettingsMap.get(consultingType.getValue()) != null) {
      return consultingTypeSettingsMap.get(consultingType.getValue());
    } else {
      throw new MissingConsultingTypeException(
          String.format("No settings for consulting type %s found.", consultingType.name()));
    }

  }

  private String getJsonFileNameWithPath(ConsultingType consultingType) {
    return CONSULTING_TYPES_SETTINGS_JSON_PATH + "/" + consultingType.name().toLowerCase()
        + ".json";
  }

}

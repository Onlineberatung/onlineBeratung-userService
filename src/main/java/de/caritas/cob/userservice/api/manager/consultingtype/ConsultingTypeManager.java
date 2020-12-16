package de.caritas.cob.userservice.api.manager.consultingtype;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.LogService;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class ConsultingTypeManager {

  @Value("${consulting.types.settings.json.path}")
  private String consultingTypesSettingsJsonPath;

  private final Map<Integer, ConsultingTypeSettings> consultingTypeSettingsMap = new HashMap<>();

  @PostConstruct
  private void init() {

    LogService.logInfo("Start initializing consulting type settings...");

    Stream.of(ConsultingType.values())
        .forEach(this::appendConsutingTypeSetting);

    LogService.logInfo("Finished initializing consulting type settings...");
  }

  private void appendConsutingTypeSetting(ConsultingType consultingType) {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<ConsultingTypeSettings> typeReference =
        new TypeReference<ConsultingTypeSettings>() {};
    InputStream inputStream =
        TypeReference.class.getResourceAsStream(getJsonFileNameWithPath(consultingType));
    try {
      ConsultingTypeSettings consultingTypeSettings = mapper.readValue(inputStream, typeReference);
      consultingTypeSettings.setConsultingType(consultingType);
      this.consultingTypeSettingsMap.put(consultingType.getValue(), consultingTypeSettings);
    } catch (IOException e) {
      LogService.logWarn(String.format("Unable to provide settings for consulting type %s",
          consultingType));
    }
  }

  /**
   * Returns the {@link ConsultingTypeSettings} for the provided {@link ConsultingType}.
   * 
   * @param consultingType {@link ConsultingType}
   * @return {@link ConsultingTypeSettings} for the provided {@link ConsultingType}
   * @throws MissingConsultingTypeException when no settings for provided consulting type where
   *         found
   */
  public ConsultingTypeSettings getConsultingTypeSettings(ConsultingType consultingType) {

    if (consultingTypeSettingsMap.containsKey(consultingType.getValue())
        && nonNull(consultingTypeSettingsMap.get(consultingType.getValue()))) {
      return consultingTypeSettingsMap.get(consultingType.getValue());
    } else {
      throw new MissingConsultingTypeException(
          String.format("No settings for consulting type %s found.", consultingType.name()));
    }

  }

  private String getJsonFileNameWithPath(ConsultingType consultingType) {
    return consultingTypesSettingsJsonPath + "/" + consultingType.name().toLowerCase() + ".json";
  }

}

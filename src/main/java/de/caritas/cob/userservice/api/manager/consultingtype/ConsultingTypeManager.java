package de.caritas.cob.userservice.api.manager.consultingtype;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.service.LogService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
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

    Stream.of(getAllJsonFiles())
        .forEach(this::appendConsultingTypeSetting);

    LogService.logInfo("Finished initializing consulting type settings...");
  }

  private void appendConsultingTypeSetting(String jsonFileName) {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<ConsultingTypeSettings> typeReference =
        new TypeReference<ConsultingTypeSettings>() {
        };
    InputStream inputStream =
        TypeReference.class.getResourceAsStream(getJsonFileNameWithPath(jsonFileName));
    try {
      ConsultingTypeSettings consultingTypeSettings = mapper.readValue(inputStream, typeReference);
      System.out.println(consultingTypeSettings.getConsultingId());
      this.consultingTypeSettingsMap
          .put(consultingTypeSettings.getConsultingId(), consultingTypeSettings);
    } catch (IOException e) {
      LogService.logWarn(String.format("Unable to provide settings for consulting file %s",
          jsonFileName));
    }
  }

  /**
   * Returns the {@link ConsultingTypeSettings} for the provided consulting ID.
   *
   * @param consultingId The consulting ID for which the seetings are searched
   * @return {@link ConsultingTypeSettings} for the provided consulting ID
   * @throws MissingConsultingTypeException when no settings for provided consulting type where
   *                                        found
   */
  public ConsultingTypeSettings getConsultingTypeSettings(int consultingId) {

    if (consultingTypeSettingsMap.containsKey(consultingId)
        && nonNull(consultingTypeSettingsMap.get(consultingId))) {
      return consultingTypeSettingsMap.get(consultingId);
    } else {
      throw new MissingConsultingTypeException(
          String.format("No settings for consultingId %d found.", consultingId));
    }
  }

  public ConsultingTypeSettings getConsultingTypeSettings(String consultingId) {
    return getConsultingTypeSettings(Integer.parseInt(consultingId));
  }

  public Integer[] getAllConsultingIDs() {
    return consultingTypeSettingsMap.keySet().toArray(new Integer[0]);
  }

  public boolean isConsultantBoundedToAgency(int consultingId) {
    return consultingTypeSettingsMap.entrySet().stream()
        .filter(entrySet -> entrySet.getKey() == consultingId).findFirst()
        .map(entry -> entry.getValue().isConsultantBoundedToConsultingType())
        .orElseThrow(
            () -> new NotFoundException(String.format("No Settings found for consultingId %d",
                consultingId)));
  }

  private String getJsonFileNameWithPath(String jsonFileName) {
    return "/" + consultingTypesSettingsJsonPath + "/" + jsonFileName;
  }

  private String[] getAllJsonFiles() {
    URL dirUrl = ConsultingTypeManager.class.getClassLoader().getResource(consultingTypesSettingsJsonPath);

    try {
      return new File(dirUrl.toURI()).list();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    return null;
  }

}

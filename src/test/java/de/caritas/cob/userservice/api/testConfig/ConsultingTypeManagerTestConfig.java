package de.caritas.cob.userservice.api.testConfig;

import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.service.ConsultingTypeService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import org.jeasy.random.EasyRandom;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ConsultingTypeManagerTestConfig {

  @Bean
  public ConsultingTypeManager consultingTypeManager(ConsultingTypeService consultingTypeService) {
    return new ConsultingTypeManager(consultingTypeService) {
      @Override
      public ExtendedConsultingTypeResponseDTO getConsultingTypeSettings(String consultingTypeId) {
        return createDummy(Integer.parseInt(consultingTypeId));
      }

      @Override
      public ExtendedConsultingTypeResponseDTO getConsultingTypeSettings(int consultingTypeId)
          throws MissingConsultingTypeException {
        return createDummy(consultingTypeId);
      }

      private ExtendedConsultingTypeResponseDTO createDummy(Integer consultingTypeId) {
        var extendedConsultingTypeResponseDTO = new EasyRandom()
            .nextObject(ExtendedConsultingTypeResponseDTO.class);
        extendedConsultingTypeResponseDTO.getMonitoring().setInitializeMonitoring(true);
        extendedConsultingTypeResponseDTO.getMonitoring()
            .setMonitoringTemplateFile("/monitoring.dto");
        extendedConsultingTypeResponseDTO.setId((int) consultingTypeId);
        return extendedConsultingTypeResponseDTO;
      }
    };
  }

}

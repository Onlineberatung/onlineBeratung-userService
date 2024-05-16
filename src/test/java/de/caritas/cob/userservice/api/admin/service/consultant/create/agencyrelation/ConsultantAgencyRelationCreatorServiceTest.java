package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConsultantAgencyRelationCreatorServiceTest {

  private final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks
  private ConsultantAgencyRelationCreatorService consultantAgencyRelationCreatorService;

  @Mock private ConsultantAgencyService consultantAgencyService;

  @Mock private ConsultantRepository consultantRepository;

  @Mock private AgencyService agencyService;

  @Mock private KeycloakService keycloakService;

  @Mock private RocketChatAsyncHelper rocketChatAsyncHelper;

  @Mock private ConsultingTypeManager consultingTypeManager;

  @Test
  public void
      createNewConsultantAgency_Should_notThrowNullPointerException_When_agencyTypeIsU25AndConsultantHasNoAgencyAssigned() {
    AgencyDTO agencyDTO = new AgencyDTO().consultingType(1).id(2L);

    when(this.consultantRepository.findByIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(new Consultant()));
    when(agencyService.getAgencyWithoutCaching(eq(2L))).thenReturn(agencyDTO);

    CreateConsultantAgencyDTO createConsultantAgencyDTO =
        new CreateConsultantAgencyDTO().roleSetKey("valid role set").agencyId(2L);

    final var response =
        easyRandom.nextObject(
            de.caritas.cob.userservice.consultingtypeservice.generated.web.model
                .ExtendedConsultingTypeResponseDTO.class);
    when(consultingTypeManager.getConsultingTypeSettings(1)).thenReturn(response);

    assertDoesNotThrow(
        () ->
            this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
                "consultant Id", createConsultantAgencyDTO));
  }
}

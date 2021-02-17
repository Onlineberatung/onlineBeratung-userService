package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantAgencyRelationCreatorServiceTest {

  @InjectMocks
  private ConsultantAgencyRelationCreatorService consultantAgencyRelationCreatorService;

  @Mock
  private ConsultantAgencyService consultantAgencyService;

  @Mock
  private ConsultantRepository consultantRepository;

  @Mock
  private AgencyServiceHelper agencyServiceHelper;

  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private SessionRepository sessionRepository;

  @Test
  public void createNewConsultantAgency_Should_notThrowNullPointerException_When_agencyTypeIsU25AndConsultantHasNoAgencyAssigned()
      throws AgencyServiceHelperException {
    AgencyDTO agencyDTO = new AgencyDTO()
        .consultingType(ConsultingType.U25)
        .id(2L);

    when(this.consultantRepository.findByIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(new Consultant()));
    when(agencyServiceHelper.getAgencyWithoutCaching(eq(2L))).thenReturn(agencyDTO);
    when(keycloakAdminClientService.userHasRole(any(), any())).thenReturn(true);

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO()
        .role("valid role")
        .agencyId(2L);

    assertDoesNotThrow(() -> this.consultantAgencyRelationCreatorService
        .createNewConsultantAgency("consultant Id", createConsultantAgencyDTO));

  }

}

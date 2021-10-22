package de.caritas.cob.userservice.api.facade.userdata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantDataProviderIT {

  @MockBean
  AgencyService agencyService;
  @MockBean
  AuthenticatedUser authenticatedUser;
  @Autowired
  private ConsultantDataProvider consultantDataProvider;
  @MockBean
  private ConsultingTypeManager consultingTypeManager;
  @Autowired
  private SessionRepository sessionRepository;

  @Test
  public void retrieveData_Should_returnDataWithHasArchiveTrue_When_ConsultantHasRegisteredSessions() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setId("94c3e0b1-0677-4fd2-a7ea-56a71aefd0e8");
    when(this.agencyService.getAgencies(any()))
        .thenReturn(List.of(new AgencyDTO().consultingType(1)));
    when(this.consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(new de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO().isAnonymousConversationAllowed(false));

    var result = consultantDataProvider.retrieveData(consultant);

    assertTrue(result.isHasArchive());
  }

  @Test
  public void retrieveData_Should_returnDataWithHasArchiveFalse_When_ConsultantHasNoRegisteredSessions() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setId("34c3x5b1-0677-4fd2-a7ea-56a71aefd099");
    when(this.agencyService.getAgencies(any()))
        .thenReturn(List.of(new AgencyDTO().consultingType(1)));
    when(this.consultingTypeManager.getConsultingTypeSettings(anyInt()))
        .thenReturn(new de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO().isAnonymousConversationAllowed(false));

    var result = consultantDataProvider.retrieveData(consultant);

    assertFalse(result.isHasArchive());
  }
}

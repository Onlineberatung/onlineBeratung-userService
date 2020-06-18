package de.caritas.cob.UserService.api.facade;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.model.UserDataResponseDTO;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

@RunWith(MockitoJUnitRunner.class)
public class GetUserDataFacadeTest {

  private final String CONSULTANT_ID = "asjdasdjfsdf8";
  private final String CONSULTANT_ROCKETCHAT_ID = "xN3Mobksn3xdp7gEk";
  private final Long AGENCY_ID = 1L;
  private final ConsultantAgency CONSULTANT_AGENCY = new ConsultantAgency(AGENCY_ID, null, 1L);
  private final Set<ConsultantAgency> CONSULTANT_AGENCIES =
      new HashSet<>(Arrays.asList(CONSULTANT_AGENCY));
  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, CONSULTANT_ROCKETCHAT_ID,
      "consultant", "first name", "last name", "consultant@cob.de", false, true, "", false, null,
      null, CONSULTANT_AGENCIES);
  private final AgencyDTO AGENCY_DTO =
      new AgencyDTO(AGENCY_ID, "Max", "12345", "description", true, false, ConsultingType.SUCHT);
  private final String USER_ID = "2485jsfdgnjw4kt";
  private final String USERNAME = "asker";
  private final User USER = new User(USER_ID, USERNAME, null, null);

  @InjectMocks
  private GetUserDataFacade getUserDataFacade;
  @Mock
  AgencyServiceHelper agencyServiceHelper;
  @Mock
  LogService logService;
  @Mock
  SessionService sessionService;

  @Test
  public void getConsultantData_Should_ReturnNullAndLogAgencyServiceHelperException_WhenAgencyServiceHelperFails()
      throws Exception {

    AgencyServiceHelperException agencyServiceHelperException =
        new AgencyServiceHelperException(new Exception());
    when(agencyServiceHelper.getAgency(AGENCY_ID)).thenThrow(agencyServiceHelperException);

    assertEquals(getUserDataFacade.getConsultantData(CONSULTANT), null);
    verify(logService, times(1)).logAgencyServiceHelperException(Mockito.anyString(),
        Mockito.eq(agencyServiceHelperException));

  }

  @Test
  public void getConsultantData_Should_ReturnUserDataResponseDTOWithAgencyDTO_WhenProvidedWithCorrectConsultant() {

    when(agencyServiceHelper.getAgency(AGENCY_ID)).thenReturn(AGENCY_DTO);


    assertThat(getUserDataFacade.getConsultantData(CONSULTANT).getAgencies(),
        everyItem(instanceOf(AgencyDTO.class)));
  }

  @Test
  public void getUserData_Should_ReturnUserDataResponseDTO_WhenProvidedWithValidUser() {
    assertThat(getUserDataFacade.getUserData(USER), instanceOf(UserDataResponseDTO.class));
  }
}

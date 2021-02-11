package de.caritas.cob.userservice.api.service;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ConsultantAgencyServiceTest {

  private final String CONSULTANT_ID = "1b71cc46-650d-42bb-8299-f8e3f6d7249a";
  private final String CONSULTANT_ROCKETCHAT_ID = "xN3Mobksn3xdp7gEk";
  private final Long AGENCY_ID = 1L;
  private final Consultant CONSULTANT =
      new Consultant(CONSULTANT_ID, CONSULTANT_ROCKETCHAT_ID, "consultant", "first name",
          "last name", "consultant@cob.de", false, false, null, false, null, null, null, null, null,
          null);
  private final ConsultantAgency CONSULTANT_AGENCY =
      new ConsultantAgency(AGENCY_ID, CONSULTANT, 1L, LocalDateTime.now(), LocalDateTime.now(),
          LocalDateTime.now());
  private final List<ConsultantAgency> CONSULTANT_AGENY_LIST = Arrays.asList(CONSULTANT_AGENCY);
  private final ConsultantAgency NULL_CONSULTANT_AGENCY = null;
  private final List<ConsultantAgency> CONSULTANT_AGENCY_NULL_LIST =
      Arrays.asList(NULL_CONSULTANT_AGENCY);
  private final ConsultantAgency CONSULTANT_NULL_AGENCY = new ConsultantAgency(AGENCY_ID, null, 1L,
      LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
  private final List<ConsultantAgency> CONSULTANT_NULL_AGENCY_LIST =
      Arrays.asList(CONSULTANT_NULL_AGENCY);
  private final String ERROR = "error";

  @InjectMocks
  private ConsultantAgencyService consultantAgencyService;
  @Mock
  private ConsultantAgencyRepository consultantAgencyRepository;
  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void saveConsultantAgency_Should_SaveConsultantAgency() {

    consultantAgencyService.saveConsultantAgency(CONSULTANT_AGENCY);
    verify(consultantAgencyRepository, times(1)).save(Mockito.any());

  }

  @Test
  public void saveConsultantAgencyt_Should_LogAndThrowInternalServerErrorException_WhenSaveConsultantAgencyFails() {

    @SuppressWarnings("serial")
    DataAccessException dataAccessException = new DataAccessException(ERROR) {
    };
    when(consultantAgencyRepository.save(Mockito.any())).thenThrow(dataAccessException);

    try {
      consultantAgencyService.saveConsultantAgency(CONSULTANT_AGENCY);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  /**
   * Method: isConsultantInAgency
   */

  @Test
  public void isConsultantInAgency_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {
    };
    when(consultantAgencyRepository.findByConsultantIdAndAgencyId(CONSULTANT_ID, AGENCY_ID))
        .thenThrow(ex);

    try {
      consultantAgencyService.isConsultantInAgency(CONSULTANT_ID, AGENCY_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void isConsultantInAgency_Should_ReturnTrue_WhenConsultantFound() {

    when(consultantAgencyRepository.findByConsultantIdAndAgencyId(Mockito.anyString(),
        Mockito.anyLong())).thenReturn(CONSULTANT_AGENY_LIST);

    assertTrue(consultantAgencyService.isConsultantInAgency(CONSULTANT_ID, AGENCY_ID));
  }

  @Test
  public void isConsultantInAgency_Should_ReturnFalse_WhenConsultantNotFound() {

    when(consultantAgencyRepository.findByConsultantIdAndAgencyId(Mockito.anyString(),
        Mockito.anyLong())).thenReturn(null);

    assertFalse(consultantAgencyService.isConsultantInAgency(CONSULTANT_ID, AGENCY_ID));
  }

  /**
   * Method: findConsultantsByAgencyId
   */

  @Test
  public void findConsultantsByAgencyId_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {
    };
    when(consultantAgencyRepository.findByAgencyId(AGENCY_ID)).thenThrow(ex);

    try {
      consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void findConsultantsByAgencyId_Should_ReturnListOfConsultantAgency_WhenAgencyFound() {

    when(consultantAgencyRepository.findByAgencyId(Mockito.anyLong()))
        .thenReturn(CONSULTANT_AGENY_LIST);

    assertThat(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID),
        everyItem(instanceOf(ConsultantAgency.class)));
  }

  /**
   * Method: getConsultantsOfAgency
   */

  @Test
  public void getConsultantsOfAgency_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {
    };
    when(consultantAgencyRepository.findByAgencyIdOrderByConsultantFirstNameAsc(AGENCY_ID))
        .thenThrow(ex);

    try {
      consultantAgencyService.getConsultantsOfAgency(AGENCY_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultantsOfAgency_Should_ThrowInternalServerErrorException_WhenDatabaseAgencyIsNull() {

    when(consultantAgencyRepository.findByAgencyIdOrderByConsultantFirstNameAsc(Mockito.anyLong()))
        .thenReturn(CONSULTANT_AGENCY_NULL_LIST);

    try {
      consultantAgencyService.getConsultantsOfAgency(AGENCY_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultantsOfAgency_Should_ThrowInternalServerErrorException_WhenDatabaseAgencyConsultantIsNull() {

    when(consultantAgencyRepository.findByAgencyIdOrderByConsultantFirstNameAsc(Mockito.anyLong()))
        .thenReturn(CONSULTANT_NULL_AGENCY_LIST);

    try {
      consultantAgencyService.getConsultantsOfAgency(AGENCY_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultantsOfAgency_Should_ReturnListOfConsultantAgency_WhenAgencyFound() {

    when(consultantAgencyRepository.findByAgencyIdOrderByConsultantFirstNameAsc(Mockito.anyLong()))
        .thenReturn(CONSULTANT_AGENY_LIST);

    assertThat(consultantAgencyService.getConsultantsOfAgency(AGENCY_ID),
        everyItem(instanceOf(ConsultantResponseDTO.class)));
  }
}

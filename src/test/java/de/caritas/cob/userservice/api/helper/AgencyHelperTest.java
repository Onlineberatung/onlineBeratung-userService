package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.testHelper.ExceptionConstants.AGENCY_SERVICE_HELPER_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class AgencyHelperTest {

  @InjectMocks
  private AgencyHelper agencyHelper;
  @Mock
  private AgencyServiceHelper agencyServiceHelper;

  /**
   * Method: getVerifiedAgency
   */

  @Test
  public void getVerifiedAgency_Should_ThrowInternalServerErrorException_When_AgencyServiceHelperFails()
      throws AgencyServiceHelperException {

    when(agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID))
        .thenThrow(AGENCY_SERVICE_HELPER_EXCEPTION);

    try {
      agencyHelper.getVerifiedAgency(AGENCY_ID, 0);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getVerifiedAgency_Should_ThrowBadRequestException_When_AgencyIsNotAssignedToGivenConsultingType()
      throws AgencyServiceHelperException {

    when(agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    try {
      agencyHelper.getVerifiedAgency(AGENCY_ID, 15);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  @Test
  public void getVerifiedAgency_Should_ReturnCorrectAgency_When_AgencyIsFoundAndValid()
      throws AgencyServiceHelperException {

    when(agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    AgencyDTO agency = agencyHelper.getVerifiedAgency(AGENCY_ID, 0);

    assertEquals(AGENCY_ID, agency.getId());
  }

  /**
   * Method: doesConsultingTypeMatchToAgency
   */

  @Test
  public void doesConsultingTypeMatchToAgency_Should_ReturnTrue_When_AgencyIsAssignedToGivenConsultingType() {

    when(agencyHelper.getVerifiedAgency(AGENCY_ID, 0))
        .thenReturn(AGENCY_DTO_SUCHT);

    boolean response =
        agencyHelper.doesConsultingTypeMatchToAgency(AGENCY_ID, 0);

    assertTrue(response);
  }

  @Test
  public void doesConsultingTypeMatchToAgency_Should_ReturnFalse_When_AgencyIsNotAssignedToGivenConsultingType() {

    when(agencyHelper.getVerifiedAgency(AGENCY_ID, 0)).thenReturn(null);

    boolean response =
        agencyHelper.doesConsultingTypeMatchToAgency(AGENCY_ID, 0);

    assertFalse(response);
  }
}

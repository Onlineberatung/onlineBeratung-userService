package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.testHelper.ExceptionConstants.AGENCY_SERVICE_HELPER_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;

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
  public void getVerifiedAgency_Should_ThrowServiceException_When_AgencyServiceHelperFails() {

    when(agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID))
        .thenThrow(AGENCY_SERVICE_HELPER_EXCEPTION);

    try {
      agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT);
      fail("Expected exception: ServiceException");
    } catch (ServiceException serviceException) {
      assertTrue("Excepted ServiceException thrown", true);
    }
  }

  @Test
  public void getVerifiedAgency_Should_ThrowBadRequestException_When_AgencyIsNotAssignedToGivenConsultingType() {

    when(agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    try {
      agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_KREUZBUND);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  @Test
  public void getVerifiedAgency_Should_ReturnCorrectAgency_When_AgencyIsFoundAndValid() {

    when(agencyServiceHelper.getAgencyWithoutCaching(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    AgencyDTO agency = agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT);

    assertEquals(AGENCY_ID, agency.getId());
  }

  /**
   * Method: doesConsultingTypeMatchToAgency
   */

  @Test
  public void doesConsultingTypeMatchToAgency_Should_ReturnTrue_When_AgencyIsAssignedToGivenConsultingType() {

    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_SUCHT);

    boolean response =
        agencyHelper.doesConsultingTypeMatchToAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT);

    assertTrue(response);
  }

  @Test
  public void doesConsultingTypeMatchToAgency_Should_ReturnFalse_When_AgencyIsNotAssignedToGivenConsultingType() {

    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT)).thenReturn(null);

    boolean response =
        agencyHelper.doesConsultingTypeMatchToAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT);

    assertFalse(response);
  }
}

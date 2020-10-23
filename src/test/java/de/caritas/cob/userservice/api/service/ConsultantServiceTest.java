package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_EMPTY_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_HTML_AND_JS;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_NULL_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_AGENCIES;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_ENCODED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ConsultantServiceTest {

  @MockBean
  private ConsultantRepository consultantRepository;
  @MockBean
  private UserHelper userHelper;
  @Mock
  private AuthenticatedUser authenticatedUser;

  private ConsultantService consultantService;

  @Before
  public void setUp() {
    this.consultantService = new ConsultantService(consultantRepository, userHelper);
  }

  @Test
  public void updateConsultantAbsent_Should_UpdateAbsenceMessageAndIsAbsence() {
    when(consultantService.saveConsultant(Mockito.any(Consultant.class))).thenReturn(CONSULTANT);
    Consultant consultant = consultantService.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO);

    Assert.assertEquals(consultant.getAbsenceMessage(), ABSENCE_DTO.getMessage());
    Assert.assertEquals(consultant.isAbsent(), ABSENCE_DTO.getAbsent());
  }

  @Test
  public void saveEnquiryMessageAndRocketChatGroupId_Should_RemoveHtmlCodeAndJsFromMessageForXssProtection() {
    when(consultantService.saveConsultant(Mockito.any(Consultant.class))).thenReturn(CONSULTANT);
    Consultant consultant =
        consultantService.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO_WITH_HTML_AND_JS);

    Assert.assertEquals(consultant.isAbsent(), ABSENCE_DTO_WITH_HTML_AND_JS.getAbsent());
    Assert.assertNotEquals(consultant.getAbsenceMessage(),
        ABSENCE_DTO_WITH_HTML_AND_JS.getMessage());
    Assert.assertEquals(MESSAGE, consultant.getAbsenceMessage());
  }

  @Test
  public void updateConsultantAbsent_Should_SetAbsenceMessageToNull_WhenAbsenceMessageFromDtoIsEmpty() {

    Consultant consultant = Mockito.mock(Consultant.class);
    consultantService.updateConsultantAbsent(consultant, ABSENCE_DTO_WITH_EMPTY_MESSAGE);

    verify(consultant, times(1)).setAbsenceMessage(null);

  }

  @Test
  public void updateConsultantAbsent_Should_SetAbsenceMessageToNull_WhenAbsenceMessageFromDtoIsNull() {

    Consultant consultant = Mockito.mock(Consultant.class);
    consultantService.updateConsultantAbsent(consultant, ABSENCE_DTO_WITH_NULL_MESSAGE);

    verify(consultant, times(1)).setAbsenceMessage(null);

  }

  @Test
  public void saveConsultant_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(consultantRepository.save(Mockito.any())).thenThrow(ex);

    try {
      consultantService.saveConsultant(CONSULTANT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultant_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(consultantRepository.findById(CONSULTANT_ID)).thenThrow(ex);

    try {
      consultantService.getConsultant(CONSULTANT_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultant_Should_ReturnConsultantWhenFound() {

    when(consultantRepository.findById(CONSULTANT_ID)).thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result = consultantService.getConsultant(CONSULTANT_ID);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void getConsultantByRcUserId_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(consultantRepository.findByRocketChatId(RC_USER_ID)).thenThrow(ex);

    try {
      consultantService.getConsultantByRcUserId(RC_USER_ID);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultantByRcUserId_Should_ReturnConsultantWhenFound() {

    when(consultantRepository.findByRocketChatId(RC_USER_ID)).thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result = consultantService.getConsultantByRcUserId(RC_USER_ID);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void getConsultantByEmail_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(consultantRepository.findByEmail(EMAIL)).thenThrow(ex);

    try {
      consultantService.getConsultantByEmail(EMAIL);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultantByEmail_Should_ReturnConsultant_WhenFound() {

    when(consultantRepository.findByEmail(EMAIL)).thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result = consultantService.getConsultantByEmail(EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void getConsultantByUsername_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(consultantRepository.findByUsername(USERNAME)).thenThrow(ex);

    try {
      consultantService.getConsultantByUsername(USERNAME);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultantByUsername_Should_ReturnConsultant_WhenFound() {

    when(consultantRepository.findByUsername(USERNAME)).thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result = consultantService.getConsultantByUsername(USERNAME);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void findConsultantByUsernameOrEmail_Should_ReturnEmptyOptional_WhenConsultantIsNotFound() {

    when(consultantRepository.findByUsername(USERNAME_DECODED)).thenReturn(Optional.empty());
    when(userHelper.encodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_ENCODED);
    when(consultantRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    Optional<Consultant> result =
        consultantService.findConsultantByUsernameOrEmail(USERNAME_DECODED, EMAIL);

    assertFalse(result.isPresent());

  }

  @Test
  public void findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByDecodedUsername() {

    when(consultantRepository.findByUsername(USERNAME_DECODED)).thenReturn(Optional.of(CONSULTANT));
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);
    when(userHelper.encodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_ENCODED);
    when(consultantRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    Optional<Consultant> result =
        consultantService.findConsultantByUsernameOrEmail(USERNAME_ENCODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByEncodedUsername() {

    when(consultantRepository.findByUsername(USERNAME_DECODED)).thenReturn(Optional.empty());
    when(consultantRepository.findByUsername(USERNAME_ENCODED)).thenReturn(Optional.of(CONSULTANT));
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);
    when(userHelper.encodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_ENCODED);
    when(consultantRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    Optional<Consultant> result =
        consultantService.findConsultantByUsernameOrEmail(USERNAME_DECODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByEmail() {

    when(consultantRepository.findByUsername(USERNAME_DECODED)).thenReturn(Optional.empty());
    when(consultantRepository.findByUsername(USERNAME_ENCODED)).thenReturn(Optional.empty());
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);
    when(userHelper.encodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_ENCODED);
    when(consultantRepository.findByEmail(EMAIL)).thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result =
        consultantService.findConsultantByUsernameOrEmail(USERNAME_ENCODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void getConsultantViaAuthenticatedUser_Should_ThrowInternalServerError_WhenConsultantIsNotFound() {

    when(consultantRepository.findById(CONSULTANT_ID)).thenReturn(Optional.empty());
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    try {
      consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void getConsultantViaAuthenticatedUser_Should_ReturnConsultantOptional() {

    when(consultantRepository.findById(CONSULTANT_ID)).thenReturn(Optional.of(CONSULTANT));
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    Optional<Consultant> result =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  /**
   * 
   * Method: findConsultantsByAgencyIds
   * 
   */
  @Test
  public void findConsultantsByAgencyIds_Should_ThrowInternalServerErrorException_WhenDatabaseFails() {

    @SuppressWarnings("serial")
    DataAccessException ex = new DataAccessException(ERROR) {};
    when(consultantRepository.findByConsultantAgenciesAgencyIdIn(Mockito.any())).thenThrow(ex);

    try {
      consultantService.findConsultantsByAgencyIds(CHAT_AGENCIES);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void findConsultantsByAgencyIds_Should_ReturnListOfConsultants() {

    when(consultantRepository.findByConsultantAgenciesAgencyIdIn(Mockito.any()))
        .thenReturn(Collections.singletonList(CONSULTANT));

    List<Consultant> result = consultantService.findConsultantsByAgencyIds(CHAT_AGENCIES);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(CONSULTANT, result.get(0));

  }

}

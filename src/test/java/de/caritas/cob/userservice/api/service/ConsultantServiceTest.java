package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_EMPTY_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_HTML_AND_JS;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_NULL_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_AGENCIES;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_ENCODED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ConsultantServiceTest {

  @InjectMocks
  private ConsultantService consultantService;

  @Mock
  private ConsultantRepository consultantRepository;

  @Mock
  private UserHelper userHelper;

  @Mock
  private ValidatedUserAccountProvider validatedUserAccountProvider;

  @Mock
  private AuthenticatedUser authenticatedUser;

  @Test
  public void getConsultant_Should_ReturnConsultantWhenFound() {
    when(consultantRepository.findByIdAndDeleteDateIsNull(CONSULTANT_ID))
        .thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result = consultantService.getConsultant(CONSULTANT_ID);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void getConsultantByRcUserId_Should_ReturnConsultantWhenFound() {
    when(consultantRepository.findByRocketChatIdAndDeleteDateIsNull(RC_USER_ID))
        .thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result = consultantService.getConsultantByRcUserId(RC_USER_ID);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void getConsultantByEmail_Should_ReturnConsultant_WhenFound() {
    when(consultantRepository.findByEmailAndDeleteDateIsNull(EMAIL))
        .thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result = consultantService.getConsultantByEmail(EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void getConsultantByUsername_Should_ReturnConsultant_WhenFound() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME))
        .thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result = consultantService.getConsultantByUsername(USERNAME);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void findConsultantByUsernameOrEmail_Should_ReturnEmptyOptional_WhenConsultantIsNotFound() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_DECODED))
        .thenReturn(Optional.empty());
    when(userHelper.encodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_ENCODED);
    when(consultantRepository.findByEmailAndDeleteDateIsNull(EMAIL)).thenReturn(Optional.empty());

    Optional<Consultant> result =
        consultantService.findConsultantByUsernameOrEmail(USERNAME_DECODED, EMAIL);

    assertFalse(result.isPresent());

  }

  @Test
  public void findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByDecodedUsername() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_DECODED))
        .thenReturn(Optional.of(CONSULTANT));
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);
    when(userHelper.encodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_ENCODED);
    when(consultantRepository.findByEmailAndDeleteDateIsNull(EMAIL)).thenReturn(Optional.empty());

    Optional<Consultant> result =
        consultantService.findConsultantByUsernameOrEmail(USERNAME_ENCODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByEncodedUsername() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_DECODED))
        .thenReturn(Optional.empty());
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_ENCODED))
        .thenReturn(Optional.of(CONSULTANT));
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);
    when(userHelper.encodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_ENCODED);
    when(consultantRepository.findByEmailAndDeleteDateIsNull(EMAIL)).thenReturn(Optional.empty());

    Optional<Consultant> result =
        consultantService.findConsultantByUsernameOrEmail(USERNAME_DECODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByEmail() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_DECODED))
        .thenReturn(Optional.empty());
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_ENCODED))
        .thenReturn(Optional.empty());
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);
    when(userHelper.encodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_ENCODED);
    when(consultantRepository.findByEmailAndDeleteDateIsNull(EMAIL))
        .thenReturn(Optional.of(CONSULTANT));

    Optional<Consultant> result =
        consultantService.findConsultantByUsernameOrEmail(USERNAME_ENCODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void getConsultantViaAuthenticatedUser_Should_returnEmptyOptional_When_ConsultantIsNotFound() {
    when(consultantRepository.findByIdAndDeleteDateIsNull(CONSULTANT_ID))
        .thenReturn(Optional.empty());
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    Optional<Consultant> viaAuthenticatedUser = consultantService
        .getConsultantViaAuthenticatedUser(authenticatedUser);

    assertThat(viaAuthenticatedUser, is(Optional.empty()));
  }

  @Test
  public void getConsultantViaAuthenticatedUser_Should_ReturnConsultantOptional() {
    when(consultantRepository.findByIdAndDeleteDateIsNull(CONSULTANT_ID))
        .thenReturn(Optional.of(CONSULTANT));
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    Optional<Consultant> result =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());

  }

  @Test
  public void findConsultantsByAgencyIds_Should_ReturnListOfConsultants() {
    when(consultantRepository.findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(Mockito.any()))
        .thenReturn(Collections.singletonList(CONSULTANT));

    List<Consultant> result = consultantService.findConsultantsByAgencyIds(CHAT_AGENCIES);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(CONSULTANT, result.get(0));

  }

}

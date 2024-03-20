package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_AGENCIES;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_ENCODED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantMobileToken;
import de.caritas.cob.userservice.api.port.out.ConsultantMobileTokenRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.service.user.UserAccountService;
import java.util.Collections;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultantServiceTest {

  @InjectMocks private ConsultantService consultantService;

  @Mock private ConsultantRepository consultantRepository;

  @Mock private ConsultantMobileTokenRepository consultantMobileTokenRepository;

  @Mock private UserAccountService userAccountService;

  @Mock private AuthenticatedUser authenticatedUser;

  @Test
  void getConsultant_Should_ReturnConsultantWhenFound() {
    when(consultantRepository.findByIdAndDeleteDateIsNull(CONSULTANT_ID))
        .thenReturn(Optional.of(CONSULTANT));

    var result = consultantService.getConsultant(CONSULTANT_ID);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());
  }

  @Test
  void getConsultantByRcUserId_Should_ReturnConsultantWhenFound() {
    when(consultantRepository.findByRocketChatIdAndDeleteDateIsNull(RC_USER_ID))
        .thenReturn(Optional.of(CONSULTANT));

    var result = consultantService.getConsultantByRcUserId(RC_USER_ID);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());
  }

  @Test
  void getConsultantByEmail_Should_ReturnConsultant_WhenFound() {
    when(consultantRepository.findByEmailAndDeleteDateIsNull(EMAIL))
        .thenReturn(Optional.of(CONSULTANT));

    var result = consultantService.findConsultantByEmail(EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());
  }

  @Test
  void getConsultantByUsername_Should_ReturnConsultant_WhenFound() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME))
        .thenReturn(Optional.of(CONSULTANT));

    var result = consultantService.getConsultantByUsername(USERNAME);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());
  }

  @Test
  void findConsultantByUsernameOrEmail_Should_ReturnEmptyOptional_WhenConsultantIsNotFound() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_DECODED))
        .thenReturn(Optional.empty());
    when(consultantRepository.findByEmailAndDeleteDateIsNull(EMAIL)).thenReturn(Optional.empty());

    var result = consultantService.findConsultantByUsernameOrEmail(USERNAME_DECODED, EMAIL);

    assertFalse(result.isPresent());
  }

  @Test
  void
      findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByDecodedUsername() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_DECODED))
        .thenReturn(Optional.of(CONSULTANT));

    var result = consultantService.findConsultantByUsernameOrEmail(USERNAME_ENCODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());
  }

  @Test
  void
      findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByEncodedUsername() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_DECODED))
        .thenReturn(Optional.empty());
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_ENCODED))
        .thenReturn(Optional.of(CONSULTANT));

    var result = consultantService.findConsultantByUsernameOrEmail(USERNAME_DECODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());
  }

  @Test
  void
      findConsultantByUsernameOrEmail_Should_ReturnConsultantOptional_WhenConsultantIsFoundByEmail() {
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_DECODED))
        .thenReturn(Optional.empty());
    when(consultantRepository.findByUsernameAndDeleteDateIsNull(USERNAME_ENCODED))
        .thenReturn(Optional.empty());
    when(consultantRepository.findByEmailAndDeleteDateIsNull(EMAIL))
        .thenReturn(Optional.of(CONSULTANT));

    var result = consultantService.findConsultantByUsernameOrEmail(USERNAME_ENCODED, EMAIL);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());
  }

  @Test
  void getConsultantViaAuthenticatedUser_Should_returnEmptyOptional_When_ConsultantIsNotFound() {
    when(consultantRepository.findByIdAndDeleteDateIsNull(CONSULTANT_ID))
        .thenReturn(Optional.empty());
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    var viaAuthenticatedUser =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    assertThat(viaAuthenticatedUser, is(Optional.empty()));
  }

  @Test
  void getConsultantViaAuthenticatedUser_Should_ReturnConsultantOptional() {
    when(consultantRepository.findByIdAndDeleteDateIsNull(CONSULTANT_ID))
        .thenReturn(Optional.of(CONSULTANT));
    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);

    var result = consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    assertTrue(result.isPresent());
    assertEquals(CONSULTANT, result.get());
  }

  @Test
  void findConsultantsByAgencyIds_Should_ReturnListOfConsultants() {
    when(consultantRepository.findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(anyList()))
        .thenReturn(Collections.singletonList(CONSULTANT));

    var result = consultantService.findConsultantsByAgencyIds(CHAT_AGENCIES);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(CONSULTANT, result.get(0));
  }

  @Test
  void findConsultantsByAgencyId_Should_ReturnListOfConsultants() {
    when(consultantRepository.findByConsultantAgenciesAgencyIdAndDeleteDateIsNull(any()))
        .thenReturn(Collections.singletonList(CONSULTANT));

    var result = consultantService.findConsultantsByAgencyId(AGENCY_ID);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(CONSULTANT, result.get(0));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void addMobileAppToken_Should_callNoOtherMethods_When_mobileTokenIsNullOrEmpty(String token) {
    consultantService.addMobileAppToken(null, token);

    verifyNoMoreInteractions(consultantMobileTokenRepository);
    verifyNoMoreInteractions(consultantRepository);
  }

  @Test
  void addMobileAppToken_Should_callNoOtherMethods_When_consultantDoesNotExist() {
    when(consultantRepository.findByIdAndDeleteDateIsNull(any())).thenReturn(Optional.empty());

    consultantService.addMobileAppToken("id", "token");

    verifyNoMoreInteractions(consultantMobileTokenRepository);
    verifyNoMoreInteractions(consultantRepository);
  }

  @Test
  void addMobileAppToken_Should_addMobileTokenToConsultant_When_consultantExists() {
    var consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.getConsultantMobileTokens().clear();
    when(consultantRepository.findByIdAndDeleteDateIsNull(any()))
        .thenReturn(Optional.of(consultant));

    consultantService.addMobileAppToken("id", "token");

    verify(consultantMobileTokenRepository, times(1)).findByMobileAppToken("token");
    verify(consultantMobileTokenRepository, times(1)).save(any());
    assertThat(consultant.getConsultantMobileTokens(), hasSize(1));
  }

  @Test
  void addMobileAppToken_Should_throwConflictException_When_tokenAlreadyExists() {
    var consultant = new EasyRandom().nextObject(Consultant.class);
    when(consultantRepository.findByIdAndDeleteDateIsNull(any()))
        .thenReturn(Optional.of(consultant));
    when(consultantMobileTokenRepository.findByMobileAppToken(any()))
        .thenReturn(Optional.of(new ConsultantMobileToken()));

    assertThrows(ConflictException.class, () -> consultantService.addMobileAppToken("id", "token"));
  }

  @Test
  void getNumberOfActiveConsultants_Should_ReturnNumberOfActiveConsultants() {
    when(consultantRepository.countByDeleteDateIsNull()).thenReturn(1L);

    var result = consultantService.getNumberOfActiveConsultants();

    verify(consultantRepository).countByDeleteDateIsNull();
    assertEquals(1L, result);
  }

  @Test
  void getNumberOfActiveConsultantsByTenantId_Should_ReturnNumberOfActiveConsultants() {
    when(consultantRepository.countByTenantIdAndDeleteDateIsNull(1L)).thenReturn(1L);

    var result = consultantService.getNumberOfActiveConsultants(1L);

    verify(consultantRepository).countByTenantIdAndDeleteDateIsNull(1L);
    assertEquals(1L, result);
  }
}

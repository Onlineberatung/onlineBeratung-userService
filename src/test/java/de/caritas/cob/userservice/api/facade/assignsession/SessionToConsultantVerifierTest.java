package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_U25;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.User;
import org.assertj.core.api.Fail;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionToConsultantVerifierTest {

  @InjectMocks private SessionToConsultantVerifier sessionToConsultantVerifier;

  @Mock private SessionToConsultantConditionProvider sessionToConsultantConditionProvider;

  @Test
  void verifySessionIsNotInProgress_Should_throwConflict_When_SessionIsInProgress() {
    when(sessionToConsultantConditionProvider.isSessionInProgress(any())).thenReturn(true);

    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder()
            .consultant(mock(Consultant.class))
            .session(mock(Session.class))
            .build();

    assertThrows(
        ConflictException.class,
        () -> sessionToConsultantVerifier.verifySessionIsNotInProgress(consultantSessionDTO));
  }

  @Test
  void verifyPreconditionsForAssignment_Should_throwException_When_sessionIsNew() {
    when(sessionToConsultantConditionProvider.isNewSession(any())).thenReturn(true);
    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder()
            .consultant(mock(Consultant.class))
            .session(mock(Session.class))
            .build();

    assertThrows(
        ConflictException.class,
        () -> sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO));
  }

  @Test
  void
      verifyPreconditionsForAssignment_Should_throwException_When_sessionIsAlreadyAssignedToConsultant() {
    when(sessionToConsultantConditionProvider.isSessionAlreadyAssignedToConsultant(any(), any()))
        .thenReturn(true);
    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder()
            .consultant(mock(Consultant.class))
            .session(mock(Session.class))
            .build();

    assertThrows(
        ConflictException.class,
        () -> sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO));
  }

  @Test
  void
      verifyPreconditionsForAssignment_Should_Not_throwException_When_sessionIsAlreadyAssignedToConsultantButSkipSameConsultantVerificationIsEnabled() {
    //    when(sessionToConsultantConditionProvider.isSessionAlreadyAssignedToConsultant(any(),
    // any()))
    //        .thenReturn(true);
    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder()
            .consultant(mock(Consultant.class))
            .session(mock(Session.class))
            .build();

    try {
      sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO, true);
    } catch (Exception ex) {
      Fail.fail("exception was not expected");
    }
  }

  @Test
  void
      verifyPreconditionsForAssignment_Should_throwException_When_consultantDoesNotHaveRocketChatIdInDb() {
    when(sessionToConsultantConditionProvider.hasConsultantNoRcId(any())).thenReturn(true);
    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder()
            .consultant(mock(Consultant.class))
            .session(mock(Session.class))
            .build();

    assertThrows(
        InternalServerErrorException.class,
        () -> sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO));
  }

  @Test
  void verifyPreconditionsForAssignment_Should_throwException_When_sessionUserHasNoRocketChatId() {
    when(sessionToConsultantConditionProvider.hasSessionUserNoRcId(any())).thenReturn(true);

    Session sessionWithUser = mock(Session.class);
    User user = mock(User.class);
    when(sessionWithUser.getUser()).thenReturn(user);

    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder()
            .consultant(mock(Consultant.class))
            .session(sessionWithUser)
            .build();

    assertThrows(
        InternalServerErrorException.class,
        () -> sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO));
  }

  @Test
  void verifyPreconditionsForAssignment_Should_notThrowException_When_anonymousSessionIsValid() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setConsultant(null);
    session.setConsultingTypeId(CONSULTING_TYPE_ID_U25);
    session.setRegistrationType(RegistrationType.ANONYMOUS);
    ConsultantAgency u25ConsultantAgency = mock(ConsultantAgency.class);
    ConsultantAgency otherConsultantAgency = mock(ConsultantAgency.class);
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setConsultantAgencies(asSet(u25ConsultantAgency, otherConsultantAgency));

    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder().consultant(consultant).session(session).build();

    assertDoesNotThrow(
        () -> sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO));
  }

  @Test
  void
      verifyPreconditionsForAssignment_Should_throwException_When_anonymousSessionHasNotConsultingType() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setConsultant(null);
    session.setConsultingTypeId(CONSULTING_TYPE_ID_U25);
    session.setRegistrationType(RegistrationType.ANONYMOUS);
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    consultant.setConsultantAgencies(null);

    when(sessionToConsultantConditionProvider.isSessionsConsultingTypeNotAvailableForConsultant(
            any(), any()))
        .thenReturn(true);

    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder().consultant(consultant).session(session).build();

    assertThrows(
        ForbiddenException.class,
        () -> sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO));
  }

  @Test
  void
      verifyPreconditionsForAssignment_Should_throwException_When_consultantIsNotAssignedToCorrectAgency() {
    Session session = mock(Session.class);
    when(session.getRegistrationType()).thenReturn(RegistrationType.REGISTERED);

    when(sessionToConsultantConditionProvider.isSessionsAgencyNotAvailableInConsultantAgencies(
            any(), any()))
        .thenReturn(true);

    ConsultantSessionDTO consultantSessionDTO =
        ConsultantSessionDTO.builder().consultant(mock(Consultant.class)).session(session).build();

    assertThrows(
        ForbiddenException.class,
        () -> sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO));
  }
}

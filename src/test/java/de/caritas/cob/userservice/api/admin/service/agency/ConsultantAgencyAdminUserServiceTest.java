package de.caritas.cob.userservice.api.admin.service.agency;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConsultantAgencyAdminUserServiceTest {

  @InjectMocks private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Mock private ConsultantAgencyRepository consultantAgencyRepository;

  @Mock private ConsultantRepository consultantRepository;

  @Mock private SessionRepository sessionRepository;

  @Mock private RemoveConsultantFromRocketChatService removeFromRocketChatService;

  @Mock private AgencyService agencyService;

  @Mock private AgencyAdminService agencyAdminService;

  @Mock private ConsultantAgencyDeletionValidationService agencyDeletionValidationService;

  @Test
  public void
      markAllAssignedConsultantsAsTeamConsultant_Should_notThrowNotFoundException_When_agencyWithIdDoesNotExist() {
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(any()))
        .thenReturn(Collections.emptyList());

    assertDoesNotThrow(
        () -> this.consultantAgencyAdminService.markAllAssignedConsultantsAsTeamConsultant(1L));
  }

  @Test
  public void
      markAllAssignedConsultantsAsTeamConsultant_Should_setFlagTeamConsultantToAllNotAlreadyTeamConsultants() {
    List<ConsultantAgency> consultantAgencies =
        new EasyRandom().objects(ConsultantAgency.class, 10).collect(Collectors.toList());
    long teamConsultants =
        consultantAgencies.stream()
            .map(ConsultantAgency::getConsultant)
            .filter(Consultant::isTeamConsultant)
            .count();

    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(any()))
        .thenReturn(consultantAgencies);

    this.consultantAgencyAdminService.markAllAssignedConsultantsAsTeamConsultant(1L);

    verify(this.consultantRepository, times((int) (10 - teamConsultants))).save(any());
  }

  @Test
  public void removeConsultantsFromTeamSessionsByAgencyId_Should_changeTeamSessionToSession() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(true);

    when(this.sessionRepository.findByAgencyIdAndStatusAndTeamSessionIsTrue(any(), any()))
        .thenReturn(singletonList(session));

    this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(1L);

    verify(this.sessionRepository, times(1)).save(any());
    verify(this.removeFromRocketChatService, times(1)).removeConsultantFromSessions(any());
  }

  @Test
  public void
      removeConsultantsFromTeamSessionsByAgencyId_Should_removeTeamConsultingFlag_When_theyAreNotInAnotherTeamAgency() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(true);
    List<Consultant> consultants =
        new EasyRandom().objects(Consultant.class, 10).collect(Collectors.toList());
    AgencyDTO agencyDTO = new AgencyDTO().id(1L).teamAgency(false);

    when(this.sessionRepository.findByAgencyIdAndStatusAndTeamSessionIsTrue(any(), any()))
        .thenReturn(singletonList(session));
    when(this.consultantRepository.findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(anyList()))
        .thenReturn(consultants);
    when(this.agencyService.getAgency(any())).thenReturn(agencyDTO);

    this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(1L);

    verify(this.consultantRepository, times(10)).save(any());
  }

  @Test
  public void
      removeConsultantsFromTeamSessionsByAgencyId_Should_throwInternalServerErrorException_When_agencyServiceFails() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          Session session = new EasyRandom().nextObject(Session.class);
          session.setTeamSession(true);
          List<Consultant> consultants =
              new EasyRandom().objects(Consultant.class, 10).collect(Collectors.toList());

          when(this.sessionRepository.findByAgencyIdAndStatusAndTeamSessionIsTrue(any(), any()))
              .thenReturn(singletonList(session));
          when(this.consultantRepository.findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(
                  anyList()))
              .thenReturn(consultants);
          when(this.agencyService.getAgency(any())).thenThrow(new InternalServerErrorException(""));

          this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(1L);
        });
  }

  @Test
  public void
      markConsultantAgencyForDeletion_Should_throwCustomValidationHttpStatusException_When_relationDoesNotExist() {
    try {
      this.consultantAgencyAdminService.markConsultantAgencyForDeletion("", 1L);
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(
          requireNonNull(e.getCustomHttpHeaders().get("X-Reason")).iterator().next(),
          is(CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST.name()));
    }
  }

  @Test
  public void
      markConsultantAgencyForDeletion_Should_deleteConsultantAgency_When_consultantAgencyCanBeDeleted() {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setDeleteDate(null);
    when(this.consultantAgencyRepository.findByConsultantIdAndAgencyIdAndDeleteDateIsNull(
            any(), any()))
        .thenReturn(singletonList(consultantAgency));

    this.consultantAgencyAdminService.markConsultantAgencyForDeletion("", 1L);

    assertThat(consultantAgency.getDeleteDate(), notNullValue());
    verify(this.consultantAgencyRepository).save(any(ConsultantAgency.class));
    verify(this.agencyDeletionValidationService).validateAndMarkForDeletion(any());
  }
}

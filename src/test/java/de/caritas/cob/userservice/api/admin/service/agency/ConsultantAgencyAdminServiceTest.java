package de.caritas.cob.userservice.api.admin.service.agency;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_HAS_OPEN_ENQUIRIES;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_IS_STILL_ACTIVE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantAgencyAdminServiceTest {

  @InjectMocks
  private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Mock
  private ConsultantAgencyRepository consultantAgencyRepository;

  @Mock
  private ConsultantRepository consultantRepository;

  @Mock
  private SessionRepository sessionRepository;

  @Mock
  private RemoveConsultantFromRocketChatService removeFromRocketChatService;

  @Mock
  private AgencyServiceHelper agencyServiceHelper;

  @Test(expected = NotFoundException.class)
  public void markAllAssignedConsultantsAsTeamConsultant_Should_throwNotFoundException_When_agencyWithIdDoesNotExist() {
    when(this.consultantAgencyRepository.findByAgencyId(any())).thenReturn(Collections.emptyList());

    this.consultantAgencyAdminService.markAllAssignedConsultantsAsTeamConsultant(1L);
  }

  @Test
  public void markAllAssignedConsultantsAsTeamConsultant_Should_setFlagTeamConsultantToAllNotAlreadyTeamConsultants() {
    List<ConsultantAgency> consultantAgencies = new EasyRandom()
        .objects(ConsultantAgency.class, 10)
        .collect(Collectors.toList());
    long teamConsultants = consultantAgencies.stream()
        .map(ConsultantAgency::getConsultant)
        .filter(Consultant::isTeamConsultant)
        .count();

    when(this.consultantAgencyRepository.findByAgencyId(any())).thenReturn(consultantAgencies);

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
  public void removeConsultantsFromTeamSessionsByAgencyId_Should_removeTeamConsultingFlag_When_theyAreNotInAnotherTeamAgency()
      throws AgencyServiceHelperException {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(true);
    List<Consultant> consultants = new EasyRandom()
        .objects(Consultant.class, 10)
        .collect(Collectors.toList());
    AgencyDTO agencyDTO = new AgencyDTO()
        .id(1L)
        .teamAgency(false);

    when(this.sessionRepository.findByAgencyIdAndStatusAndTeamSessionIsTrue(any(), any()))
        .thenReturn(singletonList(session));
    when(this.consultantRepository.findByConsultantAgenciesAgencyIdIn(any()))
        .thenReturn(consultants);
    when(this.agencyServiceHelper.getAgency(any())).thenReturn(agencyDTO);

    this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(1L);

    verify(this.consultantRepository, times(10)).save(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void removeConsultantsFromTeamSessionsByAgencyId_Should_throwInternalServerErrorException_When_agencyServiceFails()
      throws AgencyServiceHelperException {
    Session session = new EasyRandom().nextObject(Session.class);
    session.setTeamSession(true);
    List<Consultant> consultants = new EasyRandom()
        .objects(Consultant.class, 10)
        .collect(Collectors.toList());

    when(this.sessionRepository.findByAgencyIdAndStatusAndTeamSessionIsTrue(any(), any()))
        .thenReturn(singletonList(session));
    when(this.consultantRepository.findByConsultantAgenciesAgencyIdIn(any()))
        .thenReturn(consultants);
    when(this.agencyServiceHelper.getAgency(any()))
        .thenThrow(new AgencyServiceHelperException(new Exception()));

    this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(1L);
  }

  @Test
  public void markConsultantAgencyForDeletion_Should_throwCustomValidationHttpStatusException_When_relationDoesNotExist() {
    try {
      this.consultantAgencyAdminService.markConsultantAgencyForDeletion("", 1L);
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(requireNonNull(e.getCustomHttpHeader().get("X-Reason")).iterator().next(),
          is(CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST.name()));
    }
  }

  @Test
  public void markConsultantAgencyForDeletion_Should_throwCustomValidationHttpStatusException_When_consultantIsTheLastOfTheAgencyAndAgencyIsStillOnline()
      throws AgencyServiceHelperException {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setDeleteDate(null);
    when(this.consultantAgencyRepository.findByConsultantIdAndAgencyId(any(), any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.consultantAgencyRepository.findByAgencyId(any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.agencyServiceHelper.getAgency(any())).thenReturn(new AgencyDTO().offline(false));

    try {
      this.consultantAgencyAdminService.markConsultantAgencyForDeletion("", 1L);
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(requireNonNull(e.getCustomHttpHeader().get("X-Reason")).iterator().next(),
          is(CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_IS_STILL_ACTIVE.name()));
    }
  }

  @Test(expected = InternalServerErrorException.class)
  public void markConsultantAgencyForDeletion_Should_throwInternalServerErrorException_When_agencyCanNotBefetched()
      throws AgencyServiceHelperException {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setDeleteDate(null);
    when(this.consultantAgencyRepository.findByConsultantIdAndAgencyId(any(), any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.consultantAgencyRepository.findByAgencyId(any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.agencyServiceHelper.getAgency(any()))
        .thenThrow(new AgencyServiceHelperException(new Exception()));

    this.consultantAgencyAdminService.markConsultantAgencyForDeletion("", 1L);
  }

  @Test
  public void markConsultantAgencyForDeletion_Should_throwCustomValidationHttpStatusException_When_consultantIsTheLastOfTheAgencyAndAgencyHasOpenEnquiries()
      throws AgencyServiceHelperException {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setDeleteDate(null);
    when(this.consultantAgencyRepository.findByConsultantIdAndAgencyId(any(), any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.consultantAgencyRepository.findByAgencyId(any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.agencyServiceHelper.getAgency(any())).thenReturn(new AgencyDTO().offline(true));
    when(this.sessionRepository.findByAgencyIdAndStatusAndConsultantIsNull(any(), any()))
        .thenReturn(singletonList(mock(Session.class)));

    try {
      this.consultantAgencyAdminService.markConsultantAgencyForDeletion("", 1L);
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(requireNonNull(e.getCustomHttpHeader().get("X-Reason")).iterator().next(),
          is(CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_HAS_OPEN_ENQUIRIES.name()));
    }
  }

  @Test
  public void markConsultantAgencyForDeletion_Should_deleteConsultantAgency_When_consultantAgencyCanBeDeleted()
      throws AgencyServiceHelperException {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setDeleteDate(null);
    when(this.consultantAgencyRepository.findByConsultantIdAndAgencyId(any(), any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.consultantAgencyRepository.findByAgencyId(any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.agencyServiceHelper.getAgency(any())).thenReturn(new AgencyDTO().offline(true));
    when(this.sessionRepository.findByAgencyIdAndStatusAndConsultantIsNull(any(), any()))
        .thenReturn(emptyList());

    this.consultantAgencyAdminService.markConsultantAgencyForDeletion("", 1L);

    assertThat(consultantAgency.getDeleteDate(), notNullValue());
    verify(this.consultantAgencyRepository, times(1)).save(any(ConsultantAgency.class));
  }

}

package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
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

}

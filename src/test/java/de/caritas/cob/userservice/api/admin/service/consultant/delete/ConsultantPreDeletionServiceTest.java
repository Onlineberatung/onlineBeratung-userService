package de.caritas.cob.userservice.api.admin.service.consultant.delete;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_HAS_ACTIVE_SESSIONS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.admin.service.agency.ConsultantAgencyDeletionValidationService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantPreDeletionServiceTest {

  @InjectMocks
  private ConsultantPreDeletionService consultantPreDeletionService;

  @Mock
  private ConsultantAgencyDeletionValidationService validationService;

  @Mock
  private SessionRepository sessionRepository;

  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;

  @Test
  public void performPreDeletionSteps_Should_throwCustomValidationHttpStatusException_When_consultantHasOpenSessions() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(singletonList(mock(Session.class)));

    try {
      this.consultantPreDeletionService.performPreDeletionSteps(consultant);
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(requireNonNull(e.getCustomHttpHeader().get("X-Reason")).iterator().next(),
          is(CONSULTANT_HAS_ACTIVE_SESSIONS.name()));
    }
  }

  @Test
  public void performPreDeletionSteps_Should_executeValidationForAllAgencyRelations_When_consultantIsAssignedToAgencies() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.sessionRepository.findByConsultantAndStatus(any(), any())).thenReturn(emptyList());

    this.consultantPreDeletionService.performPreDeletionSteps(consultant);

    verify(this.validationService, times(consultant.getConsultantAgencies().size()))
        .validateForDeletion(any());
  }

  @Test
  public void performPreDeletionSteps_Should_setConsultantInactiveInKeycloak_When_consultantCanBeDeleted() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.sessionRepository.findByConsultantAndStatus(any(), any())).thenReturn(emptyList());

    this.consultantPreDeletionService.performPreDeletionSteps(consultant);

    verify(this.keycloakAdminClientService, times(1)).deactivateUser(eq(consultant.getId()));
  }

}

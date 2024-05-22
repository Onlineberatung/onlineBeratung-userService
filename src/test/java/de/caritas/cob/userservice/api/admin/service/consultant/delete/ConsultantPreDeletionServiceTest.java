package de.caritas.cob.userservice.api.admin.service.consultant.delete;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_HAS_ACTIVE_OR_ARCHIVE_SESSIONS;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_ARCHIVE;
import static de.caritas.cob.userservice.api.model.Session.SessionStatus.IN_PROGRESS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.admin.service.agency.ConsultantAgencyDeletionValidationService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConsultantPreDeletionServiceTest {

  private static final Boolean FORCE_DELETE_SESSIONS = false;

  @InjectMocks private ConsultantPreDeletionService consultantPreDeletionService;

  @Mock private ConsultantAgencyDeletionValidationService validationService;

  @Mock private SessionRepository sessionRepository;

  @Mock private KeycloakService keycloakService;

  @Test
  public void
      performPreDeletionSteps_Should_throwCustomValidationHttpStatusException_When_consultantHasOpenSessions() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.sessionRepository.findByConsultantAndStatusIn(
            consultant, Lists.newArrayList(IN_PROGRESS, IN_ARCHIVE)))
        .thenReturn(singletonList(mock(Session.class)));

    try {
      this.consultantPreDeletionService.performPreDeletionSteps(consultant, FORCE_DELETE_SESSIONS);
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(
          requireNonNull(e.getCustomHttpHeaders().get("X-Reason")).iterator().next(),
          is(CONSULTANT_HAS_ACTIVE_OR_ARCHIVE_SESSIONS.name()));
    }
  }

  @Test
  public void
      performPreDeletionSteps_Should_executeValidationForAllAgencyRelations_When_consultantIsAssignedToAgencies() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.sessionRepository.findByConsultantAndStatusIn(any(), any())).thenReturn(emptyList());

    this.consultantPreDeletionService.performPreDeletionSteps(consultant, FORCE_DELETE_SESSIONS);

    verify(this.validationService, times(consultant.getConsultantAgencies().size()))
        .validateAndMarkForDeletion(any());
  }

  @Test
  public void
      performPreDeletionSteps_Should_setConsultantInactiveInKeycloak_When_consultantCanBeDeleted() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.sessionRepository.findByConsultantAndStatusIn(any(), any())).thenReturn(emptyList());

    this.consultantPreDeletionService.performPreDeletionSteps(consultant, FORCE_DELETE_SESSIONS);

    verify(this.keycloakService, times(1)).deactivateUser(consultant.getId());
  }
}

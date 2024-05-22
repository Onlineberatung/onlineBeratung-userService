package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import de.caritas.cob.userservice.api.AccountManager;
import de.caritas.cob.userservice.api.admin.service.consultant.create.CreateConsultantSaga;
import de.caritas.cob.userservice.api.admin.service.consultant.delete.ConsultantPreDeletionService;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConsultantAdminServiceTest {

  @InjectMocks private ConsultantAdminService consultantAdminService;

  @Mock private ConsultantRepository consultantRepository;

  @Mock private CreateConsultantSaga createConsultantSaga;

  @Mock private ConsultantUpdateService consultantUpdateService;

  @Mock private ConsultantPreDeletionService consultantPreDeletionService;

  @Mock private AppointmentService appointmentService;

  @Mock private SessionRepository sessionRepository;

  @Mock private AuthenticatedUser authenticatedUser;

  @Mock private AccountManager accountManager;

  @Test
  public void
      markConsultantForDeletion_Should_throwNotFoundException_When_consultantdoesNotExist() {
    assertThrows(
        NotFoundException.class,
        () -> {
          when(this.consultantRepository.findByIdAndDeleteDateIsNull(any()))
              .thenReturn(Optional.empty());

          this.consultantAdminService.markConsultantForDeletion("id", false);
        });
  }

  @Test
  public void
      markConsultantForDeletion_Should_executePreDeletionStepsAndMarkConsultantAsDeleted_When_consultantExists() {
    Consultant consultant = mock(Consultant.class);
    when(this.consultantRepository.findByIdAndDeleteDateIsNull(any()))
        .thenReturn(Optional.of(consultant));

    this.consultantAdminService.markConsultantForDeletion("id", false);

    verify(this.consultantPreDeletionService, times(1)).performPreDeletionSteps(consultant, false);
    verify(consultant, times(1)).setDeleteDate(any());
    verify(this.consultantRepository, times(1)).save(consultant);
  }
}

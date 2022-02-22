package de.caritas.cob.userservice.api.admin.service.consultant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import de.caritas.cob.userservice.api.admin.service.consultant.create.ConsultantCreatorService;
import de.caritas.cob.userservice.api.admin.service.consultant.delete.ConsultantPreDeletionService;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantAdminServiceTest {

  @InjectMocks
  private ConsultantAdminService consultantAdminService;

  @Mock
  private ConsultantRepository consultantRepository;

  @Mock
  private ConsultantCreatorService consultantCreatorService;

  @Mock
  private ConsultantUpdateService consultantUpdateService;

  @Mock
  private ConsultantPreDeletionService consultantPreDeletionService;

  @Test(expected = NotFoundException.class)
  public void markConsultantForDeletion_Should_throwNotFoundException_When_consultantdoesNotExist() {
    when(this.consultantRepository.findByIdAndDeleteDateIsNull(any())).thenReturn(Optional.empty());

    this.consultantAdminService.markConsultantForDeletion("id");
  }

  @Test
  public void markConsultantForDeletion_Should_executePreDeletionStepsAndMarkConsultantAsDeleted_When_consultantExists() {
    Consultant consultant = mock(Consultant.class);
    when(this.consultantRepository.findByIdAndDeleteDateIsNull(any()))
        .thenReturn(Optional.of(consultant));

    this.consultantAdminService.markConsultantForDeletion("id");

    verify(this.consultantPreDeletionService, times(1)).performPreDeletionSteps(eq(consultant));
    verify(consultant, times(1)).setDeleteDate(any());
    verify(this.consultantRepository, times(1)).save(eq(consultant));
  }

}

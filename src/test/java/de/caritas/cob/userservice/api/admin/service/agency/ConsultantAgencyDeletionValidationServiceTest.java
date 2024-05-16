package de.caritas.cob.userservice.api.admin.service.agency;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_HAS_OPEN_ENQUIRIES;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_IS_STILL_ACTIVE;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConsultantAgencyDeletionValidationServiceTest {

  @InjectMocks private ConsultantAgencyDeletionValidationService agencyDeletionValidationService;

  @Mock private ConsultantAgencyRepository consultantAgencyRepository;

  @Mock private AgencyService agencyService;

  @Mock private SessionRepository sessionRepository;

  @Test
  public void
      validateForDeletion_Should_throwCustomValidationHttpStatusException_When_consultantIsTheLastOfTheAgencyAndAgencyIsStillOnline() {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setDeleteDate(null);
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.agencyService.getAgencyWithoutCaching(any()))
        .thenReturn(new AgencyDTO().offline(false));

    try {
      this.agencyDeletionValidationService.validateAndMarkForDeletion(consultantAgency);
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(
          requireNonNull(e.getCustomHttpHeaders().get("X-Reason")).iterator().next(),
          is(CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_IS_STILL_ACTIVE.name()));
    }
  }

  @Test
  public void
      validateForDeletion_Should_throwCustomValidationHttpStatusException_When_consultantIsTheLastOfTheAgencyAndAgencyHasOpenEnquiries() {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setDeleteDate(null);
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.agencyService.getAgencyWithoutCaching(any()))
        .thenReturn(new AgencyDTO().offline(true));
    when(this.sessionRepository.findByAgencyIdAndStatusAndConsultantIsNull(any(), any()))
        .thenReturn(singletonList(mock(Session.class)));

    try {
      this.agencyDeletionValidationService.validateAndMarkForDeletion(consultantAgency);
      fail("Exception was not thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(
          requireNonNull(e.getCustomHttpHeaders().get("X-Reason")).iterator().next(),
          is(CONSULTANT_IS_THE_LAST_OF_AGENCY_AND_AGENCY_HAS_OPEN_ENQUIRIES.name()));
    }
  }

  @Test
  public void
      validateForDeletion_Should_throwInternalServerErrorException_When_agencyCanNotBeFetched() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
          consultantAgency.setDeleteDate(null);
          when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(any()))
              .thenReturn(singletonList(consultantAgency));
          when(this.agencyService.getAgencyWithoutCaching(any()))
              .thenThrow(new InternalServerErrorException(""));

          this.agencyDeletionValidationService.validateAndMarkForDeletion(consultantAgency);
        });
  }

  @Test
  public void
      validateForDeletion_Should_notThrowAnyException_When_consultantAgencyIsValidForDeletion() {
    ConsultantAgency consultantAgency = new EasyRandom().nextObject(ConsultantAgency.class);
    consultantAgency.setDeleteDate(null);
    when(this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(any()))
        .thenReturn(singletonList(consultantAgency));
    when(this.agencyService.getAgencyWithoutCaching(any()))
        .thenReturn(new AgencyDTO().offline(true));

    assertDoesNotThrow(
        () -> this.agencyDeletionValidationService.validateAndMarkForDeletion(consultantAgency));
  }
}

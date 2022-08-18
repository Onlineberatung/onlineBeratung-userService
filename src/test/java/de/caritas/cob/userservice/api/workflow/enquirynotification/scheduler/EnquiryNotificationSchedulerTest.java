package de.caritas.cob.userservice.api.workflow.enquirynotification.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.workflow.enquirynotification.service.EnquiryNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnquiryNotificationSchedulerTest {

  @InjectMocks private EnquiryNotificationScheduler enquiryNotificationScheduler;

  @Mock private EnquiryNotificationService enquiryNotificationService;

  @Test
  void
      sendEmailNotificationsForOpenEnquiries_Should_callEnquiryNotificationService_When_featureIsEnabled() {
    setField(enquiryNotificationScheduler, "enquiryNotificationsEnabled", true);

    enquiryNotificationScheduler.sendEmailNotificationsForOpenEnquiries();

    verify(enquiryNotificationService).sendEmailNotificationsForOpenEnquiries();
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(booleans = false)
  void
      sendEmailNotificationsForOpenEnquiries_Should_notCallEnquiryNotificationService_When_featureToggleIsNullOrDisabled(
          Boolean enabled) {
    setField(enquiryNotificationScheduler, "enquiryNotificationsEnabled", enabled);

    enquiryNotificationScheduler.sendEmailNotificationsForOpenEnquiries();

    verifyNoInteractions(enquiryNotificationService);
  }
}

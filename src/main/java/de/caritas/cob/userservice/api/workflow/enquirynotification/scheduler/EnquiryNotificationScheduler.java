package de.caritas.cob.userservice.api.workflow.enquirynotification.scheduler;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.workflow.enquirynotification.service.EnquiryNotificationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduler email notifications of open enquiries. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnquiryNotificationScheduler {

  private final @NonNull EnquiryNotificationService enquiryNotificationService;

  @Value("${enquiry.open.notification.enabled}")
  private Boolean enquiryNotificationsEnabled;

  /** Entry method to build and send email notifications. */
  @Scheduled(cron = "${enquiry.open.notification.cron}")
  public void sendEmailNotificationsForOpenEnquiries() {
    try {
      log.info("Started sending email notifications for open enquiries");
      if (isTrue(enquiryNotificationsEnabled)) {
        enquiryNotificationService.sendEmailNotificationsForOpenEnquiries();
      }
    } finally {
      log.info("Completed sending email notifications for open enquiries");
    }
  }
}

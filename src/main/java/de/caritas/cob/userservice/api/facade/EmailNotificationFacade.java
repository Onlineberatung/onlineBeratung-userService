package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.EmailNotificationUtils.deserializeNotificationSettingsDTOOrDefaultIfNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ReassignmentNotificationDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.NotificationsAware;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggle;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggleService;
import de.caritas.cob.userservice.api.service.emailsupplier.AssignEnquiryEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewDirectEnquiryEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewEnquiryEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewFeedbackEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewMessageEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.ReassignmentConfirmationEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.ReassignmentRequestEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.TenantTemplateSupplier;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantData;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Facade for capsuling the mail notification via the MailService */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationFacade {

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  private final @NonNull MailService mailService;
  private final @NonNull SessionService sessionService;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull RocketChatService messageClient;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull IdentityClientConfig identityClientConfig;
  private final @NonNull NewEnquiryEmailSupplier newEnquiryEmailSupplier;
  private final @NonNull NewDirectEnquiryEmailSupplier newDirectEnquiryEmailSupplier;
  private final @NonNull AssignEnquiryEmailSupplier assignEnquiryEmailSupplier;
  private final @NonNull TenantTemplateSupplier tenantTemplateSupplier;

  private final @NonNull ReleaseToggleService releaseToggleService;

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  /**
   * Sends email notifications according to the corresponding consultant(s) when a new enquiry was
   * written.
   *
   * @param session the regarding session
   */
  @Async
  public void sendNewEnquiryEmailNotification(Session session, TenantData tenantData) {

    var sessionAlreadyAssignedToConsultant = nonNull(session.getConsultant());
    if (!sessionAlreadyAssignedToConsultant) {
      try {
        log.info(
            "Preparing to send NEW_ENQUIRY_EMAIL_NOTIFICATION email for session: {}",
            session.getId());
        TenantContext.setCurrentTenantData(tenantData);
        newEnquiryEmailSupplier.setCurrentSession(session);
        sendMailTasksToMailService(newEnquiryEmailSupplier);
        TenantContext.clear();
      } catch (Exception ex) {
        log.error(
            "EmailNotificationFacade error: Failed to send new enquiry notification for session {}.",
            session.getId(),
            ex);
      }
    }
  }

  @Async
  public void sendNewDirectEnquiryEmailNotification(
      String consultantId, Long agencyId, String postCode, TenantData tenantData) {
    log.info(
        "Preparing NEW_DIRECT_ENQUIRY_EMAIL_NOTIFICATION email to consultant ({}) in "
            + "agency ({})",
        consultantId,
        agencyId);

    try {
      TenantContext.setCurrentTenantData(tenantData);
      newDirectEnquiryEmailSupplier.setAgencyId(agencyId);
      newDirectEnquiryEmailSupplier.setConsultantId(consultantId);
      newDirectEnquiryEmailSupplier.setPostCode(postCode);
      sendMailTasksToMailService(newDirectEnquiryEmailSupplier);
      TenantContext.clear();
    } catch (Exception ex) {
      log.error("Failed to send NEW_DIRECT_ENQUIRY_EMAIL_NOTIFICATION", ex);
    }
  }

  private void sendMailTasksToMailService(EmailSupplier mailsToSend)
      throws RocketChatGetGroupMembersException {
    List<MailDTO> generatedMails = mailsToSend.generateEmails();
    if (isNotEmpty(generatedMails)) {
      MailsDTO mailsDTO = new MailsDTO().mails(generatedMails);
      log.info(
          "Sending email notifications with mailDTOs. MailSupplier class: {}",
          mailsToSend.getClass());
      mailService.sendEmailNotification(mailsDTO);
    }
  }

  /**
   * Sends email notifications according to the corresponding consultant(s) or asker when a new
   * message was written.
   *
   * @param rcGroupId the rocket chat group id
   * @param roles roles to decide the regarding recipients
   * @param userId the user id of initiating user
   */
  @Async
  @Transactional
  public void sendNewMessageNotification(
      String rcGroupId, Set<String> roles, String userId, TenantData tenantData) {
    TenantContext.setCurrentTenantData(tenantData);
    try {
      Session session = sessionService.getSessionByGroupIdAndUser(rcGroupId, userId, roles);
      EmailSupplier newMessageMails =
          NewMessageEmailSupplier.builder()
              .session(session)
              .rcGroupId(rcGroupId)
              .roles(roles)
              .userId(userId)
              .consultantAgencyService(consultantAgencyService)
              .consultingTypeManager(consultingTypeManager)
              .consultantService(consultantService)
              .applicationBaseUrl(applicationBaseUrl)
              .emailDummySuffix(identityClientConfig.getEmailDummySuffix())
              .tenantTemplateSupplier(tenantTemplateSupplier)
              .multiTenancyEnabled(multiTenancyEnabled)
              .messageClient(messageClient)
              .releaseToggleService(releaseToggleService)
              .build();
      sendMailTasksToMailService(newMessageMails);

    } catch (NotFoundException | ForbiddenException | BadRequestException getSessionException) {
      log.warn(
          "EmailNotificationFacade warning: Failed to get session for new message notification with Rocket.Chat group ID {} and user ID {}.",
          rcGroupId,
          userId,
          getSessionException);
    } catch (Exception ex) {
      log.error(
          "EmailNotificationFacade warning: Failed to send new message notification with Rocket.Chat group ID {} and user ID {}.",
          rcGroupId,
          userId,
          ex);
    }
    TenantContext.clear();
  }

  /**
   * Sends email notifications according to the corresponding consultant(s) when a new feedback
   * message was written.
   *
   * @param rcFeedbackGroupId group id of feedback chat
   * @param userId regarding user id
   */
  @Async
  public void sendNewFeedbackMessageNotification(
      String rcFeedbackGroupId, String userId, TenantData tenantData) {
    TenantContext.setCurrentTenantData(tenantData);
    try {
      Session session = sessionService.getSessionByFeedbackGroupId(rcFeedbackGroupId);
      EmailSupplier newFeedbackMessages =
          new NewFeedbackEmailSupplier(
              session,
              rcFeedbackGroupId,
              userId,
              applicationBaseUrl,
              consultantService,
              messageClient,
              rocketChatSystemUserId,
              identityClient);
      sendMailTasksToMailService(newFeedbackMessages);
    } catch (Exception e) {
      log.error(
          "EmailNotificationFacade error: List of members for rocket chat feedback group id {} is empty.",
          rcFeedbackGroupId,
          e);
    }
    TenantContext.clear();
  }

  /**
   * Sends an email notification to the consultant when an enquiry has been assigned to him by a
   * different consultant.
   *
   * @param receiverConsultant the target consultant
   * @param senderUserId the id of initiating user
   * @param askerUserName the name of the asker
   */
  @Async
  public void sendAssignEnquiryEmailNotification(
      Consultant receiverConsultant,
      String senderUserId,
      String askerUserName,
      TenantData tenantData) {
    TenantContext.setCurrentTenantData(tenantData);
    log.info(
        "Preparing to send ASSIGN_ENQUIRY_NOTIFICATION email to consultant: {}",
        receiverConsultant != null ? receiverConsultant.getId() : "No consultant selected");
    assignEnquiryEmailSupplier.setReceiverConsultant(receiverConsultant);
    assignEnquiryEmailSupplier.setSenderUserId(senderUserId);
    assignEnquiryEmailSupplier.setAskerUserName(askerUserName);
    try {
      sendMailTasksToMailService(assignEnquiryEmailSupplier);
    } catch (Exception exception) {
      log.error("EmailNotificationFacade error: ", exception);
    }
    TenantContext.clear();
  }

  @Async
  @Transactional
  public void sendReassignRequestNotification(String rcGroupId, TenantData tenantData) {
    TenantContext.setCurrentTenantData(tenantData);
    var session = sessionService.getSessionByGroupId(rcGroupId);
    var user = session.getUser();

    if (!shouldSendReassignmentNotificationForAdviceSeeker(user)) {
      log.info(
          "Not sending email notification about reassignment because adviceseeker has this disabled this toggle.");
      return;
    }

    if (hasUserValidEmailAddress(user)) {
      var reassignmentRequestEmailSupplier =
          ReassignmentRequestEmailSupplier.builder()
              .receiverEmailAddress(user.getEmail())
              .receiverLanguageCode(user.getLanguageCode())
              .receiverUsername(user.getUsername())
              .tenantTemplateSupplier(tenantTemplateSupplier)
              .applicationBaseUrl(applicationBaseUrl)
              .multiTenancyEnabled(multiTenancyEnabled)
              .build();
      try {
        sendMailTasksToMailService(reassignmentRequestEmailSupplier);
      } catch (Exception exception) {
        log.error(
            "EmailNotificationFacade error: Failed to send reassign request notification",
            exception);
      }
    }
    TenantContext.clear();
  }

  private boolean shouldSendReassignmentNotificationForAdviceSeeker(User user) {
    if (releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS)) {
      return wantsToReceiveNotificationsAboutReassignment(user);
    }
    return true;
  }

  private boolean hasUserValidEmailAddress(User user) {
    return nonNull(user)
        && isNotBlank(user.getEmail())
        && !user.getEmail().endsWith(identityClientConfig.getEmailDummySuffix());
  }

  @Async
  @Transactional
  public void sendReassignConfirmationNotification(
      ReassignmentNotificationDTO reassignmentNotification, TenantData tenantData) {
    TenantContext.setCurrentTenantData(tenantData);
    Consultant existingConsultantById =
        findExistingConsultantById(reassignmentNotification.getToConsultantId().toString());

    if (!shouldSendReassignmentNotificationForConsultant(existingConsultantById)) {
      log.info(
          "Not sending email notification about reassignment because consultant has this disabled this toggle");
      return;
    }

    var reassignmentConfirmationEmailSupplier =
        ReassignmentConfirmationEmailSupplier.builder()
            .receiverConsultant(existingConsultantById)
            .senderConsultantName(reassignmentNotification.getFromConsultantName())
            .tenantTemplateSupplier(tenantTemplateSupplier)
            .applicationBaseUrl(applicationBaseUrl)
            .multiTenancyEnabled(multiTenancyEnabled)
            .build();
    try {
      sendMailTasksToMailService(reassignmentConfirmationEmailSupplier);
    } catch (Exception exception) {
      log.error(
          "EmailNotificationFacade error: Failed to send reqssign confiration notification",
          exception);
    }
    TenantContext.clear();
  }

  private boolean shouldSendReassignmentNotificationForConsultant(
      Consultant existingConsultantById) {
    if (releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS)) {
      return wantsToReceiveNotificationsAboutReassignment(existingConsultantById);
    }
    return true;
  }

  private boolean wantsToReceiveNotificationsAboutReassignment(
      NotificationsAware notificationsAware) {
    NotificationsSettingsDTO notificationsSettingsDTO =
        deserializeNotificationSettingsDTOOrDefaultIfNull(notificationsAware);
    return notificationsAware.isNotificationsEnabled()
        && notificationsSettingsDTO.getReassignmentNotificationEnabled();
  }

  private Consultant findExistingConsultantById(String consultantId) {
    return consultantService
        .getConsultant(consultantId)
        .orElseThrow(() -> new NotFoundException("Consultant with id %s not found", consultantId));
  }
}

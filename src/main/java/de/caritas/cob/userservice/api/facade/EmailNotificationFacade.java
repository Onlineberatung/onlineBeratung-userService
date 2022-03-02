package de.caritas.cob.userservice.api.facade;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.emailsupplier.AssignEnquiryEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewEnquiryEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewFeedbackEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewMessageEmailSupplier;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
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

/**
 * Facade for capsuling the mail notification via the MailService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationFacade {

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  @Value("${keycloakService.user.dummySuffix}")
  private String emailDummySuffix;

  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull MailService mailService;
  private final @NonNull SessionService sessionService;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull NewEnquiryEmailSupplier newEnquiryEmailSupplier;
  private final @NonNull AssignEnquiryEmailSupplier assignEnquiryEmailSupplier;

  /**
   * Sends email notifications according to the corresponding consultant(s) when a new enquiry was
   * written.
   *
   * @param session the regarding session
   * @param requestServerName
   */
  @Async
  public void sendNewEnquiryEmailNotification(Session session, String requestServerName) {
    try {
      overtakeCurrentTenantContextFromSessionForAsyncThread(session);
      newEnquiryEmailSupplier.setCurrentSession(session);
      newEnquiryEmailSupplier.setRequestServerName(requestServerName);
      sendMailTasksToMailService(newEnquiryEmailSupplier);
      TenantContext.clear();
    } catch (Exception ex) {
      LogService.logEmailNotificationFacadeError(String.format(
          "Failed to send new enquiry notification for session %s.", session.getId()), ex);
    }
  }

  private void overtakeCurrentTenantContextFromSessionForAsyncThread(Session session) {
    if (TenantContext.getCurrentTenant() == null && session.getTenantId() != null) {
      TenantContext.setCurrentTenant(session.getTenantId());
    }
  }

  private void sendMailTasksToMailService(EmailSupplier mailsToSend)
      throws RocketChatGetGroupMembersException {
    List<MailDTO> generatedMails = mailsToSend.generateEmails();
    if (isNotEmpty(generatedMails)) {
      MailsDTO mailsDTO = new MailsDTO()
          .mails(generatedMails);
      log.info("Sending email notifications with mailDTOs ", mailsDTO);
      mailService.sendEmailNotification(mailsDTO);
    }
  }


  @Async
  @Transactional
  public void sendNewMessageNotification(String rcGroupId, Set<String> roles, String userId,
      Long tenantId) {
    TenantContext.setCurrentTenant(tenantId);
    sendNewMessageNotification(rcGroupId, roles, userId);
    TenantContext.clear();
  }

  /**
   * Sends email notifications according to the corresponding consultant(s) or asker when a new
   * message was written.
   *
   * @param rcGroupId the rocket chat group id
   * @param roles     roles to decide the regarding recipients
   * @param userId    the user id of initiating user
   */
  @Async
  @Transactional
  public void sendNewMessageNotification(String rcGroupId, Set<String> roles, String userId) {

    try {
      Session session = sessionService.getSessionByGroupIdAndUser(rcGroupId, userId, roles);
      EmailSupplier newMessageMails = NewMessageEmailSupplier
          .builder()
          .session(session)
          .rcGroupId(rcGroupId)
          .roles(roles)
          .userId(userId)
          .consultantAgencyService(consultantAgencyService)
          .consultingTypeManager(consultingTypeManager)
          .consultantService(consultantService)
          .applicationBaseUrl(applicationBaseUrl)
          .emailDummySuffix(emailDummySuffix)
          .build();
      sendMailTasksToMailService(newMessageMails);

    } catch (NotFoundException | ForbiddenException | BadRequestException getSessionException) {
      LogService.logEmailNotificationFacadeWarning(String.format(
          "Failed to get session for new message notification with Rocket.Chat group ID %s and user ID %s.",
          rcGroupId, userId), getSessionException);
    } catch (Exception ex) {
      LogService.logEmailNotificationFacadeError(String.format(
          "Failed to send new message notification with Rocket.Chat group ID %s and user ID %s.",
          rcGroupId, userId), ex);
    }
  }

  /**
   * Sends email notifications according to the corresponding consultant(s) when a new feedback
   * message was written.
   *
   * @param rcFeedbackGroupId group id of feedback chat
   * @param userId            regarding user id
   */
  @Async
  public void sendNewFeedbackMessageNotification(String rcFeedbackGroupId, String userId) {

    try {
      Session session = sessionService.getSessionByFeedbackGroupId(rcFeedbackGroupId);
      EmailSupplier newFeedbackMessages = new NewFeedbackEmailSupplier(session,
          rcFeedbackGroupId, userId, applicationBaseUrl, consultantService, rocketChatService,
          rocketChatSystemUserId, keycloakAdminClientService);
      sendMailTasksToMailService(newFeedbackMessages);
    } catch (Exception e) {
      LogService.logEmailNotificationFacadeError(String.format(
          "List of members for rocket chat feedback group id %s is empty.", rcFeedbackGroupId), e);
    }
  }

  /**
   * Sends an email notification to the consultant when an enquiry has been assigned to him by a
   * different consultant.
   *
   * @param receiverConsultant the target consultant
   * @param senderUserId       the id of initiating user
   * @param askerUserName      the name of the asker
   */
  @Async
  public void sendAssignEnquiryEmailNotification(Consultant receiverConsultant, String senderUserId,
      String askerUserName) {

    assignEnquiryEmailSupplier.setReceiverConsultant(receiverConsultant);
    assignEnquiryEmailSupplier.setSenderUserId(senderUserId);
    assignEnquiryEmailSupplier.setAskerUserName(askerUserName);
    assignEnquiryEmailSupplier.setApplicationBaseUrl(applicationBaseUrl);
    assignEnquiryEmailSupplier.setConsultantService(consultantService);
    try {
      sendMailTasksToMailService(assignEnquiryEmailSupplier);
    } catch (Exception exception) {
      LogService.logEmailNotificationFacadeError(exception);
    }
  }

  @Async
  public void sendAssignEnquiryEmailNotification(Consultant receiverConsultant, String senderUserId,
      String askerUserName, Long tenantId) {
    TenantContext.setCurrentTenant(tenantId);
    sendAssignEnquiryEmailNotification(receiverConsultant, senderUserId, askerUserName);
    TenantContext.clear();
  }

}

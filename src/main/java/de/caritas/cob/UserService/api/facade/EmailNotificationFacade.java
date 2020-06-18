package de.caritas.cob.UserService.api.facade;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.authorization.UserRole;
import de.caritas.cob.UserService.api.exception.EmailNotificationException;
import de.caritas.cob.UserService.api.exception.NewMessageNotificationException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.helper.EmailNotificationHelper;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.model.mailService.MailDTO;
import de.caritas.cob.UserService.api.model.mailService.MailDtoBuilder;
import de.caritas.cob.UserService.api.model.mailService.MailsDTO;
import de.caritas.cob.UserService.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgencyRepository;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.session.SessionStatus;
import de.caritas.cob.UserService.api.service.ConsultantAgencyService;
import de.caritas.cob.UserService.api.service.ConsultantService;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.RocketChatService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.UserService.api.service.helper.MailServiceHelper;

/**
 * Facade for capsuling the mail notification via the MailService
 */
@Service
public class EmailNotificationFacade {

  @Value("${app.base.url}")
  private String APPLICATION_BASE_URL;

  @Value("${keycloakService.user.dummySuffix}")
  private String EMAIL_DUMMY_SUFFIX;

  @Value("${rocket.systemuser.id}")
  private String ROCKET_CHAT_SYSTEM_USER_ID;

  private ConsultantAgencyRepository consultantAgencyRepository;
  private MailServiceHelper mailServiceHelper;
  private AgencyServiceHelper agencyServiceHelper;
  private final SessionService sessionService;
  private final ConsultantAgencyService consultantAgencyService;
  private final ConsultantService consultantService;
  private final LogService logService;
  private final RocketChatService rocketChatService;
  private final MailDtoBuilder mailDtoBuilder;
  private final ConsultingTypeManager consultingTypeManager;
  private final UserHelper userHelper;

  @Autowired
  public EmailNotificationFacade(ConsultantAgencyRepository consultantAgencyRepository,
      MailServiceHelper mailServiceHelper, AgencyServiceHelper agencyServiceHelper,
      SessionService sessionService, ConsultantAgencyService consultantAgencyService,
      LogService logService, ConsultantService consultantService,
      RocketChatService rocketChatService, MailDtoBuilder mailDtoBuilder,
      ConsultingTypeManager consultingTypeManager, UserHelper userHelper) {

    this.consultantAgencyRepository = consultantAgencyRepository;
    this.mailServiceHelper = mailServiceHelper;
    this.agencyServiceHelper = agencyServiceHelper;
    this.sessionService = sessionService;
    this.consultantAgencyService = consultantAgencyService;
    this.consultantService = consultantService;
    this.logService = logService;
    this.rocketChatService = rocketChatService;
    this.mailDtoBuilder = mailDtoBuilder;
    this.consultingTypeManager = consultingTypeManager;
    this.userHelper = userHelper;
  }

  @Async
  public void sendNewEnquiryEmailNotification(Session session) {


    try {

      List<ConsultantAgency> consultantAgencyList =
          consultantAgencyRepository.findByAgencyId(session.getAgencyId());

      if (consultantAgencyList != null && !consultantAgencyList.isEmpty()) {

        AgencyDTO agency = agencyServiceHelper.getAgency(session.getAgencyId());

        MailsDTO mailsDTO = new MailsDTO();
        List<MailDTO> mailList = new ArrayList<MailDTO>();

        for (ConsultantAgency consultantAgency : consultantAgencyList) {

          if (consultantAgency.getConsultant() != null
              && consultantAgency.getConsultant().getEmail() != null
              && !consultantAgency.getConsultant().getEmail().isEmpty()
              && !consultantAgency.getConsultant().isAbsent()) {

            mailList.add(getMailDtoForNewEnquiryNotificationConsultant(
                consultantAgency.getConsultant().getEmail(),
                consultantAgency.getConsultant().getFullName(), session.getPostcode(),
                agency.getName()));

          }

        }

        if (!mailList.isEmpty()) {

          mailsDTO.setMails(mailList);
          mailServiceHelper.sendEmailNotification(mailsDTO);

        }
      }

    } catch (Exception ex) {
      throw new EmailNotificationException("Error while sending email notification", ex);
    }

  }

  /**
   * Sends email notifications according to the corresponding consultant(s) or asker when a new
   * message was written.
   * 
   */
  @Async
  public void sendNewMessageNotification(String rcGroupId, Set<String> roles, String userId) {

    List<MailDTO> mailList = new ArrayList<MailDTO>();

    try {
      Session session = sessionService.getSessionByGroupIdAndUserId(rcGroupId, userId, roles);

      // Asker wrote the answer -> inform the consultant(s)
      if (roles.contains(UserRole.USER.getValue())) {

        // Only send a notification if the session exists and also belongs to the requesting asker
        if (session != null && session.getUser().getUserId().equals(userId)
            && session.getStatus().equals(SessionStatus.IN_PROGRESS)) {

          List<ConsultantAgency> consultantList = null;
          ConsultingTypeSettings consultingTypeSettings =
              consultingTypeManager.getConsultantTypeSettings(session.getConsultingType());

          // Currently only all consultants of a team session are being informed if
          // allTeamConsultants is set to true in the ConsultingTypeSettings
          if (session.isTeamSession() && consultingTypeSettings.getNotifications().getNewMessage()
              .getTeamSession().getToConsultant().isAllTeamConsultants()) {
            // Team session -> inform all consultants of this agency
            consultantList =
                consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());
          } else {
            // Single session -> inform the assigned consultant
            if (session.getConsultant().getEmail() != null
                && !session.getConsultant().getEmail().isEmpty()) {
              ConsultantAgency agency = new ConsultantAgency(null, session.getConsultant(), null);
              consultantList = Arrays.asList(agency);
            }
          }

          // Create a mail template for every consultant on the mail list
          if (consultantList != null && !consultantList.isEmpty()) {
            for (ConsultantAgency agency : consultantList) {
              if (!agency.getConsultant().getEmail().isEmpty()) {

                mailList.add(
                    getMailDtoForNewMessageNotificationConsultant(agency.getConsultant().getEmail(),
                        agency.getConsultant().getFullName(), session.getPostcode()));

              }
            }
          }

        } else {
          logService.logEmailNotificationFacadeError(String.format(
              "No currently running (SessionStatus = IN_PROGRESS) session found for Rocket.Chat group id %s and user id %s or the session does not belong to the user.",
              rcGroupId, userId));
        }
      }

      // Consultant wrote the answer -> inform the asker
      if (roles.contains(UserRole.CONSULTANT.getValue())) {

        // Only send a notification if the session exists and the consultant is also assigned to
        // this session and if the asker has provided a e-mail address
        if (session != null && session.getConsultant().getId().equals(userId)
            && session.getStatus().equals(SessionStatus.IN_PROGRESS)
            && (session.getUser().getEmail() != null && !session.getUser().getEmail().isEmpty())) {

          if (session.getUser().getEmail().contains(EMAIL_DUMMY_SUFFIX)) {
            return;
          }

          mailList.add(getMailDtoForNewMessageNotificationAsker(session.getUser().getEmail(),
              userHelper.decodeUsername(session.getConsultant().getUsername()),
              userHelper.decodeUsername(session.getUser().getUsername())));

        } else {
          logService.logEmailNotificationFacadeError(String.format(
              "No currently running (SessionStatus = IN_PROGRESS) session found for Rocket.Chat group id %s and user id %s, the session does not belong to the user or has not provided a e-mail address.",
              rcGroupId, userId));
        }
      }

    } catch (ServiceException ex) {
      throw new NewMessageNotificationException("Error while sending new message notification: ",
          ex);
    }


    // Send e mail task to MailService
    if (mailList != null && !mailList.isEmpty()) {
      MailsDTO mailsDTO = new MailsDTO();
      mailsDTO.setMails(mailList);
      mailServiceHelper.sendEmailNotification(mailsDTO);
    }

  }

  /**
   * Sends email notifications according to the corresponding consultant(s) when a new feedback
   * message was written.
   * 
   */
  @Async
  public void sendNewFeedbackMessageNotification(String rcFeedbackGroupId, Set<String> roles,
      String userId) {

    List<MailDTO> mailList = new ArrayList<MailDTO>();

    try {

      Optional<Consultant> sendingConsultantOptional = consultantService.getConsultant(userId);
      if (!sendingConsultantOptional.isPresent()) {
        logService.logEmailNotificationFacadeError(
            String.format("Consultant with id %s not found.", userId));
        return;
      }

      Session session = sessionService.getSessionByFeedbackGroupId(rcFeedbackGroupId);

      if (session == null) {
        logService.logEmailNotificationFacadeError(String.format(
            "No session found for the rocket chat feedback group id %s.", rcFeedbackGroupId));
        return;
      }

      if (session.getConsultant() == null) {
        logService.logEmailNotificationFacadeError(String.format(
            "No consultant is assigned to the session found by rocket chat feedback group id %s.",
            rcFeedbackGroupId));
        return;
      }

      // Assigned consultant wrote -> inform all other consultants
      if (userId.equals(session.getConsultant().getId())) {

        List<GroupMemberDTO> groupMembers = rocketChatService.getMembersOfGroup(rcFeedbackGroupId);

        if (groupMembers == null || groupMembers.isEmpty()) {
          logService.logEmailNotificationFacadeError(String.format(
              "List of members for rocket chat feedback group id %s is empty.", rcFeedbackGroupId));
          return;
        }

        for (GroupMemberDTO groupMemberDTO : groupMembers) {

          if (ROCKET_CHAT_SYSTEM_USER_ID.equals(groupMemberDTO.get_id())) {
            continue;
          }

          Optional<Consultant> consultantOptional =
              consultantService.getConsultantByRcUserId(groupMemberDTO.get_id());

          if (!consultantOptional.isPresent()) {
            logService.logEmailNotificationFacadeError(String.format(
                "Consultant with rc user id %s not found. Why is this consultant in the rc room with the id %s?",
                groupMemberDTO.get_id(), rcFeedbackGroupId));
            continue;
          }

          if (consultantOptional.get().getEmail() != null
              && !userId.equals(consultantOptional.get().getId())
              && !session.getConsultant().getId().equals(consultantOptional.get().getId())
              && !consultantOptional.get().isAbsent()) {

            String nameSender = sendingConsultantOptional.get().getFullName();
            String nameRecipient = consultantOptional.get().getFullName();
            String nameUser = userHelper.decodeUsername(session.getUser().getUsername());
            String email = consultantOptional.get().getEmail();

            mailList.add(getMailDtoForFeedbackMessageNotification(email, nameSender, nameRecipient,
                nameUser));
          }

        }

      }

      // Other consultant wrote -> inform only the assigned consultant
      if (!userId.equals(session.getConsultant().getId())
          && !session.getConsultant().getEmail().isEmpty() && !session.getConsultant().isAbsent()) {

        String nameSender = sendingConsultantOptional.get().getFullName();
        String nameRecipient = session.getConsultant().getFullName();
        String nameUser = userHelper.decodeUsername(session.getUser().getUsername());
        String email = session.getConsultant().getEmail();

        mailList.add(
            getMailDtoForFeedbackMessageNotification(email, nameSender, nameRecipient, nameUser));
      }

    } catch (ServiceException ex) {
      throw new NewMessageNotificationException("Error while sending new message notification: ",
          ex);
    }

    // Send e mail task to MailService
    if (mailList != null && !mailList.isEmpty()) {
      MailsDTO mailsDTO = new MailsDTO();
      mailsDTO.setMails(mailList);
      mailServiceHelper.sendEmailNotification(mailsDTO);
    }

  }

  /**
   * Sends an email notifications to the consultant when an enquiry has been assigned to him by a
   * different consultant
   * 
   */
  @Async
  public void sendAssignEnquiryEmailNotification(Consultant receiverConsultant, String senderUserId,
      String askerUserName) {

    if (receiverConsultant == null || receiverConsultant.getEmail() == null
        || receiverConsultant.getEmail().isEmpty()) {
      logService.logEmailNotificationFacadeError(String.format(
          "Error while sending assign message notification: Receiver consultant with id %s is null or doesn't have an email address.",
          receiverConsultant != null ? receiverConsultant.getId() : "unknown"));
      return;
    }

    Optional<Consultant> senderConsultant = consultantService.getConsultant(senderUserId);

    if (!senderConsultant.isPresent()) {
      logService.logEmailNotificationFacadeError(String.format(
          "Error while sending assign message notification: Sender consultant with id %s could not be found in database.",
          senderUserId));
      return;
    }

    MailsDTO mailsDTO = new MailsDTO();
    List<MailDTO> mailList = new ArrayList<MailDTO>();
    mailList.add(getMailDtoForAssignEnquiryNotification(receiverConsultant.getEmail(),
        senderConsultant.get().getFullName(), receiverConsultant.getFullName(),
        userHelper.decodeUsername(askerUserName)));

    if (!mailList.isEmpty()) {
      mailsDTO.setMails(mailList);
      mailServiceHelper.sendEmailNotification(mailsDTO);
    }
  }

  /**
   * 
   * Get MailDTO for new enquiry notification
   * 
   * @param email
   * @param nameSender
   * @param nameRecipient
   * @param nameUser
   * @return
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForNewEnquiryNotificationConsultant(String email, String name,
      String postCode, String agency) {
    return mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_NEW_ENQUIRY_NOTIFICATION, email,
        new SimpleImmutableEntry<String, String>("name", name),
        new SimpleImmutableEntry<String, String>("plz", postCode),
        new SimpleImmutableEntry<String, String>("beratungsstelle", agency),
        new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL));
  }

  /**
   * 
   * Get MailDTO for new message notification (consultant)
   * 
   * @param email
   * @param name
   * @param postCode
   * @return
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForNewMessageNotificationConsultant(String email, String name,
      String postCode) {
    return mailDtoBuilder.build(
        EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT, email,
        new SimpleImmutableEntry<String, String>("name", name),
        new SimpleImmutableEntry<String, String>("plz", postCode),
        new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL));
  }


  /**
   * 
   * Get MailDTO for new message notification (asker)
   * 
   * @param email
   * @param consultantName
   * @param askerName
   * @return
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForNewMessageNotificationAsker(String email, String consultantName,
      String askerName) {
    return mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER,
        email, new SimpleImmutableEntry<String, String>("consultantName", consultantName),
        new SimpleImmutableEntry<String, String>("askerName", askerName),
        new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL));
  }

  /**
   * 
   * Get MailDTO for feedback message notification
   * 
   * @param email
   * @param nameSender
   * @param nameRecipient
   * @param nameUser
   * @return
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForFeedbackMessageNotification(String email, String nameSender,
      String nameRecipient, String nameUser) {

    return mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION,
        email, new SimpleImmutableEntry<String, String>("name_sender", nameSender),
        new SimpleImmutableEntry<String, String>("name_recipient", nameRecipient),
        new SimpleImmutableEntry<String, String>("name_user", nameUser),
        new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL));

  }

  /**
   * 
   * Get MailDTO for assign enquiry notification
   * 
   * @param email
   * @param nameSender
   * @param nameRecipient
   * @param nameUser
   * @return
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForAssignEnquiryNotification(String email, String nameSender,
      String nameRecipient, String nameUser) {

    return mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_ASSIGN_ENQUIRY_NOTIFICATION, email,
        new SimpleImmutableEntry<String, String>("name_sender", nameSender),
        new SimpleImmutableEntry<String, String>("name_recipient", nameRecipient),
        new SimpleImmutableEntry<String, String>("name_user", nameUser),
        new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL));

  }

}

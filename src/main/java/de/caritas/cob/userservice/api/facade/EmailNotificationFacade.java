package de.caritas.cob.userservice.api.facade;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.EmailNotificationException;
import de.caritas.cob.userservice.api.exception.NewMessageNotificationException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.helper.EmailNotificationHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.mailService.MailDTO;
import de.caritas.cob.userservice.api.model.mailService.MailDtoBuilder;
import de.caritas.cob.userservice.api.model.mailService.MailsDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.MailServiceHelper;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Facade for capsuling the mail notification via the MailService
 */
@Service
@RequiredArgsConstructor
public class EmailNotificationFacade {

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  @Value("${keycloakService.user.dummySuffix}")
  private String emailDummySuffix;

  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull MailServiceHelper mailServiceHelper;
  private final @NonNull AgencyServiceHelper agencyServiceHelper;
  private final @NonNull SessionService sessionService;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull MailDtoBuilder mailDtoBuilder;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UserHelper userHelper;

  /**
   * Sends email notifications according to the corresponding consultant(s) when a new enquiry was
   * written.
   *
   * @param session the regarding session
   */
  @Async
  public void sendNewEnquiryEmailNotification(Session session) {

    try {
      List<ConsultantAgency> consultantAgencyList =
          consultantAgencyRepository.findByAgencyId(session.getAgencyId());

      if (isNotEmpty(consultantAgencyList)) {
        AgencyDTO agency = agencyServiceHelper.getAgency(session.getAgencyId());

        List<MailDTO> mailList = consultantAgencyList.stream()
            .filter(onlyValidConsultantAgency())
            .map(toEnquiryMailDTO(session, agency))
            .collect(Collectors.toList());

        sendMailTasksToMailService(mailList);
      }
    } catch (Exception ex) {
      throw new EmailNotificationException("Error while sending email notification", ex);
    }
  }

  private Predicate<ConsultantAgency> onlyValidConsultantAgency() {
    return consultantAgency -> nonNull(consultantAgency) &&
        isNotBlank(consultantAgency.getConsultant().getEmail()) &&
        !consultantAgency.getConsultant().isAbsent();
  }

  private Function<ConsultantAgency, MailDTO> toEnquiryMailDTO(Session session, AgencyDTO agency) {
    return consultantAgency -> getMailDtoForNewEnquiryNotificationConsultant(
        consultantAgency.getConsultant().getEmail(),
        consultantAgency.getConsultant().getFullName(),
        session.getPostcode(),
        agency.getName()
    );
  }

  private void sendMailTasksToMailService(List<MailDTO> mailList) {
    if (isNotEmpty(mailList)) {
      MailsDTO mailsDTO = new MailsDTO(mailList);
      mailServiceHelper.sendEmailNotification(mailsDTO);
    }
  }

  /**
   * Sends email notifications according to the corresponding consultant(s) or asker when a new
   * message was written.
   *
   * @param rcGroupId the rocket chat group id
   * @param roles roles to decide the regarding recipients
   * @param userId the user id of initiated user
   */
  @Async
  public void sendNewMessageNotification(String rcGroupId, Set<String> roles, String userId) {

    try {
      Session session = sessionService.getSessionByGroupIdAndUserId(rcGroupId, userId, roles);

      // Asker wrote the answer -> inform the consultant(s)
      if (roles.contains(UserRole.USER.getValue())) {
        sendNewMessageNotificationToConsultants(rcGroupId, userId, session);
      }

      // Consultant wrote the answer -> inform the asker
      if (roles.contains(UserRole.CONSULTANT.getValue())) {
        sendNewMessageNotificationToAsker(rcGroupId, userId, session);
      }

    } catch (InternalServerErrorException ex) {
      throw new NewMessageNotificationException("Error while sending new message notification: ",
          ex);
    }
  }

  private void sendNewMessageNotificationToConsultants(String rcGroupId, String userId,
      Session session) {
    if (doesSessionExistAndBelongsToAsker(userId, session)) {

      List<ConsultantAgency> consultantList = retrieveDependentConsultantAgencies(session);

      if (isNotEmpty(consultantList)) {
        List<MailDTO> mailList = consultantList.stream()
            .filter(agency -> !agency.getConsultant().getEmail().isEmpty())
            .map(toNewConsultantMessageMailDTO(session))
            .collect(Collectors.toList());
        sendMailTasksToMailService(mailList);
      }

    } else if (!SessionStatus.NEW.equals(isNull(session) ? session : session.getStatus())) {
      LogService.logEmailNotificationFacadeError(String.format(
          "No currently running (SessionStatus = IN_PROGRESS) session found for Rocket.Chat group id %s and user id %s or the session does not belong to the user.",
          rcGroupId, userId));
    }
  }

  private Function<ConsultantAgency, MailDTO> toNewConsultantMessageMailDTO(Session session) {
    return agency -> getMailDtoForNewMessageNotificationConsultant(
        agency.getConsultant().getEmail(),
        agency.getConsultant().getFullName(), session.getPostcode());
  }

  private List<ConsultantAgency> retrieveDependentConsultantAgencies(Session session) {
    if (shouldInformAllConsultantsOfTeamSession(session)) {
      return consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());
    } else {
      if (isNotBlank(session.getConsultant().getEmail())) {
        return singletonList(new ConsultantAgency(null, session.getConsultant(), null));
      }
    }
    return emptyList();
  }

  private boolean doesSessionExistAndBelongsToAsker(String userId, Session session) {
    return nonNull(session) && session.getUser().getUserId().equals(userId)
        && session.getStatus().equals(SessionStatus.IN_PROGRESS);
  }

  private boolean shouldInformAllConsultantsOfTeamSession(Session session) {
    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(session.getConsultingType());
    return session.isTeamSession() && consultingTypeSettings.getNotifications().getNewMessage()
        .getTeamSession().getToConsultant().isAllTeamConsultants();
  }

  private void sendNewMessageNotificationToAsker(String rcGroupId, String userId, Session session) {

    if (doesSessionExistAndBelongsToConsultant(userId, session)) {
      if (!session.getUser().getEmail().contains(emailDummySuffix)) {
        MailDTO mailDTO = getMailDtoForNewMessageNotificationAsker(session.getUser().getEmail(),
            userHelper.decodeUsername(session.getConsultant().getUsername()),
            userHelper.decodeUsername(session.getUser().getUsername()));
        sendMailTasksToMailService(singletonList(mailDTO));
      }
    } else {
      LogService.logEmailNotificationFacadeError(String.format(
          "No currently running (SessionStatus = IN_PROGRESS) session found for Rocket.Chat group id %s and user id %s, the session does not belong to the user or has not provided a e-mail address.",
          rcGroupId, userId));
    }
  }

  private boolean doesSessionExistAndBelongsToConsultant(String userId, Session session) {
    return nonNull(session) && session.getConsultant().getId().equals(userId)
        && session.getStatus().equals(SessionStatus.IN_PROGRESS)
        && isNotBlank(session.getUser().getEmail());
  }

  /**
   * Sends email notifications according to the corresponding consultant(s) when a new feedback
   * message was written.
   *
   * @param rcFeedbackGroupId group id of feedback chat
   * @param userId regarding user id
   */
  @Async
  public void sendNewFeedbackMessageNotification(String rcFeedbackGroupId, String userId) {

    try {

      Session session = sessionService.getSessionByFeedbackGroupId(rcFeedbackGroupId);
      if (nonNull(session)) {
        sendFeedbackMessageForExistingSession(rcFeedbackGroupId, userId, session);
      } else {
        LogService.logEmailNotificationFacadeError(String.format(
            "No session found for the rocket chat feedback group id %s.", rcFeedbackGroupId));
      }

    } catch (InternalServerErrorException ex) {
      throw new NewMessageNotificationException("Error while sending new message notification: ",
          ex);
    } catch (RocketChatGetGroupMembersException e) {
      LogService.logEmailNotificationFacadeError(String.format(
          "List of members for rocket chat feedback group id %s is empty.", rcFeedbackGroupId));
    }
  }

  private void sendFeedbackMessageForExistingSession(String rcFeedbackGroupId, String userId,
      Session session) throws RocketChatGetGroupMembersException {
    if (nonNull(session.getConsultant())) {
      sendFeedbackMessageForExistingConsultant(rcFeedbackGroupId, userId, session);
    } else {
      LogService.logEmailNotificationFacadeError(String.format(
          "No consultant is assigned to the session found by rocket chat feedback group id %s.",
          rcFeedbackGroupId));
    }
  }

  private void sendFeedbackMessageForExistingConsultant(String rcFeedbackGroupId, String userId,
      Session session) throws RocketChatGetGroupMembersException {
    Optional<Consultant> sendingConsultantOptional = consultantService.getConsultant(userId);
    if (sendingConsultantOptional.isPresent()) {
      Consultant sendingConsultant = sendingConsultantOptional.get();
      if (areUsersEqual(userId, session.getConsultant())) {
        sendNotificationMailToAllOtherConsultants(rcFeedbackGroupId, userId, sendingConsultant,
            session);
      }

      if (didAnotherConsultantWrote(userId, session)) {
        sendNotificationMailToAssignedConsultant(sendingConsultant, session);
      }
    } else {
      LogService.logEmailNotificationFacadeError(
          String.format("Consultant with id %s not found.", userId));
    }
  }

  private boolean areUsersEqual(String userId, Consultant consultant) {
    return userId.equals(consultant.getId());
  }

  private void sendNotificationMailToAllOtherConsultants(String rcFeedbackGroupId, String userId,
      Consultant sendingConsultantOptional, Session session)
      throws RocketChatGetGroupMembersException {
    List<GroupMemberDTO> groupMembers = rocketChatService.getMembersOfGroup(rcFeedbackGroupId);
    if (isNotEmpty(groupMembers)) {
      List<MailDTO> mailList = groupMembers.stream()
          .filter(groupMemberDTO -> !rocketChatSystemUserId.equals(groupMemberDTO.get_id()))
          .map(groupMemberDTO -> this.toValidatedConsultant(groupMemberDTO, rcFeedbackGroupId))
          .filter(Objects::nonNull)
          .filter(consultant -> sessionBelongsToConsultant(userId, session, consultant))
          .map(consultant -> buildMailForAssignedConsultant(sendingConsultantOptional, session,
              consultant))
          .collect(Collectors.toList());
      sendMailTasksToMailService(mailList);
    }

    LogService.logEmailNotificationFacadeError(String.format(
        "List of members for rocket chat feedback group id %s is empty.", rcFeedbackGroupId));
  }

  private void sendNotificationMailToAssignedConsultant(
      Consultant sendingConsultantOptional, Session session) {
    List<MailDTO> mailList = singletonList(buildMailForAssignedConsultant(sendingConsultantOptional,
        session, session.getConsultant()));
    sendMailTasksToMailService(mailList);
  }

  private Consultant toValidatedConsultant(GroupMemberDTO groupMemberDTO,
      String rcFeedbackGroupId) {
    Optional<Consultant> optionalConsultant =
        this.consultantService.getConsultantByRcUserId(groupMemberDTO.get_id());
    if (optionalConsultant.isPresent()) {
      return optionalConsultant.get();
    }
    LogService.logEmailNotificationFacadeError(String.format(
        "Consultant with rc user id %s not found. Why is this consultant in the rc room with the id %s?",
        groupMemberDTO.get_id(), rcFeedbackGroupId));
    return null;
  }

  private boolean sessionBelongsToConsultant(String userId, Session session,
      Consultant consultant) {
    return isNotBlank(consultant.getEmail())
        && !areUsersEqual(userId, consultant)
        && !areUsersEqual(session.getConsultant().getId(), consultant)
        && !consultant.isAbsent();
  }

  private boolean didAnotherConsultantWrote(String userId, Session session) {
    return !areUsersEqual(userId, session.getConsultant())
        && !session.getConsultant().getEmail().isEmpty() && !session.getConsultant().isAbsent();
  }

  private MailDTO buildMailForAssignedConsultant(Consultant sendingConsultant,
      Session session, Consultant consultant) {
    String nameSender = sendingConsultant.getFullName();
    String nameRecipient = consultant.getFullName();
    String nameUser = userHelper.decodeUsername(session.getUser().getUsername());
    String email = consultant.getEmail();

    return getMailDtoForFeedbackMessageNotification(email, nameSender, nameRecipient, nameUser);
  }

  /**
   * Sends an email notifications to the consultant when an enquiry has been assigned to him by a
   * different consultant
   */
  @Async
  public void sendAssignEnquiryEmailNotification(Consultant receiverConsultant, String senderUserId,
      String askerUserName) {

    if (nonNull(receiverConsultant) && isNotBlank(receiverConsultant.getEmail())) {
      sendAssignEnquiryMailWithExistingReceiver(receiverConsultant, senderUserId, askerUserName);
    } else {
      LogService.logEmailNotificationFacadeError(String.format(
          "Error while sending assign message notification: Receiver consultant with id %s is null or doesn't have an email address.",
          receiverConsultant != null ? receiverConsultant.getId() : "unknown"));
    }
  }

  private void sendAssignEnquiryMailWithExistingReceiver(Consultant receiverConsultant,
      String senderUserId, String askerUserName) {
    Optional<Consultant> senderConsultant = consultantService.getConsultant(senderUserId);

    if (senderConsultant.isPresent()) {
      List<MailDTO> mailList = singletonList(getMailDtoForAssignEnquiryNotification(
          receiverConsultant.getEmail(),
          senderConsultant.get().getFullName(),
          receiverConsultant.getFullName(),
          userHelper.decodeUsername(askerUserName)));

      sendMailTasksToMailService(mailList);
    } else {
      LogService.logEmailNotificationFacadeError(String.format(
          "Error while sending assign message notification: Sender consultant with id %s could not be found in database.",
          senderUserId));
    }
  }

  /**
   * Get MailDTO for new enquiry notification
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForNewEnquiryNotificationConsultant(String email, String name,
      String postCode, String agency) {
    return mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_NEW_ENQUIRY_NOTIFICATION, email,
        new SimpleImmutableEntry<>("name", name),
        new SimpleImmutableEntry<>("plz", postCode),
        new SimpleImmutableEntry<>("beratungsstelle", agency),
        new SimpleImmutableEntry<>("url", applicationBaseUrl));
  }

  /**
   * Get MailDTO for new message notification (consultant)
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForNewMessageNotificationConsultant(String email, String name,
      String postCode) {
    return mailDtoBuilder.build(
        EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT, email,
        new SimpleImmutableEntry<>("name", name),
        new SimpleImmutableEntry<>("plz", postCode),
        new SimpleImmutableEntry<>("url", applicationBaseUrl));
  }


  /**
   * Get MailDTO for new message notification (asker)
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForNewMessageNotificationAsker(String email, String consultantName,
      String askerName) {
    return mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER,
        email, new SimpleImmutableEntry<>("consultantName", consultantName),
        new SimpleImmutableEntry<>("askerName", askerName),
        new SimpleImmutableEntry<>("url", applicationBaseUrl));
  }

  /**
   * Get MailDTO for feedback message notification
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForFeedbackMessageNotification(String email, String nameSender,
      String nameRecipient, String nameUser) {

    return mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION,
        email, new SimpleImmutableEntry<>("name_sender", nameSender),
        new SimpleImmutableEntry<>("name_recipient", nameRecipient),
        new SimpleImmutableEntry<>("name_user", nameUser),
        new SimpleImmutableEntry<>("url", applicationBaseUrl));
  }

  /**
   * Get MailDTO for assign enquiry notification
   */
  @SuppressWarnings("unchecked")
  private MailDTO getMailDtoForAssignEnquiryNotification(String email, String nameSender,
      String nameRecipient, String nameUser) {

    return mailDtoBuilder.build(EmailNotificationHelper.TEMPLATE_ASSIGN_ENQUIRY_NOTIFICATION, email,
        new SimpleImmutableEntry<>("name_sender", nameSender),
        new SimpleImmutableEntry<>("name_recipient", nameRecipient),
        new SimpleImmutableEntry<>("name_user", nameUser),
        new SimpleImmutableEntry<>("url", applicationBaseUrl));
  }

}

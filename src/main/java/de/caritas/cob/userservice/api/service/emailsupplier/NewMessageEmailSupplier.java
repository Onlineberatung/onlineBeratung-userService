package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER;
import static de.caritas.cob.userservice.api.helper.EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.mailservice.MailDTO;
import de.caritas.cob.userservice.api.model.mailservice.TemplateDataDTO;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

/**
 * Supplier to provide mails to be sent when a new message has been written.
 */
@AllArgsConstructor
public class NewMessageEmailSupplier implements EmailSupplier {

  private final Session session;
  private final String rcGroupId;
  private final Set<String> roles;
  private final String userId;
  private final ConsultantAgencyService consultantAgencyService;
  private final ConsultingTypeManager consultingTypeManager;
  private final String applicationBaseUrl;
  private final String emailDummySuffix;
  private final UserHelper userHelper;

  /**
   * Generates new message notification mails sent to regarding consultants when a user has written
   * a new message and to regarding user when a consultant has written a new message.
   *
   * @return a list of the generated {@link MailDTO}
   */
  @Override
  public List<MailDTO> generateEmails() {
    // Asker wrote the answer -> inform the consultant(s)
    if (roles.contains(UserRole.USER.getValue())) {
      return buildMailsForConsultants();
    }
    // Consultant wrote the answer -> inform the asker
    if (roles.contains(UserRole.CONSULTANT.getValue())) {
      return buildMailForAsker();
    }
    return emptyList();
  }

  private List<MailDTO> buildMailsForConsultants() {

    if (isSessionActiveAndBelongToAsker()) {
      return buildMailsForSession();
    }
    if (isNotTheFirstMessage()) {
      LogService.logEmailNotificationFacadeError(String.format(
          "No currently running (SessionStatus = IN_PROGRESS) session found for Rocket.Chat group id %s and user id %s or the session does not belong to the user.",
          rcGroupId, userId));
    }
    return emptyList();
  }

  private boolean isSessionActiveAndBelongToAsker() {
    return nonNull(session) && session.getUser().getUserId().equals(userId)
        && session.getStatus().equals(SessionStatus.IN_PROGRESS);
  }

  private List<MailDTO> buildMailsForSession() {
    List<ConsultantAgency> consultantList = retrieveDependentConsultantAgencies();
    if (isNotEmpty(consultantList)) {
      return consultantList.stream()
          .filter(agency -> !agency.getConsultant().getEmail().isEmpty())
          .map(this::toNewConsultantMessageMailDTO)
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  private boolean isNotTheFirstMessage() {
    return !SessionStatus.NEW.equals(isNull(session) ? session : session.getStatus());
  }

  private List<ConsultantAgency> retrieveDependentConsultantAgencies() {
    if (shouldInformAllConsultantsOfTeamSession()) {
      return consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());
    } else {
      if (isNotBlank(session.getConsultant().getEmail())) {
        return singletonList(new ConsultantAgency(null, session.getConsultant(), null));
      }
    }
    return emptyList();
  }

  private boolean shouldInformAllConsultantsOfTeamSession() {
    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultingTypeSettings(session.getConsultingType());
    return session.isTeamSession() && isTrue(consultingTypeSettings.getNotifications().getNewMessage()
        .getTeamSession().getToConsultant().getAllTeamConsultants());
  }

  private MailDTO toNewConsultantMessageMailDTO(ConsultantAgency agency) {
    return buildMailDtoForNewMessageNotificationConsultant(
        agency.getConsultant().getEmail(),
        agency.getConsultant().getFullName(), session.getPostcode());
  }

  private MailDTO buildMailDtoForNewMessageNotificationConsultant(String email, String name,
      String postCode) {
    return MailDTO.builder()
        .template(TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT)
        .email(email)
        .templateData(asList(
            new TemplateDataDTO("name", name),
            new TemplateDataDTO("plz", postCode),
            new TemplateDataDTO("url", applicationBaseUrl)))
        .build();
  }

  private List<MailDTO> buildMailForAsker() {

    if (isSessionActiveAndBelongToConsultant() && isNotADummyMail()) {
      return singletonList(
          buildMailDtoForNewMessageNotificationAsker(session.getUser().getEmail(),
              userHelper.decodeUsername(session.getConsultant().getUsername()),
              userHelper.decodeUsername(session.getUser().getUsername())));
    }
    if (isNotADummyMail()) {
      LogService.logEmailNotificationFacadeError(String.format(
          "No currently running (SessionStatus = IN_PROGRESS) session found for Rocket.Chat group id %s and user id %s, the session does not belong to the user or has not provided a e-mail address.",
          rcGroupId, userId));
    }

    return emptyList();
  }

  private boolean isSessionActiveAndBelongToConsultant() {
    return nonNull(session) && userId.equals(session.getConsultant().getId())
        && session.getStatus().equals(SessionStatus.IN_PROGRESS)
        && isNotBlank(session.getUser().getEmail());
  }

  private boolean isNotADummyMail() {
    return !session.getUser().getEmail().contains(emailDummySuffix);
  }

  private MailDTO buildMailDtoForNewMessageNotificationAsker(String email, String consultantName,
      String askerName) {
    return MailDTO.builder()
        .template(TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER)
        .email(email)
        .templateData(asList(
            new TemplateDataDTO("consultantName", consultantName),
            new TemplateDataDTO("askerName", askerName),
            new TemplateDataDTO("url", applicationBaseUrl)))
        .build();
  }

}

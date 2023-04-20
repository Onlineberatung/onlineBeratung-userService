package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.helper.EmailNotificationUtils.deserializeNotificationSettingsOrDefaultIfNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.Lists;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.NotificationSettings;
import de.caritas.cob.userservice.api.model.NotificationsAware;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggle;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggleService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NotificationsDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/** Supplier to provide mails to be sent when a new message has been written. */
@Slf4j
@AllArgsConstructor
@Builder
public class NewMessageEmailSupplier implements EmailSupplier {

  private final Session session;
  private final String rcGroupId;
  private final Set<String> roles;
  private final String userId;
  private final ConsultantAgencyService consultantAgencyService;
  private final ConsultingTypeManager consultingTypeManager;
  private final ConsultantService consultantService;
  private final String applicationBaseUrl;
  private final String emailDummySuffix;
  private boolean multiTenancyEnabled;
  private final TenantTemplateSupplier tenantTemplateSupplier;
  private final MessageClient messageClient;

  private final ReleaseToggleService releaseToggleService;

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
      log.error(
          "EmailNotificationFacade error: No currently running (SessionStatus = IN_PROGRESS) "
              + "session found for Rocket.Chat group id {} and user id {} or the session does not "
              + "belong to the user.",
          rcGroupId,
          userId);
    }
    return emptyList();
  }

  private boolean isSessionActiveAndBelongToAsker() {
    return nonNull(session)
        && session.getUser().getUserId().equals(userId)
        && session.getStatus().equals(SessionStatus.IN_PROGRESS);
  }

  private List<MailDTO> buildMailsForSession() {
    List<ConsultantAgency> consultantList = retrieveDependentConsultantAgencies();
    if (isNotEmpty(consultantList)) {
      return consultantList.stream()
          .filter(agency -> !agency.getConsultant().getEmail().isEmpty())
          .filter(agency -> wantsToReceiveNotifications(agency.getConsultant()))
          .filter(isConsultantLoggedOut())
          .map(this::toNewConsultantMessageMailDTO)
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  private boolean wantsToReceiveNotifications(Consultant consultant) {

    if (isNewNotificationToggleEnabled()) {
      return wantsToReceiveNotificationsAboutNewMessage(consultant);
    } else {
      return consultant.getNotifyNewChatMessageFromAdviceSeeker();
    }
  }

  private boolean isNewNotificationToggleEnabled() {
    return releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS);
  }

  private boolean wantsToReceiveNotificationsAboutNewMessage(
      NotificationsAware notificationsAware) {
    NotificationSettings notificationSettings =
        deserializeNotificationSettingsOrDefaultIfNull(notificationsAware);
    return notificationsAware.isNotificationsEnabled()
        && notificationSettings.isNewChatMessageNotificationEnabled();
  }

  private boolean isNotTheFirstMessage() {
    return !SessionStatus.NEW.equals(isNull(session) ? session : session.getStatus());
  }

  private List<ConsultantAgency> retrieveDependentConsultantAgencies() {
    if (shouldInformAllConsultantsOfTeamSession()) {
      return consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());
    } else {
      if (isNotBlank(session.getConsultant().getEmail())) {
        return singletonList(
            new ConsultantAgency(
                null, session.getConsultant(), null, nowInUtc(), nowInUtc(), null, null, null));
      }
    }
    return emptyList();
  }

  private boolean shouldInformAllConsultantsOfTeamSession() {
    var extendedConsultingTypeResponseDTO =
        consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId());
    return session.isTeamSession()
        && retrieveCheckedAllTeamConsultantsProperty(
            extendedConsultingTypeResponseDTO.getNotifications());
  }

  private boolean retrieveCheckedAllTeamConsultantsProperty(NotificationsDTO notificationsDTO) {
    if (isNull(notificationsDTO)
        || isNull(notificationsDTO.getTeamSessions())
        || isNull(notificationsDTO.getTeamSessions().getNewMessage())) {
      return false;
    }
    return isTrue(notificationsDTO.getTeamSessions().getNewMessage().getAllTeamConsultants());
  }

  private MailDTO toNewConsultantMessageMailDTO(ConsultantAgency agency) {
    return buildMailDtoForNewMessageNotificationConsultant(
        agency.getConsultant(), session.getPostcode());
  }

  private MailDTO buildMailDtoForNewMessageNotificationConsultant(
      Consultant recipient, String postCode) {
    var templateAttributes = new ArrayList<TemplateDataDTO>();
    templateAttributes.add(new TemplateDataDTO().key("name").value(recipient.getFullName()));
    templateAttributes.add(new TemplateDataDTO().key("plz").value(postCode));
    if (!multiTenancyEnabled) {
      templateAttributes.add(new TemplateDataDTO().key("url").value(applicationBaseUrl));
    } else {
      templateAttributes.addAll(tenantTemplateSupplier.getTemplateAttributes());
    }

    return new MailDTO()
        .template(TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT)
        .email(recipient.getEmail())
        .language(languageOf(recipient.getLanguageCode()))
        .templateData(templateAttributes);
  }

  private List<MailDTO> buildMailForAsker() {
    if (isNull(session)) {
      log.error(
          "EmailNotificationFacade error: No currently running (SessionStatus = IN_PROGRESS) "
              + "session found for Rocket.Chat group id {} and user id {}",
          rcGroupId,
          userId);
    } else if (!isAdviceSeekerWithEmail()) {
      log.info("Cannot send email. Advice seeker ({}) has no genuine address.", userId);
    } else if (isAdviceSeekerLoggedOut()) {
      return buildMailForAskerList();
    }

    return emptyList();
  }

  private Predicate<ConsultantAgency> isConsultantLoggedOut() {
    return agency ->
        !messageClient.isLoggedIn(agency.getConsultant().getRocketChatId()).orElse(false);
  }

  private boolean isAdviceSeekerLoggedOut() {
    return !messageClient.isLoggedIn(session.getUser().getRcUserId()).orElse(false);
  }

  private List<MailDTO> buildMailForAskerList() {

    var asker = session.getUser();
    if (isNewNotificationToggleEnabled()
        && !wantsToReceiveNotificationsAboutNewMessage(session.getUser())) {
      return Lists.newArrayList();
    }

    var usernameTranscoder = new UsernameTranscoder();
    var consultantUsername = obtainConsultantUsername();
    var mailDTO =
        buildMailDtoForNewMessageNotificationAsker(
            asker.getEmail(),
            asker.getLanguageCode(),
            usernameTranscoder.decodeUsername(consultantUsername),
            usernameTranscoder.decodeUsername(asker.getUsername()));

    return singletonList(mailDTO);
  }

  private String obtainConsultantUsername() {
    if (isSessionBelongsToConsultant()) {
      return session.getConsultant().getUsername();
    } else {
      return consultantService
          .getConsultant(userId)
          .orElseThrow(
              () ->
                  new InternalServerErrorException(
                      String.format("Consultant with id %s not found.", userId)))
          .getUsername();
    }
  }

  private boolean isSessionBelongsToConsultant() {
    return nonNull(session.getConsultant()) && session.getConsultant().getId().equals(userId);
  }

  private boolean isAdviceSeekerWithEmail() {
    var email = session.getUser().getEmail();

    return isNotBlank(email) && !email.contains(emailDummySuffix);
  }

  private MailDTO buildMailDtoForNewMessageNotificationAsker(
      String email, LanguageCode languageCode, String consultantName, String askerName) {
    var templateAttributes = new ArrayList<TemplateDataDTO>();
    templateAttributes.add(new TemplateDataDTO().key("consultantName").value(consultantName));
    templateAttributes.add(new TemplateDataDTO().key("askerName").value(askerName));

    if (!multiTenancyEnabled) {
      templateAttributes.add(new TemplateDataDTO().key("url").value(applicationBaseUrl));
    } else {
      templateAttributes.addAll(tenantTemplateSupplier.getTemplateAttributes());
    }

    return new MailDTO()
        .template(TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER)
        .email(email)
        .language(languageOf(languageCode))
        .templateData(templateAttributes);
  }

  private static de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode languageOf(
      LanguageCode languageCode) {
    return de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.fromValue(
        languageCode.toString());
  }
}

package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationHelper.TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

/**
 * Supplier to provide mails to be sent when a feedback message has been written.
 */
@AllArgsConstructor
public class NewFeedbackEmailSupplier implements EmailSupplier {

  private final Session session;
  private final String rcFeedbackGroupId;
  private final String userId;
  private final String applicationBaseUrl;
  private final UserHelper userHelper;
  private final ConsultantService consultantService;
  private final RocketChatService rocketChatService;
  private final String rocketChatSystemUserId;

  /**
   * Generates feedback message notification mails sent to regarding consultants.
   *
   * @return a list of the generated {@link MailDTO}
   */
  @Override
  public List<MailDTO> generateEmails() throws RocketChatGetGroupMembersException {
    if (nonNull(session)) {
      return buildFeedbackMessageMailsForExistingSession();
    }
    LogService.logEmailNotificationFacadeError(String.format(
        "No session found for the rocket chat feedback group id %s.", rcFeedbackGroupId));

    return emptyList();
  }

  private List<MailDTO> buildFeedbackMessageMailsForExistingSession()
      throws RocketChatGetGroupMembersException {
    if (nonNull(session.getConsultant())) {
      return buildFeedbackMessageMailsForExistingConsultant();
    }
    LogService.logEmailNotificationFacadeError(String.format(
        "No consultant is assigned to the session found by rocket chat feedback group id %s.",
        rcFeedbackGroupId));

    return emptyList();
  }

  private List<MailDTO> buildFeedbackMessageMailsForExistingConsultant()
      throws RocketChatGetGroupMembersException {
    Optional<Consultant> sendingConsultantOptional = consultantService.getConsultant(userId);
    if (sendingConsultantOptional.isPresent()) {
      Consultant sendingConsultant = sendingConsultantOptional.get();
      return buildMailsDependingOnAuthor(sendingConsultant);
    }
    LogService.logEmailNotificationFacadeError(
        String.format("Consultant with id %s not found.", userId));

    return emptyList();
  }

  private List<MailDTO> buildMailsDependingOnAuthor(Consultant sendingConsultant)
      throws RocketChatGetGroupMembersException {
    if (areUsersEqual(userId, session.getConsultant())) {
      return buildNotificationMailsForAllOtherConsultants(sendingConsultant);
    }

    if (didAnotherConsultantWrite()) {
      return singletonList(buildMailForAssignedConsultant(sendingConsultant,
          session.getConsultant()));
    }
    return emptyList();
  }

  private boolean areUsersEqual(String userId, Consultant consultant) {
    return userId.equals(consultant.getId());
  }

  private List<MailDTO> buildNotificationMailsForAllOtherConsultants(Consultant sendingConsultant)
      throws RocketChatGetGroupMembersException {
    List<GroupMemberDTO> groupMembers = rocketChatService.getMembersOfGroup(rcFeedbackGroupId);
    if (isNotEmpty(groupMembers)) {
      return buildMailsForAllConsultantsExceptSystemUser(sendingConsultant, groupMembers);
    }

    LogService.logEmailNotificationFacadeError(String.format(
        "List of members for rocket chat feedback group id %s is empty.", rcFeedbackGroupId));
    return emptyList();
  }

  private List<MailDTO> buildMailsForAllConsultantsExceptSystemUser(Consultant sendingConsultant,
      List<GroupMemberDTO> groupMembers) {
    return groupMembers.stream()
        .filter(groupMemberDTO -> !rocketChatSystemUserId.equals(groupMemberDTO.get_id()))
        .map(this::toValidatedConsultant)
        .filter(Objects::nonNull)
        .filter(this::notHimselfAndNotAbsent)
        .map(consultant -> buildMailForAssignedConsultant(sendingConsultant, consultant))
        .collect(Collectors.toList());
  }

  private Consultant toValidatedConsultant(GroupMemberDTO groupMemberDTO) {
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

  private boolean notHimselfAndNotAbsent(Consultant consultant) {
    return isNotBlank(consultant.getEmail())
        && !areUsersEqual(userId, consultant)
        && !areUsersEqual(session.getConsultant().getId(), consultant)
        && !consultant.isAbsent();
  }

  private boolean didAnotherConsultantWrite() {
    return !areUsersEqual(userId, session.getConsultant())
        && !session.getConsultant().getEmail().isEmpty() && !session.getConsultant().isAbsent();
  }

  private MailDTO buildMailForAssignedConsultant(Consultant sendingConsultant,
      Consultant consultant) {
    String nameSender = sendingConsultant.getFullName();
    String nameRecipient = consultant.getFullName();
    String nameUser = userHelper.decodeUsername(session.getUser().getUsername());
    String email = consultant.getEmail();

    return buildMailDtoForFeedbackMessageNotification(email, nameSender, nameRecipient, nameUser);
  }

  private MailDTO buildMailDtoForFeedbackMessageNotification(String email, String nameSender,
      String nameRecipient, String nameUser) {
    return new MailDTO()
        .template(TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION)
        .email(email)
        .templateData(asList(
            new TemplateDataDTO().key("name_sender").value(nameSender),
            new TemplateDataDTO().key("name_recipient").value(nameRecipient),
            new TemplateDataDTO().key("name_user").value(nameUser),
            new TemplateDataDTO().key("url").value(applicationBaseUrl)));
  }

}

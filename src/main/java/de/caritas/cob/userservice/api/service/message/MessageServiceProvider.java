package de.caritas.cob.userservice.api.service.message;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostFurtherStepsMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.MessageHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.messageservice.generated.ApiClient;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.userservice.messageservice.generated.web.model.AliasOnlyMessageDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Service class to provide message transmission to Rocket.Chat via the MessageService.
 */
@Service
@RequiredArgsConstructor
public class MessageServiceProvider {

  private final @NonNull MessageControllerApi messageControllerApi;
  private final @NonNull RocketChatCredentialsProvider rocketChatCredentialsProvider;
  private final @NonNull UserHelper userHelper;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

  /**
   * Posts an enquiry message via the MessageService to the given Rocket.Chat group ID.
   *
   * @param message               Message
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @param rcGroupId             Rocket.Chat group ID
   * @param exceptionInformation  {@link CreateEnquiryExceptionInformation}
   * @throws RocketChatPostMessageException exception when posting the message fails
   */
  public void postEnquiryMessage(String message, RocketChatCredentials rocketChatCredentials,
      String rcGroupId, CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostMessageException {

    try {
      this.postMessage(message, rocketChatCredentials, rcGroupId);

    } catch (RestClientException exception) {
      throw new RocketChatPostMessageException(
          String.format("Could not post enquiry message to Rocket.Chat group %s with user %s",
              rcGroupId,
              rocketChatCredentials.getRocketChatUserId()), exception, exceptionInformation);
    }
  }

  private void postMessage(String message, RocketChatCredentials rcCredentials, String rcGroupId) {

    addDefaultHeaders(this.messageControllerApi.getApiClient());
    this.messageControllerApi.createMessage(rcCredentials.getRocketChatToken(),
        rcCredentials.getRocketChatUserId(), rcGroupId, new MessageDTO().message(message));
  }

  /**
   * Posts a welcome message as system user to the given Rocket.Chat group if configured in the
   * provided {@link ExtendedConsultingTypeResponseDTO}.
   *
   * @param rcGroupId                         Rocket.Chat group ID
   * @param user                              {@link User} who receives the message
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   * @param exceptionInformation              {@link CreateEnquiryExceptionInformation}
   * @throws RocketChatPostWelcomeMessageException exception when posting the welcome message fails
   */
  public void postWelcomeMessageIfConfigured(String rcGroupId, User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO,
      CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostWelcomeMessageException {

    if (!extendedConsultingTypeResponseDTO.getWelcomeMessage().getSendWelcomeMessage()) {
      return;
    }

    String welcomeMessage =
        MessageHelper.replaceUsernameInMessage(
            extendedConsultingTypeResponseDTO.getWelcomeMessage().getWelcomeMessageText(),
            userHelper.decodeUsername(user.getUsername()));

    try {
      this.postMessageAsSystemUser(welcomeMessage, rcGroupId);

    } catch (RestClientException | RocketChatUserNotInitializedException exception) {
      throw new RocketChatPostWelcomeMessageException(
          String.format("Could not post welcome message in Rocket.Chat group %s", rcGroupId),
          exception, exceptionInformation);
    }
  }

  private void postMessageAsSystemUser(String message, String rcGroupId)
      throws RocketChatUserNotInitializedException {

    RocketChatCredentials systemUser = rocketChatCredentialsProvider.getSystemUser();
    this.postMessage(message, systemUser, rcGroupId);
  }

  /**
   * Posts an alias only message and/or save session data message as system user in the provided
   * Rocket.Chat group ID.
   *
   * @param rcGroupId                         Rocket.Chat group ID
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   * @param exceptionInformation              {@link CreateEnquiryExceptionInformation}
   */
  public void postFurtherStepsOrSaveSessionDataMessageIfConfigured(String rcGroupId,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO,
      CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostFurtherStepsMessageException {

    if (extendedConsultingTypeResponseDTO.getSendFurtherStepsMessage()) {
      this.postAliasOnlyMessage(rcGroupId, MessageType.FURTHER_STEPS, exceptionInformation);
    }

    if (extendedConsultingTypeResponseDTO.getSendSaveSessionDataMessage()) {
      this.postAliasOnlyMessage(rcGroupId, MessageType.UPDATE_SESSION_DATA, exceptionInformation);
    }
  }

  private void postAliasOnlyMessage(String rcGroupId, MessageType messageType,
      CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostFurtherStepsMessageException {
    addDefaultHeaders(this.messageControllerApi.getApiClient());
    try {
      this.messageControllerApi.saveAliasOnlyMessage(rcGroupId, new AliasOnlyMessageDTO()
          .messageType(messageType));

    } catch (RestClientException exception) {
      throw new RocketChatPostFurtherStepsMessageException(String
          .format("Could not post further steps message in Rocket.Chat group with id %s",
              rcGroupId), exception, exceptionInformation);
    }
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}

package de.caritas.cob.userservice.api.service.message;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.config.apiclient.MessageServiceApiControllerFactory;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostFurtherStepsMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.messageservice.generated.ApiClient;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.userservice.messageservice.generated.web.model.AliasMessageDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.AliasOnlyMessageDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageResponseDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageType;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/** Service class to provide message transmission to Rocket.Chat via the MessageService. */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceProvider {

  private final @NonNull MessageServiceApiControllerFactory messageServiceApiControllerFactory;
  private final @NonNull RocketChatCredentialsProvider rocketChatCredentialsProvider;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  /**
   * Posts an enquiry message via the MessageService to the given Rocket.Chat group ID.
   *
   * @param rocketChatData rocket chat data necessary for sending messages
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   * @return {@link MessageResponseDTO}
   * @throws RocketChatPostMessageException exception when posting the message fails
   */
  public MessageResponseDTO postEnquiryMessage(
      RocketChatData rocketChatData, CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostMessageException {

    try {
      return this.postMessage(rocketChatData);
    } catch (RestClientException exception) {
      throw new RocketChatPostMessageException(
          String.format(
              "Could not post enquiry message to Rocket.Chat group %s with user %s",
              rocketChatData.getRcGroupId(),
              rocketChatData.getRocketChatCredentials().getRocketChatUserId()),
          exception,
          exceptionInformation);
    }
  }

  private MessageResponseDTO postMessage(RocketChatData rocketChatData) {
    var rcCredentials = rocketChatData.getRocketChatCredentials();
    MessageControllerApi controllerApi = messageServiceApiControllerFactory.createControllerApi();
    addDefaultHeaders(controllerApi.getApiClient());
    var message = new MessageDTO().message(rocketChatData.getMessage()).t(rocketChatData.getType());
    return controllerApi.createMessage(
        rcCredentials.getRocketChatToken(),
        rcCredentials.getRocketChatUserId(),
        rocketChatData.getRcGroupId(),
        message);
  }

  /**
   * Posts a welcome message as system user to the given Rocket.Chat group if configured in the
   * provided {@link ExtendedConsultingTypeResponseDTO}.
   *
   * @param rcGroupId Rocket.Chat group ID
   * @param user {@link User} who receives the message
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   * @throws RocketChatPostWelcomeMessageException exception when posting the welcome message fails
   */
  public void postWelcomeMessageIfConfigured(
      String rcGroupId,
      User user,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO,
      CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostWelcomeMessageException {

    var welcomeMessageDTO = extendedConsultingTypeResponseDTO.getWelcomeMessage();
    if (isNull(welcomeMessageDTO) || isFalse(welcomeMessageDTO.getSendWelcomeMessage())) {
      return;
    }

    var username = new UsernameTranscoder().decodeUsername(user.getUsername());
    var placeholderMap = Map.of("username", username);
    var stringSubstitutor = new StringSubstitutor(placeholderMap, "${", "}");
    var welcomeMessage = stringSubstitutor.replace(welcomeMessageDTO.getWelcomeMessageText());

    try {
      this.postMessageAsSystemUser(welcomeMessage, rcGroupId);

    } catch (RestClientException | RocketChatUserNotInitializedException exception) {
      log.error("Exception calling RocketChat API: {}", exception.getMessage());
      throw new RocketChatPostWelcomeMessageException(
          String.format("Could not post welcome message in Rocket.Chat group %s", rcGroupId),
          exception,
          exceptionInformation);
    }
  }

  private void postMessageAsSystemUser(String message, String rcGroupId)
      throws RocketChatUserNotInitializedException {
    var systemUserCredentials = rocketChatCredentialsProvider.getSystemUser();
    var rocketChatData = new RocketChatData(message, systemUserCredentials, rcGroupId);
    this.postMessage(rocketChatData);
  }

  /**
   * Posts an alias only message as system user in the provided Rocket.Chat group ID.
   *
   * @param rcGroupId Rocket.Chat group ID
   * @param extendedConsultingTypeResponseDTO {@link ExtendedConsultingTypeResponseDTO}
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   */
  public void postFurtherStepsIfConfigured(
      String rcGroupId,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO,
      CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostFurtherStepsMessageException {

    if (isTrue(extendedConsultingTypeResponseDTO.getSendFurtherStepsMessage())) {
      this.postAliasOnlyMessage(rcGroupId, MessageType.FURTHER_STEPS, exceptionInformation);
    }
  }

  public MessageResponseDTO assignUserToRocketChatGroup(
      String rcGroupId, CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostFurtherStepsMessageException {
    MessageControllerApi controllerApi = messageServiceApiControllerFactory.createControllerApi();
    addDefaultHeaders(controllerApi.getApiClient());
    try {
      return controllerApi.saveAliasMessageWithContent(
          rcGroupId, new AliasMessageDTO().messageType(MessageType.INITIAL_APPOINTMENT_DEFINED));

    } catch (RestClientException exception) {
      throw new RocketChatPostFurtherStepsMessageException(
          String.format(
              "Could not post further steps message in Rocket.Chat group with id %s", rcGroupId),
          exception,
          exceptionInformation);
    }
  }

  private void postAliasOnlyMessage(
      String rcGroupId,
      MessageType messageType,
      CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostFurtherStepsMessageException {
    MessageControllerApi controllerApi = messageServiceApiControllerFactory.createControllerApi();
    addDefaultHeaders(controllerApi.getApiClient());
    try {
      controllerApi.saveAliasOnlyMessage(
          rcGroupId, new AliasOnlyMessageDTO().messageType(messageType));

    } catch (RestClientException exception) {
      throw new RocketChatPostFurtherStepsMessageException(
          String.format(
              "Could not post further steps message in Rocket.Chat group with id %s", rcGroupId),
          exception,
          exceptionInformation);
    }
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}

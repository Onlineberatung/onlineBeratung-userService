package de.caritas.cob.userservice.api.service.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.MessageHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.messageService.MessageDTO;
import de.caritas.cob.userservice.api.repository.user.User;

/**
 *
 * Helper class to communicate with the MessageService
 *
 */

@Component
public class MessageServiceHelper {

  @Value("${message.service.api.post.message}")
  private String messageServiceApiPostMessageUrl;

  private final RestTemplate restTemplate;
  private final ServiceHelper serviceHelper;
  private final RocketChatCredentialsHelper rcCredentialHelper;
  private final UserHelper userHelper;

  @Autowired
  public MessageServiceHelper(RestTemplate restTemplate, ServiceHelper serviceHelper,
      RocketChatCredentialsHelper rcCredentialHelper, UserHelper userHelper) {
    this.serviceHelper = serviceHelper;
    this.restTemplate = restTemplate;
    this.rcCredentialHelper = rcCredentialHelper;
    this.userHelper = userHelper;
  }

  /**
   * Calls the MesageService API call for posting a message to a Rocket.Chat group. Throws
   * {@link RocketChatPostMessageException} when the call fails.
   *
   * @param message Message
   * @param rcUserId Rocket.Chat user ID
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @throws RocketChatPostMessageException
   */
  /**
   * Calls the MesageService API call for posting a message to a Rocket.Chat group. Throws
   * {@link RocketChatPostMessageException} when the call fails.
   *
   * @param message Message
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @param rcGroupId Rocket.Chat group ID
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   * @throws RocketChatPostMessageException
   */
  public void postMessage(String message, RocketChatCredentials rocketChatCredentials,
      String rcGroupId, CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostMessageException {

    ResponseEntity<Void> response = null;
    MessageDTO messageDTO = new MessageDTO(message);

    try {
      HttpHeaders header =
          serviceHelper.getRocketChatAndCsrfHttpHeaders(rocketChatCredentials, rcGroupId);
      HttpEntity<MessageDTO> request = new HttpEntity<MessageDTO>(messageDTO, header);

      response = restTemplate.exchange(messageServiceApiPostMessageUrl, HttpMethod.POST, request,
          Void.class);

    } catch (Exception ex) {
      throw new RocketChatPostMessageException(
          String.format("Could not post message to Rocket.Chat group %s with user %s", rcGroupId,
              rocketChatCredentials.getRocketChatUserId()),
          ex, exceptionInformation);
    }

    if (response == null || response.getStatusCode() != HttpStatus.CREATED) {
      throw new RocketChatPostMessageException(
          String.format("Could not post message to Rocket.Chat group %s with user %s", rcGroupId,
              rocketChatCredentials.getRocketChatUserId()),
          exceptionInformation);
    }
  }

  /**
   * Posts a welcome message as system user to the given Rocket.Chat group
   *
   * @param rcGroupId Rocket.Chat group ID
   * @param user {@link User} who receives the message
   * @param consultingTypeSettings {@link ConsultingTypeSettings}
   * @throws RocketChatPostWelcomeMessageException
   */
  public void postWelcomeMessageIfConfigured(String rcGroupId, User user,
      ConsultingTypeSettings consultingTypeSettings,
      CreateEnquiryExceptionInformation exceptionInformation)
      throws RocketChatPostWelcomeMessageException {

    if (consultingTypeSettings.isSendWelcomeMessage()) {
      String welcomeMessage =
          MessageHelper.replaceUsernameInMessage(consultingTypeSettings.getWelcomeMessage(),
              userHelper.decodeUsername(user.getUsername()));

      try {
        this.postMessageAsSystemUser(welcomeMessage, rcGroupId);

      } catch (RocketChatPostMessageException | RocketChatUserNotInitializedException exception) {
        throw new RocketChatPostWelcomeMessageException(
            String.format("Could not post welcome message in Rocket.Chat group %s", rcGroupId),
            exception, exceptionInformation);
      }
    }
  }

  /**
   * Posts a message as system user to the given Rocket.Chat group.
   *
   * @param message Message
   * @param rcGroupId Rocket.Chat group ID
   * @throws RocketChatPostMessageException
   * @throws RocketChatUserNotInitializedException
   */
  private void postMessageAsSystemUser(String message, String rcGroupId)
      throws RocketChatPostMessageException, RocketChatUserNotInitializedException {

    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
    this.postMessage(message, systemUser, rcGroupId, null);
  }

}

package de.caritas.cob.UserService.api.service.helper;

import de.caritas.cob.UserService.api.model.rocketChat.RocketChatCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.UserService.api.exception.MessageServiceHelperException;
import de.caritas.cob.UserService.api.model.messageService.MessageDTO;
import de.caritas.cob.UserService.api.service.LogService;

/**
 * 
 * Helper class to communicate with the MessageService
 *
 */

@Component
public class MessageServiceHelper {

  @Value("${message.service.api.post.message}")
  private String messageServiceApiPostMessageUrl;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ServiceHelper serviceHelper;

  @Autowired
  private LogService logService;

  @Autowired
  private RocketChatCredentialsHelper rcCredentialHelper;

  /**
   * Calls the MesageService API call for posting a message to Rocket.Chat and returns true/false if
   * message has been successfully submitted.
   * 
   * @param message
   * @param rcUserId
   * @param rcToken
   * @param rcGroupId
   * @return
   */
  public boolean postMessage(String message, String rcUserId, String rcToken, String rcGroupId) {

    ResponseEntity<Void> response = null;
    MessageDTO messageDTO = new MessageDTO(message);

    try {
      HttpHeaders header =
          serviceHelper.getRocketChatAndCsrfHttpHeaders(rcUserId, rcToken, rcGroupId);
      HttpEntity<MessageDTO> request = new HttpEntity<MessageDTO>(messageDTO, header);

      response = restTemplate.exchange(messageServiceApiPostMessageUrl, HttpMethod.POST, request,
          Void.class);

    } catch (Exception ex) {
      throw new MessageServiceHelperException(ex);
    }

    if (response != null) {
      if (response.getStatusCode() == HttpStatus.CREATED) {
        return true;
      } else {
        logService.logMessageServiceHelperException(
            "MessageService API call failed with " + response.getStatusCodeValue());
      }
    }

    return false;
  }

  /**
   * 
   * Calls the MesageService API call for posting a message to Rocket.Chat as system user and
   * returns true/false if message has been successfully submitted.
   * 
   * @param message
   * @param rcGroupId
   * @return
   */
  public boolean postMessageAsSystemUser(String message, String rcGroupId) {
    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
    return this.postMessage(message, systemUser.getRocketChatUserId(),
        systemUser.getRocketChatToken(), rcGroupId);
  }

}

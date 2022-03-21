package de.caritas.cob.userservice.api.actions.session;

import static de.caritas.cob.userservice.messageservice.generated.web.model.MessageType.FINISHED_CONVERSATION;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.messageservice.generated.ApiClient;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.userservice.messageservice.generated.web.model.AliasOnlyMessageDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to post a conversation finished alias message in rocket chat via the message service.
 */
@Component
@RequiredArgsConstructor
public class PostConversationFinishedAliasMessageActionCommand implements ActionCommand<Session> {

  private final @NonNull MessageControllerApi messageControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull IdentityClient identityClient;

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  /**
   * Posts a {@link AliasOnlyMessageDTO} with type finished conversation into rocket chat.
   *
   * @param actionTarget the session containing the rocket chat group id
   */
  @Override
  public void execute(Session actionTarget) {
    if (nonNull(actionTarget) && isNotBlank(actionTarget.getGroupId())) {
      addDefaultHeaders(messageControllerApi.getApiClient());
      this.messageControllerApi.saveAliasOnlyMessage(actionTarget.getGroupId(),
          new AliasOnlyMessageDTO().messageType(FINISHED_CONVERSATION));
    }
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var keycloakLoginResponseDTO = identityClient.loginUser(
        keycloakTechnicalUsername, keycloakTechnicalPassword
    );
    var headers = this.securityHeaderSupplier
        .getKeycloakAndCsrfHttpHeaders(keycloakLoginResponseDTO.getAccessToken());
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}

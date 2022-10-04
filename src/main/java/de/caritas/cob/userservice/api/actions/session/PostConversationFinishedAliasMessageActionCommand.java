package de.caritas.cob.userservice.api.actions.session;

import static de.caritas.cob.userservice.messageservice.generated.web.model.MessageType.FINISHED_CONVERSATION;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.messageservice.generated.ApiClient;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.userservice.messageservice.generated.web.model.AliasOnlyMessageDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Action to post a conversation finished alias message in rocket chat via the message service. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostConversationFinishedAliasMessageActionCommand implements ActionCommand<Session> {

  private final @NonNull MessageControllerApi messageControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull IdentityClientConfig identityClientConfig;

  /**
   * Posts a {@link AliasOnlyMessageDTO} with type finished conversation into rocket chat.
   *
   * @param actionTarget the session containing the rocket chat group id
   */
  @Override
  public void execute(Session actionTarget) {
    if (nonNull(actionTarget) && isNotBlank(actionTarget.getGroupId())) {
      try {
        addDefaultHeaders(messageControllerApi.getApiClient());
        this.messageControllerApi.saveAliasOnlyMessage(
            actionTarget.getGroupId(),
            new AliasOnlyMessageDTO().messageType(FINISHED_CONVERSATION));
      } catch (Exception e) {
        log.error("Unable to post conversation finished message");
        log.error(getStackTrace(e));
      }
    }
  }

  @SuppressWarnings("Duplicates")
  private void addDefaultHeaders(ApiClient apiClient) {
    var techUser = identityClientConfig.getTechnicalUser();
    var keycloakLogin = identityClient.loginUser(techUser.getUsername(), techUser.getPassword());
    var headers =
        securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders(keycloakLogin.getAccessToken());
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}

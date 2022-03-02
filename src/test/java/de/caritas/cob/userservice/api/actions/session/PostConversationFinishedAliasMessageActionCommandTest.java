package de.caritas.cob.userservice.api.actions.session;

import static de.caritas.cob.userservice.messageservice.generated.web.model.MessageType.FINISHED_CONVERSATION;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.model.keycloak.login.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.KeycloakService;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.userservice.messageservice.generated.web.model.AliasOnlyMessageDTO;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
class PostConversationFinishedAliasMessageActionCommandTest {

  @InjectMocks
  private PostConversationFinishedAliasMessageActionCommand actionCommand;

  @Mock
  private MessageControllerApi messageControllerApi;

  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;

  @Mock
  private KeycloakService keycloakService;

  @ParameterizedTest
  @MethodSource("sessionsWithoutInteractionsExpected")
  void execute_Should_doNothing_When_sessionIsNullOrWithoutRcRooms(Session session) {
    this.actionCommand.execute(session);

    verifyNoMoreInteractions(this.keycloakService);
    verifyNoMoreInteractions(this.securityHeaderSupplier);
    verifyNoMoreInteractions(this.messageControllerApi);
  }

  private static List<Session> sessionsWithoutInteractionsExpected() {
    var sessionWithEmptyRcRoomId = new Session();
    sessionWithEmptyRcRoomId.setGroupId("");
    return asList(null, new Session(), sessionWithEmptyRcRoomId);
  }

  @Test
  void execute_Should_postFinishedConversationMessage_When_sessionHasGroupId() {
    var keycloakLoginResponseDTO = new KeycloakLoginResponseDTO();
    keycloakLoginResponseDTO.setAccessToken("token");
    when(this.keycloakService.loginUser(any(), any())).thenReturn(keycloakLoginResponseDTO);
    when(this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders(any())).thenReturn(
        new HttpHeaders());
    var session = new EasyRandom().nextObject(Session.class);

    this.actionCommand.execute(session);

    verify(this.keycloakService, times(1)).loginUser(null, null);
    verify(this.securityHeaderSupplier, times(1)).getKeycloakAndCsrfHttpHeaders("token");
    var expectedMessageType = new AliasOnlyMessageDTO().messageType(FINISHED_CONVERSATION);
    verify(this.messageControllerApi, times(1))
        .saveAliasOnlyMessage(session.getGroupId(), expectedMessageType);
  }

}

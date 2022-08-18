package de.caritas.cob.userservice.api.conversation.service;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.conversation.provider.ConversationListProvider;
import de.caritas.cob.userservice.api.conversation.registry.ConversationListProviderRegistry;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationListResolverTest {

  @InjectMocks private ConversationListResolver conversationListResolver;

  @Mock private ConversationListProviderRegistry conversationListProviderRegistry;

  @Mock private ConsultantSessionListResponseDTO consultantSessionListResponseDTO;

  @Mock private ConversationListProvider conversationListProvider;

  @Test
  void resolveConversations_Should_returnExpectedResponse_When_paramsAreValid() {
    whenConversationListProviderReturnsAnonymousResponseSessions(
        List.of(mock(ConsultantSessionResponseDTO.class)));

    var responseDTO =
        this.conversationListResolver.resolveConversations(0, 1, ANONYMOUS_ENQUIRY, "");

    assertThat(responseDTO, is(consultantSessionListResponseDTO));
  }

  private void whenConversationListProviderReturnsAnonymousResponseSessions(
      List<ConsultantSessionResponseDTO> responseSessions) {
    when(this.conversationListProvider.buildConversations(any()))
        .thenReturn(this.consultantSessionListResponseDTO);
    when(this.conversationListProviderRegistry.findByConversationType(ANONYMOUS_ENQUIRY))
        .thenReturn(this.conversationListProvider);
    when(this.consultantSessionListResponseDTO.getSessions()).thenReturn(responseSessions);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void resolveConversations_Should_throwException_When_noSessionsAreFound(
      List<ConsultantSessionResponseDTO> emptySessions) {
    whenConversationListProviderReturnsAnonymousResponseSessions(emptySessions);

    assertThrows(
        NoContentException.class,
        () -> {
          this.conversationListResolver.resolveConversations(0, 1, ANONYMOUS_ENQUIRY, "");
        });
  }
}

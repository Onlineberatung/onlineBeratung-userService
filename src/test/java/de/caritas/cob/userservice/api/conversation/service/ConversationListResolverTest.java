package de.caritas.cob.userservice.api.conversation.service;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.conversation.provider.ConversationListProvider;
import de.caritas.cob.userservice.api.conversation.registry.ConversationListProviderRegistry;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConversationListResolverTest {

  @InjectMocks
  private ConversationListResolver conversationListResolver;

  @Mock
  private ConversationListProviderRegistry conversationListProviderRegistry;

  @Mock
  private ConsultantSessionListResponseDTO consultantSessionListResponseDTO;

  @Mock
  private ConversationListProvider conversationListProvider;

  @Test
  public void resolveConversations_Should_returnExpectedResponse_When_paramsAreValid() {
    when(this.conversationListProvider.buildConversations(any()))
        .thenReturn(this.consultantSessionListResponseDTO);
    when(this.conversationListProviderRegistry.findByConversationType(ANONYMOUS_ENQUIRY))
        .thenReturn(this.conversationListProvider);

    ConsultantSessionListResponseDTO responseDTO = this.conversationListResolver
        .resolveConversations("token", 0, 1, ANONYMOUS_ENQUIRY);

    assertThat(responseDTO, is(consultantSessionListResponseDTO));
  }

}

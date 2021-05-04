package de.caritas.cob.userservice.api.conversation.service;

import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.conversation.registry.ConversationListProviderRegistry;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resolver class for all conversation lists.
 */
@Service
@RequiredArgsConstructor
public class ConversationListResolver {

  private final @NonNull ConversationListProviderRegistry conversationListProviderRegistry;

  /**
   * Resolves the requested converation list by given {@link ConversationListType}.
   *
   * @param offset the current offset
   * @param count the requested limit
   * @param conversationType the given {@link ConversationListType}
   * @return the relevant {@link ConsultantSessionListResponseDTO}
   */
  public ConsultantSessionListResponseDTO resolveConversations(Integer offset,
      Integer count, ConversationListType conversationType) {

    PageableListRequest pageableListRequest = PageableListRequest.builder()
        .offset(offset)
        .count(count)
        .build();

    return this.conversationListProviderRegistry.findByConversationType(conversationType)
        .buildConversations(pageableListRequest);
  }

}

package de.caritas.cob.userservice.api.conversation.service;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.conversation.registry.ConversationListProviderRegistry;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/** Resolver class for all conversation lists. */
@Service
@RequiredArgsConstructor
public class ConversationListResolver {

  private final @NonNull ConversationListProviderRegistry conversationListProviderRegistry;

  /**
   * Resolves the requested conversation list by given {@link ConversationListType}.
   *
   * @param offset the current offset
   * @param count the requested limit
   * @param conversationType the given {@link ConversationListType}
   * @return the relevant {@link ConsultantSessionListResponseDTO}
   */
  public ConsultantSessionListResponseDTO resolveConversations(
      Integer offset, Integer count, ConversationListType conversationType, String rcToken) {

    var pageableListRequest =
        PageableListRequest.builder().offset(offset).count(count).rcToken(rcToken).build();

    var responseDto =
        this.conversationListProviderRegistry
            .findByConversationType(conversationType)
            .buildConversations(pageableListRequest);
    if (CollectionUtils.isNotEmpty(responseDto.getSessions())) {
      return responseDto;
    }

    var exceptionMessage =
        String.format("No sessions found for parameters offset=%s, count=%s", offset, count);
    throw new NoContentException(exceptionMessage);
  }
}

package de.caritas.cob.userservice.api.conversation.provider;

import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;

/**
 * The basic provider for all conversation lists.
 */
public interface ConversationListProvider {

  /**
   * Builds the {@link ConsultantSessionListResponseDTO}.
   *
   * @param pageableListRequest the pageable request
   * @return the relevant {@link ConsultantSessionListResponseDTO}
   */
  ConsultantSessionListResponseDTO buildConversations(PageableListRequest pageableListRequest);

  /**
   * Returns the {@link ConversationListType} the implementation is accountable for.
   *
   * @return the relevant {@link ConversationListType}
   */
  ConversationListType providedType();

}

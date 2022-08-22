package de.caritas.cob.userservice.api.conversation.provider;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;

/** The basic provider for all conversation lists. */
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

  default int obtainPageByOffsetAndCount(PageableListRequest pageableListRequest) {
    if (pageableListRequest.getCount() < 1) {
      throw new InternalServerErrorException("Pageable count attribute must be greater than zero");
    }
    return pageableListRequest.getOffset() / pageableListRequest.getCount();
  }
}

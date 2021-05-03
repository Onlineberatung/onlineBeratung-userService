package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;

import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import org.springframework.stereotype.Service;

/**
 * {@link ConversationListProvider} to provide anonymous enquiry conversations.
 */
@Service
public class AnonymousEnquiryConversationListProvider implements ConversationListProvider {

  /**
   * Builds the {@link ConsultantSessionListResponseDTO}.
   *
   * @param pageableListRequest the pageable request
   * @return the relevant {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ConsultantSessionListResponseDTO buildConversations(
      PageableListRequest pageableListRequest) {
    return null;
  }

  /**
   * Returns the {@link ConversationListType} the implementation is accountable for.
   *
   * @return the relevant {@link ConversationListType}
   */
  @Override
  public ConversationListType providedType() {
    return ANONYMOUS_ENQUIRY;
  }
}

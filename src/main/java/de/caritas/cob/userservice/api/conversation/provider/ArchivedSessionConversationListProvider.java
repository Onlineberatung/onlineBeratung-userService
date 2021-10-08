package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ARCHIVED_SESSION;

import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionEnricher;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import lombok.NonNull;
import org.springframework.stereotype.Service;

/**
 * {@link ConversationListProvider} to provide archived session conversations.
 */
@Service
public class ArchivedSessionConversationListProvider extends DefaultConversationListProvider {

  private final SessionService sessionService;
  private final ValidatedUserAccountProvider userAccountProvider;

  public ArchivedSessionConversationListProvider(
      @NonNull ValidatedUserAccountProvider userAccountProvider,
      @NonNull ConsultantSessionEnricher consultantSessionEnricher,
      @NonNull SessionService sessionService) {
    super(consultantSessionEnricher);
    this.sessionService = sessionService;
    this.userAccountProvider = userAccountProvider;
  }

  /**
   * Builds the {@link ConsultantSessionListResponseDTO}.
   *
   * @param pageableListRequest the pageable request
   * @return the relevant {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ConsultantSessionListResponseDTO buildConversations(
      PageableListRequest pageableListRequest) {
    var consultant = this.userAccountProvider.retrieveValidatedConsultant();

    return buildConversations(pageableListRequest,
        consultant,
        () -> this.sessionService.getArchivedSessionsForConsultant(consultant));
  }

  /**
   * Returns the {@link ConversationListType} the implementation is accountable for.
   *
   * @return the relevant {@link ConversationListType}
   */
  @Override
  public ConversationListType providedType() {
    return ARCHIVED_SESSION;
  }
}

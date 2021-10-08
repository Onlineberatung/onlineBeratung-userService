package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ARCHIVED_TEAM_SESSION;

import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionEnricher;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import lombok.NonNull;
import org.springframework.stereotype.Service;

/**
 * {@link ConversationListProvider} to provide archived team session conversations.
 */
@Service
public class ArchivedTeamSessionConversationListProvider extends DefaultConversationListProvider {

  private final SessionService sessionService;
  private final ValidatedUserAccountProvider userAccountProvider;

  public ArchivedTeamSessionConversationListProvider(
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
        () -> this.sessionService.getArchivedTeamSessionsForConsultant(consultant));
  }

  /**
   * Returns the {@link ConversationListType} the implementation is accountable for.
   *
   * @return the relevant {@link ConversationListType}
   */
  @Override
  public ConversationListType providedType() {
    return ARCHIVED_TEAM_SESSION;
  }
}

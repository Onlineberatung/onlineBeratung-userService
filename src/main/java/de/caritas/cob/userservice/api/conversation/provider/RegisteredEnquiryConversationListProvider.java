package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;

import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionEnricher;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.RegistrationType;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;

/**
 * {@link ConversationListProvider} to provide registered enquiry conversations.
 */
@Service
@RequiredArgsConstructor
public class RegisteredEnquiryConversationListProvider implements ConversationListProvider {

  private final @NonNull ValidatedUserAccountProvider userAccountProvider;
  private final @NonNull SessionService sessionService;
  private final @NonNull ConsultantSessionEnricher consultantSessionEnricher;

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

    PagedListHolder<ConsultantSessionResponseDTO> enquiriesForConsultant = new PagedListHolder<>(
        this.sessionService.getEnquiriesForConsultant(consultant, RegistrationType.REGISTERED));

    enquiriesForConsultant.setPage(pageableListRequest.getOffset());
    enquiriesForConsultant.setPageSize(pageableListRequest.getCount());

    List<ConsultantSessionResponseDTO> pageList = enquiriesForConsultant.getPageList();
    pageList.forEach(sessionResponse -> this.consultantSessionEnricher
        .updateRequiredConsultantSessionValues(sessionResponse,
            pageableListRequest.getRcToken(), consultant));

    return new ConsultantSessionListResponseDTO()
        .sessions(pageList)
        .offset(pageableListRequest.getOffset())
        .count(pageList.size())
        .total(enquiriesForConsultant.getNrOfElements());
  }

  /**
   * Returns the {@link ConversationListType} the implementation is accountable for.
   *
   * @return the relevant {@link ConversationListType}
   */
  @Override
  public ConversationListType providedType() {
    return REGISTERED_ENQUIRY;
  }
}

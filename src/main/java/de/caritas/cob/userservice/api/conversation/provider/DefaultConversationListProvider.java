package de.caritas.cob.userservice.api.conversation.provider;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionEnricher;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.support.PagedListHolder;

/** Default basic provider for conversation lists. */
@RequiredArgsConstructor
public abstract class DefaultConversationListProvider implements ConversationListProvider {

  private final @NonNull ConsultantSessionEnricher consultantSessionEnricher;

  /** {@inheritDoc} */
  protected ConsultantSessionListResponseDTO buildConversations(
      PageableListRequest pageableListRequest,
      Consultant consultant,
      List<ConsultantSessionResponseDTO> sessionList) {

    PagedListHolder<ConsultantSessionResponseDTO> enquiriesForConsultant =
        new PagedListHolder<>(sessionList);

    enquiriesForConsultant.setPage(obtainPageByOffsetAndCount(pageableListRequest));
    enquiriesForConsultant.setPageSize(pageableListRequest.getCount());

    List<ConsultantSessionResponseDTO> pageList = enquiriesForConsultant.getPageList();
    consultantSessionEnricher.updateRequiredConsultantSessionValues(
        pageList, pageableListRequest.getRcToken(), consultant);

    return new ConsultantSessionListResponseDTO()
        .sessions(pageList)
        .offset(pageableListRequest.getOffset())
        .count(pageList.size())
        .total(enquiriesForConsultant.getNrOfElements());
  }
}

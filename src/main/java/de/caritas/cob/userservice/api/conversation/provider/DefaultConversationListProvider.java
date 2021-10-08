package de.caritas.cob.userservice.api.conversation.provider;

import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionEnricher;
import java.util.List;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.support.PagedListHolder;

@RequiredArgsConstructor
public abstract class DefaultConversationListProvider implements ConversationListProvider {

  private final @NonNull ConsultantSessionEnricher consultantSessionEnricher;

  protected ConsultantSessionListResponseDTO buildConversations(
      PageableListRequest pageableListRequest,
      Consultant consultant,
      Supplier<List<ConsultantSessionResponseDTO>> sessionSupplier) {

    PagedListHolder<ConsultantSessionResponseDTO> enquiriesForConsultant = new PagedListHolder<>(
        sessionSupplier.get());

    enquiriesForConsultant.setPage(obtainPageByOffsetAndCount(pageableListRequest));
    enquiriesForConsultant.setPageSize(pageableListRequest.getCount());

    List<ConsultantSessionResponseDTO> pageList = enquiriesForConsultant.getPageList();
    consultantSessionEnricher
        .updateRequiredConsultantSessionValues(pageList, pageableListRequest.getRcToken(),
            consultant);

    return new ConsultantSessionListResponseDTO()
        .sessions(pageList)
        .offset(pageableListRequest.getOffset())
        .count(pageList.size())
        .total(enquiriesForConsultant.getNrOfElements());
  }
}

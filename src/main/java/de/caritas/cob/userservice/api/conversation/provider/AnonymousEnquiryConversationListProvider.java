package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static de.caritas.cob.userservice.api.repository.session.RegistrationType.ANONYMOUS;

import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionEnricher;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.session.SessionMapper;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * {@link ConversationListProvider} to provide anonymous enquiry conversations.
 */
@Service
@RequiredArgsConstructor
public class AnonymousEnquiryConversationListProvider implements ConversationListProvider {

  private final @NonNull ValidatedUserAccountProvider userAccountProvider;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull AgencyService agencyService;
  private final @NonNull ConsultantSessionEnricher consultantSessionEnricher;

  /**
   * {@inheritDoc}
   */
  @Override
  public ConsultantSessionListResponseDTO buildConversations(
      PageableListRequest pageableListRequest) {
    var consultant = this.userAccountProvider.retrieveValidatedConsultant();
    Set<Integer> relatedConsultingTypes = retrieveRelatedConsultingTypes(consultant);

    Page<Session> anonymousSessionsOfConsultant = queryForRelevantSessions(
        pageableListRequest, relatedConsultingTypes);

    List<ConsultantSessionResponseDTO> sessions = anonymousSessionsOfConsultant.stream()
        .map(session -> new SessionMapper().toConsultantSessionDto(session))
        .collect(Collectors.toList());

    this.consultantSessionEnricher.updateRequiredConsultantSessionValues(sessions,
        pageableListRequest.getRcToken(), consultant);

    return new ConsultantSessionListResponseDTO()
        .sessions(sessions)
        .count(sessions.size())
        .offset(pageableListRequest.getOffset())
        .total((int) anonymousSessionsOfConsultant.getTotalElements());
  }

  private Set<Integer> retrieveRelatedConsultingTypes(Consultant consultant) {
    List<Long> consultantAgencyIds = consultant.getConsultantAgencies().stream()
        .map(ConsultantAgency::getAgencyId)
        .collect(Collectors.toList());
    return this.agencyService.getAgencies(consultantAgencyIds)
        .stream().map(AgencyDTO::getConsultingType)
        .collect(Collectors.toSet());
  }

  private Page<Session> queryForRelevantSessions(PageableListRequest pageableListRequest,
      Set<Integer> relatedConsultingTypes) {
    var requestedPage = obtainPageByOffsetAndCount(pageableListRequest);
    var pageable = PageRequest.of(requestedPage, pageableListRequest.getCount());

    return this.sessionRepository
        .findByConsultingTypeIdInAndRegistrationTypeAndStatusOrderByCreateDateAsc(
            relatedConsultingTypes, ANONYMOUS, SessionStatus.NEW, pageable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConversationListType providedType() {
    return ANONYMOUS_ENQUIRY;
  }
}

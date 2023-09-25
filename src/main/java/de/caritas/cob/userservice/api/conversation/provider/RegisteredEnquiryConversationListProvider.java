package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.sessionlist.ConsultantSessionEnricher;
import de.caritas.cob.userservice.api.service.user.UserAccountService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

/** {@link ConversationListProvider} to provide registered enquiry conversations. */
@Service
public class RegisteredEnquiryConversationListProvider extends DefaultConversationListProvider {

  private final @NonNull UserAccountService userAccountProvider;
  private final @NonNull SessionService sessionService;

  public RegisteredEnquiryConversationListProvider(
      @NonNull UserAccountService userAccountProvider,
      @NonNull ConsultantSessionEnricher consultantSessionEnricher,
      @NonNull SessionService sessionService) {
    super(consultantSessionEnricher);
    this.sessionService = sessionService;
    this.userAccountProvider = userAccountProvider;
  }

  /** {@inheritDoc} */
  @Override
  public ConsultantSessionListResponseDTO buildConversations(PageableListRequest request) {
    var consultant = this.userAccountProvider.retrieveValidatedConsultant();
    var registeredEnquiries = sessionService.getRegisteredEnquiriesForConsultant(consultant);

    return buildConversations(request, consultant, registeredEnquiries);
  }

  /** {@inheritDoc} */
  @Override
  public ConversationListType providedType() {
    return REGISTERED_ENQUIRY;
  }
}

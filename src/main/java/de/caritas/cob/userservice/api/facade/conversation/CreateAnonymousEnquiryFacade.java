package de.caritas.cob.userservice.api.facade.conversation;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryResponseDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.user.anonymous.AnonymousUserCreatorService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps of creating a new anonymous conversation.
 */
@Service
@RequiredArgsConstructor
public class CreateAnonymousEnquiryFacade {

  private final @NonNull AnonymousUserCreatorService anonymousUserCreatorService;

  public CreateAnonymousEnquiryResponseDTO createAnonymousEnquiry(
      CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {

    checkIfConsultingTypeIsAllowed(createAnonymousEnquiryDTO);

    return anonymousUserCreatorService.createAnonymousUser(createAnonymousEnquiryDTO);
  }

  private void checkIfConsultingTypeIsAllowed(CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {
    // TODO auslagern und umbenennen:
    ConsultingType consultingType =
        ConsultingType.values()[createAnonymousEnquiryDTO.getConsultingType()];
    List<ConsultingType> blockedConsultingTypes = List.of(ConsultingType.U25,
        ConsultingType.REGIONAL, ConsultingType.KREUZBUND, ConsultingType.SUPPORTGROUPVECHTA);
    if (blockedConsultingTypes.contains(consultingType)) {
      throw new BadRequestException("Consulting type does not support anonymous conversations.");
    }
  }
}

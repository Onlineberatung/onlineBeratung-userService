package de.caritas.cob.userservice.api.facade.conversation;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_KREUZBUND;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.conversation.facade.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.model.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.conversation.service.AnonymousConversationCreatorService;
import de.caritas.cob.userservice.api.conversation.service.user.anonymous.AnonymousUserCreatorService;
import de.caritas.cob.userservice.api.conversation.service.user.anonymous.AnonymousUsernameRegistry;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class CreateAnonymousEnquiryFacadeTest {

  @InjectMocks private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;
  @Mock private AnonymousUserCreatorService anonymousUserCreatorService;
  @Mock private AnonymousConversationCreatorService anonymousConversationCreatorService;
  @Mock private AnonymousUsernameRegistry usernameRegistry;
  @Mock private UserHelper userHelper;
  @Mock private ConsultingTypeManager consultingTypeManager;

  EasyRandom easyRandom = new EasyRandom();

  @Test
  public void
      createAnonymousEnquiry_Should_ThrowBadRequestException_When_GivenConsultingTypeDoesNotSupportAnonymousConversations() {
    assertThrows(
        BadRequestException.class,
        () -> {
          CreateAnonymousEnquiryDTO anonymousEnquiryDTO =
              easyRandom.nextObject(CreateAnonymousEnquiryDTO.class);
          anonymousEnquiryDTO.setConsultingType(CONSULTING_TYPE_ID_KREUZBUND);
          var consultingTypeResponseDTO =
              easyRandom.nextObject(ExtendedConsultingTypeResponseDTO.class);
          consultingTypeResponseDTO.setIsAnonymousConversationAllowed(false);
          when(consultingTypeManager.getConsultingTypeSettings(
                  anonymousEnquiryDTO.getConsultingType()))
              .thenReturn(consultingTypeResponseDTO);

          createAnonymousEnquiryFacade.createAnonymousEnquiry(anonymousEnquiryDTO);

          verifyNoInteractions(anonymousUserCreatorService);
          verifyNoInteractions(anonymousConversationCreatorService);
          verifyNoInteractions(usernameRegistry);
          verifyNoInteractions(userHelper);
        });
  }

  @Test
  public void createAnonymousEnquiry_Should_ReturnValidCreateAnonymousEnquiryResponseDTO() {
    CreateAnonymousEnquiryDTO anonymousEnquiryDTO =
        easyRandom.nextObject(CreateAnonymousEnquiryDTO.class);
    anonymousEnquiryDTO.setConsultingType(CONSULTING_TYPE_ID_SUCHT);
    AnonymousUserCredentials credentials = easyRandom.nextObject(AnonymousUserCredentials.class);
    when(anonymousUserCreatorService.createAnonymousUser(any())).thenReturn(credentials);
    Session session = easyRandom.nextObject(Session.class);
    when(anonymousConversationCreatorService.createAnonymousConversation(any(), any()))
        .thenReturn(session);
    var consultingTypeResponseDTO = easyRandom.nextObject(ExtendedConsultingTypeResponseDTO.class);
    consultingTypeResponseDTO.setIsAnonymousConversationAllowed(true);
    when(consultingTypeManager.getConsultingTypeSettings(anonymousEnquiryDTO.getConsultingType()))
        .thenReturn(consultingTypeResponseDTO);

    createAnonymousEnquiryFacade.createAnonymousEnquiry(anonymousEnquiryDTO);

    verify(anonymousUserCreatorService, times(1)).createAnonymousUser(any());
    verify(anonymousConversationCreatorService, times(1)).createAnonymousConversation(any(), any());
    verify(consultingTypeManager, times(1))
        .getConsultingTypeSettings(anonymousEnquiryDTO.getConsultingType());
  }
}

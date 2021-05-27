package de.caritas.cob.userservice.api.facade.conversation;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.model.user.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.conversation.anonymous.AnonymousConversationCreatorService;
import de.caritas.cob.userservice.api.service.user.anonymous.AnonymousUserCreatorService;
import de.caritas.cob.userservice.api.service.user.anonymous.AnonymousUsernameRegistry;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CreateAnonymousEnquiryFacadeTest {

  @InjectMocks
  private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;
  @Mock
  private AnonymousUserCreatorService anonymousUserCreatorService;
  @Mock
  private AnonymousConversationCreatorService anonymousConversationCreatorService;
  @Mock
  private AnonymousUsernameRegistry usernameRegistry;
  @Mock
  private UserHelper userHelper;

  EasyRandom easyRandom = new EasyRandom();

  @Test(expected = BadRequestException.class)
  public void createAnonymousEnquiry_Should_ThrowBadRequestException_When_GivenConsultingTypeDoesNotSupportAnonymousConversations() {
    CreateAnonymousEnquiryDTO anonymousEnquiryDTO =
        easyRandom.nextObject(CreateAnonymousEnquiryDTO.class);
    anonymousEnquiryDTO.setConsultingType(CONSULTING_TYPE_ID_KREUZBUND);

    createAnonymousEnquiryFacade.createAnonymousEnquiry(anonymousEnquiryDTO);

    verifyNoMoreInteractions(anonymousUserCreatorService);
    verifyNoMoreInteractions(anonymousConversationCreatorService);
    verifyNoMoreInteractions(usernameRegistry);
    verifyNoMoreInteractions(userHelper);
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

    createAnonymousEnquiryFacade.createAnonymousEnquiry(anonymousEnquiryDTO);

    verify(anonymousUserCreatorService, times(1)).createAnonymousUser(any());
    verify(anonymousConversationCreatorService, times(1)).createAnonymousConversation(any(), any());
  }
}

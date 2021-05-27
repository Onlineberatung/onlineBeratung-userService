package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static de.caritas.cob.userservice.api.repository.session.ConsultingType.OFFENDER;
import static de.caritas.cob.userservice.api.repository.session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_OFFENDER;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_U25;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.jeasy.random.EasyRandom;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class AnonymousEnquiryConversationListProviderIT {

  @Autowired
  private AnonymousEnquiryConversationListProvider anonymousEnquiryConversationListProvider;

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private UserRepository userRepository;

  @MockBean
  private AgencyService agencyService;

  @MockBean
  private ValidatedUserAccountProvider userAccountProvider;

  @MockBean
  private UsernameTranscoder usernameTranscoder;

  @Before
  public void setup() {
    Consultant consultant = mock(Consultant.class);
    ConsultantAgency consultantAgency = mock(ConsultantAgency.class);
    when(consultant.getConsultantAgencies()).thenReturn(asSet(consultantAgency));
    when(this.userAccountProvider.retrieveValidatedConsultant()).thenReturn(consultant);
    AgencyDTO agencyDTO = new AgencyDTO().consultingType(CONSULTING_TYPE_ID_OFFENDER);
    when(this.agencyService.getAgencies(any())).thenReturn(singletonList(agencyDTO));
  }

  @After
  public void cleanDatabase() {
    this.sessionRepository.deleteAll();
  }

  @Test
  public void buildConversations_Should_returnExpectedResponseDTO_When_consultantHasAnonymousEnquiries() {
    saveAnonymousSessions(10);
    PageableListRequest request = PageableListRequest.builder()
        .count(5)
        .offset(0)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.anonymousEnquiryConversationListProvider
        .buildConversations(request);

    assertThat(responseDTO.getCount(), is(5));
    assertThat(responseDTO.getOffset(), is(0));
    assertThat(responseDTO.getTotal(), is(10));
    assertThat(responseDTO.getSessions(), hasSize(5));
  }

  @Test
  public void buildConversations_Should_returnExpectedElements_When_paginationParamsAreAtTheEnd() {
    saveAnonymousSessions(10);
    PageableListRequest request = PageableListRequest.builder()
        .count(3)
        .offset(9)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.anonymousEnquiryConversationListProvider
        .buildConversations(request);

    assertThat(responseDTO.getCount(), is(1));
    assertThat(responseDTO.getOffset(), is(9));
    assertThat(responseDTO.getTotal(), is(10));
    assertThat(responseDTO.getSessions(), hasSize(1));
  }

  @Test
  public void buildConversations_Should_returnElementsInExpectedOrder() {
    saveAnonymousSessions(100);
    PageableListRequest request = PageableListRequest.builder()
        .count(100)
        .offset(0)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.anonymousEnquiryConversationListProvider
        .buildConversations(request);

    PeekingIterator<ConsultantSessionResponseDTO> peeker =
        new PeekingIterator<>(responseDTO.getSessions().iterator());
    while (peeker.hasNext()) {
      ConsultantSessionResponseDTO current = peeker.next();
      ConsultantSessionResponseDTO next = peeker.peek();
      if (nonNull(next)) {
        assertThat(next.getLatestMessage(), greaterThanOrEqualTo(current.getLatestMessage()));
      }
    }
  }

  @Test
  public void providedType_Should_return_anonymousEnquiry() {
    ConversationListType conversationListType = this.anonymousEnquiryConversationListProvider
        .providedType();

    assertThat(conversationListType, is(ANONYMOUS_ENQUIRY));
  }

  private void saveAnonymousSessions(int amount) {
    List<Session> sessions = new EasyRandom().objects(Session.class, amount)
        .collect(Collectors.toList());
    User user = this.userRepository.findAll().iterator().next();
    sessions.forEach(session -> {
      session.setRegistrationType(ANONYMOUS);
      session.setConsultant(null);
      session.setUser(user);
      session.setId(null);
      session.setSessionData(null);
      session.setPostcode("12345");
      session.setConsultingTypeId(CONSULTING_TYPE_ID_OFFENDER);
      session.setStatus(SessionStatus.NEW);
    });
    this.sessionRepository.saveAll(sessions);
  }

}

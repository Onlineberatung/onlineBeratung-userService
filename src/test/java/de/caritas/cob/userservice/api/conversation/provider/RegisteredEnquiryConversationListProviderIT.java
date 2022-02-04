package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;
import static de.caritas.cob.userservice.api.repository.session.RegistrationType.REGISTERED;
import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import java.util.List;
import java.util.Random;
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
public class RegisteredEnquiryConversationListProviderIT {

  @Autowired
  private RegisteredEnquiryConversationListProvider registeredEnquiryConversationListProvider;

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private UserRepository userRepository;

  @MockBean
  private ValidatedUserAccountProvider userAccountProvider;

  @Before
  public void setup() {
    Consultant consultant = mock(Consultant.class);
    ConsultantAgency consultantAgency = mock(ConsultantAgency.class);
    when(consultantAgency.getAgencyId()).thenReturn(1L);
    when(consultant.getConsultantAgencies()).thenReturn(asSet(consultantAgency));
    when(this.userAccountProvider.retrieveValidatedConsultant()).thenReturn(consultant);
  }

  @After
  public void cleanDatabase() {
    this.sessionRepository.deleteAll();
  }

  @Test
  public void buildConversations_Should_returnExpectedResponseDTO_When_consultantHasRegisteredEnquiries() {
    saveRegisteredSessions(10);
    PageableListRequest request = PageableListRequest.builder()
        .count(5)
        .offset(0)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.registeredEnquiryConversationListProvider
        .buildConversations(request);

    assertThat(responseDTO.getCount(), is(5));
    assertThat(responseDTO.getOffset(), is(0));
    assertThat(responseDTO.getTotal(), is(10));
    assertThat(responseDTO.getSessions(), hasSize(5));
  }

  @Test
  public void buildConversations_Should_returnExpectedElements_When_paginationParamsAreAtTheEnd() {
    saveRegisteredSessions(10);
    PageableListRequest request = PageableListRequest.builder()
        .count(3)
        .offset(9)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.registeredEnquiryConversationListProvider
        .buildConversations(request);

    assertThat(responseDTO.getCount(), is(1));
    assertThat(responseDTO.getOffset(), is(9));
    assertThat(responseDTO.getTotal(), is(10));
    assertThat(responseDTO.getSessions(), hasSize(1));
  }

  @Test
  public void buildConversations_Should_returnElementsInExpectedOrder() {
    saveRegisteredSessions(100);
    PageableListRequest request = PageableListRequest.builder()
        .count(100)
        .offset(0)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.registeredEnquiryConversationListProvider
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
  public void providedType_Should_return_registeredEnquiry() {
    ConversationListType conversationListType = this.registeredEnquiryConversationListProvider
        .providedType();

    assertThat(conversationListType, is(REGISTERED_ENQUIRY));
  }

  private void saveRegisteredSessions(int amount) {
    var random = new Random();
    List<Session> sessions = new EasyRandom().objects(Session.class, amount + 4)
        .collect(Collectors.toList());
    User user = this.userRepository.findAll().iterator().next();
    sessions.forEach(session -> {
      session.setRegistrationType(REGISTERED);
      session.setConsultant(null);
      session.setUser(user);
      session.setId(null);
      session.setSessionData(null);
      session.setPostcode("12345");
      session.setAgencyId(1L);
      session.setStatus(SessionStatus.NEW);
      session.setConsultingTypeId(random.nextInt(127));
    });
    sessions.get(0).setStatus(SessionStatus.INITIAL);
    sessions.get(1).setStatus(SessionStatus.IN_PROGRESS);
    sessions.get(2).setStatus(SessionStatus.DONE);
    sessions.get(3).setStatus(SessionStatus.IN_ARCHIVE);
    this.sessionRepository.saveAll(sessions);
  }

}

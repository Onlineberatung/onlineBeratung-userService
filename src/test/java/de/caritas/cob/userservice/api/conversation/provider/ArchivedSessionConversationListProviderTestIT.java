package de.caritas.cob.userservice.api.conversation.provider;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ARCHIVED_SESSION;
import static de.caritas.cob.userservice.api.repository.session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_OFFENDER;
import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.jeasy.random.EasyRandom;
import org.junit.After;
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
public class ArchivedSessionConversationListProviderTestIT {

  @Autowired
  private ArchivedSessionConversationListProvider archivedSessionConversationListProvider;

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private ConsultantAgencyRepository consultantAgencyRepository;

  @Autowired
  private UserRepository userRepository;

  @MockBean
  private ValidatedUserAccountProvider userAccountProvider;

  @After
  public void cleanDatabase() {
    this.sessionRepository.deleteAll();
    this.consultantAgencyRepository.deleteAll();
    this.consultantRepository.deleteAll();
  }

  @Test
  public void buildConversations_Should_returnExpectedResponseDTO_When_consultantHasArchivedSessions() {
    saveTestData(10);
    PageableListRequest request = PageableListRequest.builder()
        .count(5)
        .offset(0)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.archivedSessionConversationListProvider
        .buildConversations(request);

    assertThat(responseDTO.getCount(), is(5));
    assertThat(responseDTO.getOffset(), is(0));
    assertThat(responseDTO.getTotal(), is(10));
    assertThat(responseDTO.getSessions(), hasSize(5));
  }

  @Test
  public void buildConversations_Should_returnExpectedElements_When_paginationParamsAreAtTheEnd() {
    saveTestData(10);
    PageableListRequest request = PageableListRequest.builder()
        .count(3)
        .offset(9)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.archivedSessionConversationListProvider
        .buildConversations(request);

    assertThat(responseDTO.getCount(), is(1));
    assertThat(responseDTO.getOffset(), is(9));
    assertThat(responseDTO.getTotal(), is(10));
    assertThat(responseDTO.getSessions(), hasSize(1));
  }

  @Test
  public void buildConversations_Should_returnElementsInExpectedOrder() {
    saveTestData(100);
    PageableListRequest request = PageableListRequest.builder()
        .count(100)
        .offset(0)
        .build();

    ConsultantSessionListResponseDTO responseDTO = this.archivedSessionConversationListProvider
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
  public void providedType_Should_return_archivedSession() {
    ConversationListType conversationListType = this.archivedSessionConversationListProvider
        .providedType();

    assertThat(conversationListType, is(ARCHIVED_SESSION));
  }

  private void saveTestData(int amount) {
    Consultant consultant = buildConsultant();
    consultantRepository.save(consultant);
    when(this.userAccountProvider.retrieveValidatedConsultant()).thenReturn(consultant);
    Consultant consultant2 = buildConsultant();
    consultantRepository.save(consultant2);

    ConsultantAgency consultantAgency = buildConsultantAgency(consultant);
    consultantAgencyRepository.save(consultantAgency);
    ConsultantAgency consultantAgency2 = buildConsultantAgency(consultant2);
    consultantAgencyRepository.save(consultantAgency2);

    List<Session> sessions = new EasyRandom().objects(Session.class, amount + 5)
        .collect(Collectors.toList());
    User user = this.userRepository.findAll().iterator().next();
    sessions.forEach(session -> {
      session.setRegistrationType(REGISTERED);
      session.setConsultant(consultant);
      session.setUser(user);
      session.setId(null);
      session.setTeamSession(false);
      session.setSessionData(null);
      session.setPostcode("12345");
      session.setConsultingTypeId(CONSULTING_TYPE_ID_OFFENDER);
      session.setStatus(SessionStatus.IN_ARCHIVE);
    });
    sessions.get(0).setStatus(SessionStatus.INITIAL);
    sessions.get(1).setStatus(SessionStatus.IN_PROGRESS);
    sessions.get(2).setStatus(SessionStatus.DONE);
    sessions.get(3).setStatus(SessionStatus.NEW);
    sessions.get(4).setStatus(SessionStatus.IN_ARCHIVE);
    sessions.get(4).setConsultant(consultant2);
    sessions.get(4).setTeamSession(true);
    this.sessionRepository.saveAll(sessions);
  }

  private void setupConsultants() {

  }

  private Consultant buildConsultant() {
    Consultant consultant = new Consultant();
    consultant.setId(UUID.randomUUID().toString());
    consultant.setUsername("consultant");
    consultant.setFirstName("firstname");
    consultant.setLastName("lastname");
    consultant.setEmail("firstname@lastname.de");
    consultant.setTeamConsultant(false);
    consultant.setLanguageFormal(false);
    consultant.setAbsent(false);
    return consultant;
  }

  private ConsultantAgency buildConsultantAgency(Consultant consultant) {
    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setConsultant(consultant);
    consultantAgency.setAgencyId(1L);
    consultant.setConsultantAgencies(Collections.singleton(consultantAgency));
    return consultantAgency;
  }

}

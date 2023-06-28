package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.TopicServiceApiControllerFactory;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import java.util.Collections;
import java.util.Set;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@TestPropertySource(properties = "feature.topics.enabled=true")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class SessionServiceIT {

  @Autowired private SessionService sessionService;

  @Autowired private SessionRepository sessionRepository;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private UserRepository userRepository;

  @MockBean private TopicControllerApi topicControllerApi;

  @MockBean private TopicServiceApiControllerFactory topicServiceApiControllerFactory;

  @MockBean private AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @MockBean
  @Qualifier("primary")
  private AgencyControllerApi agencyControllerApi;

  @BeforeEach
  private void setUp() {
    when(topicServiceApiControllerFactory.createControllerApi()).thenReturn(topicControllerApi);
    when(agencyServiceApiControllerFactory.createControllerApi()).thenReturn(agencyControllerApi);
    when(agencyControllerApi.getApiClient()).thenReturn(new ApiClient());
  }

  @Test
  void fetchSessionForConsultant_Should_ThrowNotFoundException_When_SessionIsNotFound() {
    assertThrows(
        NotFoundException.class, () -> sessionService.fetchSessionForConsultant(-1L, CONSULTANT));
  }

  @Test
  void fetchSessionForConsultant_Should_Throw_ForbiddenException_When_ConsultantHasNoPermission() {
    Consultant consultant =
        consultantRepository
            .findByIdAndDeleteDateIsNull("fb77d849-470f-4cec-89ca-6aa673bacb88")
            .get();

    assertThrows(
        ForbiddenException.class, () -> sessionService.fetchSessionForConsultant(1L, consultant));
  }

  @Test
  void getSessionsByUserAndGroupOrFeedbackGroupIdsShouldBeForbiddenIfUserHasNotRequiredRole() {
    assertThrows(
        ForbiddenException.class,
        () ->
            sessionService.getSessionsByUserAndGroupOrFeedbackGroupIds(
                "9c4057d0-05ad-4e86-a47c-dc5bdeec03b9",
                Set.of("9faSTWZ5gurHLXy4R"),
                Collections.emptySet()));
  }

  @Test
  void getSessionsByUserAndGroupOrFeedbackGroupIdsShouldFetchAgencyForSession() {
    var sessions =
        sessionService.getSessionsByUserAndGroupOrFeedbackGroupIds(
            "9c4057d0-05ad-4e86-a47c-dc5bdeec03b9", Set.of("9faSTWZ5gurHLXy4R"), Set.of("user"));

    assertEquals(1, sessions.size());
    var userSession = sessions.get(0);
    assertEquals("9faSTWZ5gurHLXy4R", userSession.getSession().getFeedbackGroupId());
  }

  @Test
  void
      fetchSessionForConsultant_Should_Return_ValidConsultantSessionDTO_When_ConsultantIsAssigned() {
    givenAValidTopicServiceResponse();
    Consultant consultant =
        consultantRepository
            .findByIdAndDeleteDateIsNull("473f7c4b-f011-4fc2-847c-ceb636a5b399")
            .get();
    Session session = sessionRepository.findById(1216L).get();

    ConsultantSessionDTO result = sessionService.fetchSessionForConsultant(1216L, consultant);

    assertNotNull(result);
    assertEquals(session.getId(), result.getId());
    assertEquals(session.isTeamSession(), result.getIsTeamSession());
    assertEquals(session.getAgencyId(), result.getAgencyId());
    assertEquals(session.getConsultant().getId(), result.getConsultantId());
    assertEquals(session.getConsultant().getRocketChatId(), result.getConsultantRcId());
    assertEquals(session.getUser().getUserId(), result.getAskerId());
    assertEquals(session.getUser().getRcUserId(), result.getAskerRcId());
    assertEquals(session.getUser().getUsername(), result.getAskerUserName());
    assertEquals(session.getPostcode(), result.getPostcode());
    assertEquals(session.getStatus().getValue(), result.getStatus().intValue());
    assertEquals(session.getGroupId(), result.getGroupId());
    assertEquals(session.getFeedbackGroupId(), result.getFeedbackGroupId());
    assertEquals(session.getConsultingTypeId(), result.getConsultingType().intValue());
    assertEquals(session.getUserAge(), result.getAge());
    assertEquals(session.getUserGender(), result.getGender());
    assertEquals(session.getCounsellingRelation(), result.getCounsellingRelation());
    assertEquals(session.getReferer(), result.getReferer());
    assertNotNull(result.getMainTopic());
    assertEquals(1, result.getMainTopic().getId());
    assertEquals("topic name", result.getMainTopic().getName());
    assertEquals("topic desc", result.getMainTopic().getDescription());
    assertNotNull(result.getTopics());
    assertEquals(2, result.getTopics().size());
    assertNotNull(result.getTopics().get(0));
    assertEquals(1, result.getTopics().get(0).getId());
    assertEquals("topic name", result.getTopics().get(0).getName());
    assertEquals("topic desc", result.getTopics().get(0).getDescription());
    assertNotNull(result.getTopics().get(1));
    assertEquals(2, result.getTopics().get(1).getId());
    assertEquals("topic name 2", result.getTopics().get(1).getName());
    assertEquals("topic desc 2", result.getTopics().get(1).getDescription());
  }

  @Test
  @Transactional
  void
      fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_When_ConsultantIsToTeamSessionAgencyAssigned() {
    givenAValidTopicServiceResponse();
    Consultant consultant =
        consultantRepository
            .findByIdAndDeleteDateIsNull("e2f20d3a-1ca7-4cb5-9fac-8e26033416b3")
            .get();
    assertNotNull(sessionService.fetchSessionForConsultant(2L, consultant));
  }

  @Test
  void fetchGroupIdWithConsultantAndUser_Should_Return_GroupId() {
    String groupId =
        sessionService.findGroupIdByConsultantAndUser(
            "473f7c4b-f011-4fc2-847c-ceb636a5b399", "1da238c6-cd46-4162-80f1-bff74eafe77f");
    assertEquals("4WKq3kj9C7WESSQuK", groupId);
  }

  @Test
  void fetchGroupIdWithConsultantAndUser_Should_Return_BadRequestException() {
    Session session = new Session();
    session.setConsultant(
        consultantRepository.findById("473f7c4b-f011-4fc2-847c-ceb636a5b399").get());
    session.setUser(userRepository.findById("1da238c6-cd46-4162-80f1-bff74eafe77f").get());
    session.setConsultingTypeId(9);
    session.setLanguageCode(LanguageCode.de);
    session.setPostcode("12345");
    session.setRegistrationType(RegistrationType.ANONYMOUS);
    session.setIsConsultantDirectlySet(false);
    sessionService.saveSession(session);
    assertThrows(
        javax.ws.rs.BadRequestException.class,
        () -> {
          sessionService.findGroupIdByConsultantAndUser(
              "473f7c4b-f011-4fc2-847c-ceb636a5b399", "1da238c6-cd46-4162-80f1-bff74eafe77f");
        });

    sessionRepository.delete(session);
  }

  private void givenAValidTopicServiceResponse() {
    var firstTopic =
        new de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO()
            .id(1L)
            .name("topic name")
            .description("topic desc")
            .status("INACTIVE")
            .internalIdentifier("internal identifier 1");
    var secondTopic =
        new de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO()
            .id(2L)
            .name("topic name 2")
            .description("topic desc 2")
            .status("ACTIVE")
            .internalIdentifier("internal identifier 2");

    when(topicControllerApi.getApiClient())
        .thenReturn(new de.caritas.cob.userservice.topicservice.generated.ApiClient());
    when(topicControllerApi.getAllTopics()).thenReturn(Lists.newArrayList(firstTopic, secondTopic));
    when(topicControllerApi.getAllActiveTopics())
        .thenReturn(Lists.newArrayList(firstTopic, secondTopic));
  }
}

package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.caritas.cob.userservice.api.adapters.web.dto.RegistrationStatisticsListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.RegistrationStatisticsResponseDTO;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.SessionTopic;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;
import java.time.LocalDateTime;
import java.util.Map;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrationStatisticsResponseDTOConverterTest {

  RegistrationStatisticsResponseDTOConverter registrationStatisticsResponseDTOConverter;

  Map<Long, TopicDTO> topicDTOMap = Maps.newHashMap();

  EasyRandom easyRandom;

  @BeforeEach
  public void setUp() {
    easyRandom = new EasyRandom();
    topicDTOMap.put(1L, new TopicDTO().internalIdentifier("angeho01"));
    topicDTOMap.put(2L, new TopicDTO().internalIdentifier("angeho02"));
    registrationStatisticsResponseDTOConverter = new RegistrationStatisticsResponseDTOConverter(topicDTOMap);
  }

  @Test
  void convert_Should_ConvertEmptySessionListToEmptyResponse() {
    // given, when
    RegistrationStatisticsListResponseDTO convert = registrationStatisticsResponseDTOConverter.convert(
        Lists.newArrayList());

    // then
    assertThat(convert.getRegistrationStatistics()).isEmpty();
  }

  @Test
  void convert_Should_ConvertSessionAttributesToValidResponse() {
    // given
    Session session = getRandomSessionWithTopics();

    var expectedResponse = new RegistrationStatisticsResponseDTO()
        .userId(session.getUser().getUserId())
        .age(session.getUserAge())
        .gender(session.getUserGender())
        .counsellingRelation(session.getCounsellingRelation())
        .topicsInternalAttributes(Lists.newArrayList("angeho01", "angeho02", ""))
        .mainTopicInternalAttribute("angeho01")
        .registrationDate(toIsoTime(session.getCreateDate()))
        .postalCode(session.getPostcode());

    // when
    var converted = registrationStatisticsResponseDTOConverter.convert(
        Lists.newArrayList(session));

    // then
    assertThat(converted.getRegistrationStatistics()).hasSize(1);
    assertThat(converted.getRegistrationStatistics().get(0)).isEqualTo(expectedResponse);
  }

  @Test
  void convert_Should_ConvertSessionAttributesToValidResponse_When_SessionContainsNullValues() {
    // given
    User user = new User();
    user.setUserId("userId");
    Session session = Session.builder()
        .user(user)
        .registrationType(RegistrationType.REGISTERED)
        .postcode("00000")
        .sessionTopics(Lists.newArrayList())
        .status(SessionStatus.INITIAL)
        .build();
    session.setCreateDate(LocalDateTime.now());


    var expectedResponse = new RegistrationStatisticsResponseDTO()
        .userId(session.getUser().getUserId())
        .age(null)
        .gender(null)
        .counsellingRelation(null)
        .topicsInternalAttributes(Lists.newArrayList())
        .mainTopicInternalAttribute("")
        .registrationDate(toIsoTime(session.getCreateDate()))
        .postalCode(session.getPostcode());

    // when
    var converted = registrationStatisticsResponseDTOConverter.convert(
        Lists.newArrayList(session));

    // then
    assertThat(converted.getRegistrationStatistics()).hasSize(1);
    assertThat(converted.getRegistrationStatistics().get(0)).isEqualTo(expectedResponse);
  }


  @Test
  void convert_Should_ConvertMoreThanOneSessionObjectToAValidResponse() {
    // given
    var sessions = Lists.newArrayList(getRandomSessionWithTopics(),
        getRandomSessionWithTopics());

    // when
    var converted = registrationStatisticsResponseDTOConverter.convert(
        sessions);

    // then
    assertThat(converted.getRegistrationStatistics()).hasSize(2);
    assertThat(converted.getRegistrationStatistics()).extracting("userId").isNotEmpty();
    assertThat(converted.getRegistrationStatistics()).extracting("age").isNotEmpty();
    assertThat(converted.getRegistrationStatistics()).extracting("gender").isNotEmpty();
    assertThat(converted.getRegistrationStatistics()).extracting("counsellingRelation").isNotEmpty();
    assertThat(converted.getRegistrationStatistics()).extracting("topicsInternalAttributes").isNotEmpty();
    assertThat(converted.getRegistrationStatistics()).extracting("mainTopicInternalAttribute").isNotEmpty();
    assertThat(converted.getRegistrationStatistics()).extracting("registrationDate").isNotEmpty();
    assertThat(converted.getRegistrationStatistics()).extracting("postalCode").isNotEmpty();
  }

  private Session getRandomSessionWithTopics() {
    Session session = easyRandom.nextObject(Session.class);
    session.setMainTopicId(1);
    session.setSessionTopics(Lists.newArrayList(getSessionTopic(session, 1), getSessionTopic(session, 2),  getSessionTopic(session, 3)));
    return session;
  }

  private static SessionTopic getSessionTopic(Session session, Integer topicId) {
    return SessionTopic.builder().topicId(topicId).session(session).build();
  }
}
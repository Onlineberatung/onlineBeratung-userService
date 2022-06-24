package de.caritas.cob.userservice.api.service.session;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionTopicDTO;
import de.caritas.cob.userservice.api.service.consultingtype.TopicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;

@ExtendWith(MockitoExtension.class)
class SessionTopicEnrichmentServiceTest {

  @InjectMocks
  SessionTopicEnrichmentService sessionTopicEnrichmentService;

  @Mock
  TopicService topicService;

  @Test
  void enrichSessionWithTopicData_Should_EnrichSessionWithTopicDataFromTopicService() {
    // given
    when(topicService.getAllTopics()).thenReturn(
        newArrayList(new TopicDTO().id(1L).name("first topic").description("first desc"),
            new TopicDTO().id(2L).name("second topic").description("second desc")));
    var session = new SessionDTO().topic(new SessionTopicDTO().id(1));

    // when
    sessionTopicEnrichmentService.enrichSessionWithTopicData(session);

    // then
    assertThat(session.getTopic().getName()).isEqualTo("first topic");
    assertThat(session.getTopic().getDescription()).isEqualTo("first desc");
  }

  @Test
  void enrichSessionWithTopicData_Should_NotEnrichSessionWithTopicDataFromTopicServiceIfNoMatchingTopicFound() {
    // given
    when(topicService.getAllTopics()).thenReturn(
        newArrayList(new TopicDTO().id(1L).name("first topic").description("first desc"),
            new TopicDTO().id(2L).name("second topic").description("second desc")));
    var session = new SessionDTO().topic(new SessionTopicDTO().id(3));

    // when
    sessionTopicEnrichmentService.enrichSessionWithTopicData(session);

    // then
    assertThat(session.getTopic().getId()).isNotNull();
    assertThat(session.getTopic().getName()).isNull();
    assertThat(session.getTopic().getDescription()).isNull();
  }

  @Test
  void enrichSessionWithTopicData_Should_NotEnrichSessionWithTopicDataFromTopicServiceIfTopicIsNull() {
    // given
    var session = new SessionDTO().topic(null);

    // when
    sessionTopicEnrichmentService.enrichSessionWithTopicData(session);

    // then
    Mockito.verify(topicService, Mockito.never()).getAllTopics();
    assertThat(session.getTopic()).isNull();
  }


  @Test
  void enrichSessionWithTopicData_Should_NotEnrichSessionWithTopicDataFromTopicServiceIfTopicIdNotSet() {
    // given
    var session = new SessionDTO().topic(new SessionTopicDTO());

    // when
    sessionTopicEnrichmentService.enrichSessionWithTopicData(session);

    // then
    Mockito.verify(topicService, Mockito.never()).getAllTopics();
    assertThat(session.getTopic().getId()).isNull();
  }

}
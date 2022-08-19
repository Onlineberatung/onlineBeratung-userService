package de.caritas.cob.userservice.api.facade;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.web.dto.RegistrationStatisticsListResponseDTO;
import de.caritas.cob.userservice.api.service.consultingtype.TopicService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsersStatisticsFacadeTest {

  @Mock SessionService sessionService;

  @Mock TopicService topicService;

  @InjectMocks UsersStatisticsFacade usersStatisticsFacade;

  @Test
  void
      getRegistrationStatistics_Should_ConvertSessionsToResponseDTOAndCallTopicServiceForAllTopicsMap() {
    // given
    Mockito.when(sessionService.findAllSessions()).thenReturn(Lists.newArrayList());

    // when
    RegistrationStatisticsListResponseDTO registrationStatistics =
        usersStatisticsFacade.getRegistrationStatistics();

    // then
    assertThat(registrationStatistics).isNotNull();
    verify(topicService).getAllTopicsMap();
  }
}

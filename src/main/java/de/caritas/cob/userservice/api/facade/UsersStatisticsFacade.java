package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.adapters.web.dto.RegistrationStatisticsListResponseDTO;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.consultingtype.TopicService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade to encapsulate the gathering of user statistics */
@Service
@RequiredArgsConstructor
public class UsersStatisticsFacade {

  private @NonNull SessionService sessionService;

  private final @NonNull TopicService topicService;

  public RegistrationStatisticsListResponseDTO getRegistrationStatistics() {

    List<Session> sessions = sessionService.findAllSessions();
    RegistrationStatisticsResponseDTOConverter converter =
        new RegistrationStatisticsResponseDTOConverter(topicService.getAllTopicsMap());
    return converter.convert(sessions);
  }
}

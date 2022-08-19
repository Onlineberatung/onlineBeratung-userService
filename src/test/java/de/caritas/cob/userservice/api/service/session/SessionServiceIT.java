package de.caritas.cob.userservice.api.service.session;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class SessionServiceIT {

  @Autowired private SessionService sessionService;

  @Autowired private SessionRepository sessionRepository;

  @Autowired private ConsultantRepository consultantRepository;

  @Test
  public void fetchSessionForConsultant_Should_ThrowNotFoundException_When_SessionIsNotFound() {
    assertThrows(
        NotFoundException.class, () -> sessionService.fetchSessionForConsultant(-1L, CONSULTANT));
  }

  @Test
  public void
      fetchSessionForConsultant_Should_Throw_ForbiddenException_When_ConsultantHasNoPermission() {
    Consultant consultant =
        consultantRepository
            .findByIdAndDeleteDateIsNull("fb77d849-470f-4cec-89ca-6aa673bacb88")
            .get();

    assertThrows(
        ForbiddenException.class, () -> sessionService.fetchSessionForConsultant(1L, consultant));
  }

  @Test
  public void
      getSessionsByUserAndGroupOrFeedbackGroupIdsShouldBeForbiddenIfUserHasNotRequiredRole() {
    assertThrows(
        ForbiddenException.class,
        () ->
            sessionService.getSessionsByUserAndGroupOrFeedbackGroupIds(
                "9c4057d0-05ad-4e86-a47c-dc5bdeec03b9",
                Set.of("9faSTWZ5gurHLXy4R"),
                Collections.emptySet()));
  }

  @Test
  public void getSessionsByUserAndGroupOrFeedbackGroupIdsShouldFetchAgencyForSession() {
    var sessions =
        sessionService.getSessionsByUserAndGroupOrFeedbackGroupIds(
            "9c4057d0-05ad-4e86-a47c-dc5bdeec03b9", Set.of("9faSTWZ5gurHLXy4R"), Set.of("user"));

    assertEquals(1, sessions.size());
    var userSession = sessions.get(0);
    assertEquals("9faSTWZ5gurHLXy4R", userSession.getSession().getFeedbackGroupId());
  }

  @Test
  public void
      fetchSessionForConsultant_Should_Return_ValidConsultantSessionDTO_When_ConsultantIsAssigned() {
    Consultant consultant =
        consultantRepository
            .findByIdAndDeleteDateIsNull("473f7c4b-f011-4fc2-847c-ceb636a5b399")
            .get();
    Session session = sessionRepository.findById(1L).get();
    ConsultantSessionDTO result = sessionService.fetchSessionForConsultant(1L, consultant);

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
    assertEquals(session.isMonitoring(), result.getIsMonitoring());
    assertEquals(session.getStatus().getValue(), result.getStatus().intValue());
    assertEquals(session.getGroupId(), result.getGroupId());
    assertEquals(session.getFeedbackGroupId(), result.getFeedbackGroupId());
    assertEquals(session.getConsultingTypeId(), result.getConsultingType().intValue());
  }

  @Test
  @Transactional
  public void
      fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_When_ConsultantIsToTeamSessionAgencyAssigned() {
    Consultant consultant =
        consultantRepository
            .findByIdAndDeleteDateIsNull("e2f20d3a-1ca7-4cb5-9fac-8e26033416b3")
            .get();
    assertNotNull(sessionService.fetchSessionForConsultant(2L, consultant));
  }
}

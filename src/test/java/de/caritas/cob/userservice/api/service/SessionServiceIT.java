package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class SessionServiceIT {

  @Autowired
  private SessionService sessionService;
  @Autowired
  private SessionRepository sessionRepository;
  @Autowired
  private ConsultantRepository consultantRepository;

  @Test(expected = NotFoundException.class)
  public void fetchSessionForConsultant_Should_ThrowNotFoundException_When_SessionIsNotFound() {

    sessionService.fetchSessionForConsultant(-1L, CONSULTANT);
  }

  @Test(expected = ForbiddenException.class)
  public void fetchSessionForConsultant_Should_Throw_ForbiddenException_When_ConsultantHasNoPermission() {

    Consultant consultant = consultantRepository
        .findByIdAndDeleteDateIsNull("fb77d849-470f-4cec-89ca-6aa673bacb88")
        .get();
    sessionService.fetchSessionForConsultant(1L, consultant);
  }

  @Test
  public void fetchSessionForConsultant_Should_Return_ValidConsultantSessionDTO_When_ConsultantIsAssigned() {

    Consultant consultant = consultantRepository
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
    assertEquals(session.getConsultingId(), result.getConsultingId().intValue());
  }

  @Test
  @Transactional
  public void fetchSessionForConsultant_Should_Return_ConsultantSessionDTO_When_ConsultantIsToTeamSessionAgencyAssigned() {

    Consultant consultant = consultantRepository
        .findByIdAndDeleteDateIsNull("e2f20d3a-1ca7-4cb5-9fac-8e26033416b3")
        .get();
    assertNotNull(sessionService.fetchSessionForConsultant(2L, consultant));
  }

}

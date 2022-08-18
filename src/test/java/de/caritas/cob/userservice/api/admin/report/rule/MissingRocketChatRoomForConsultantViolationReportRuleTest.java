package de.caritas.cob.userservice.api.admin.report.rule;

import static de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO.ViolationTypeEnum.CONSULTANT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserRoomDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MissingRocketChatRoomForConsultantViolationReportRuleTest {

  @InjectMocks private MissingRocketChatRoomForConsultantViolationReportRule reportRule;

  @Mock private ConsultantRepository consultantRepository;

  @Mock private SessionRepository sessionRepository;

  @Mock private RocketChatService rocketChatService;

  @Test
  public void generateViolations_Should_returnEmptyList_When_noViolationExists() {
    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(0));
  }

  @Test
  public void generateViolations_Should_returnExpectedViolation_When_oneViolatedSessionExists() {
    Consultant violatedConsultant = new EasyRandom().nextObject(Consultant.class);
    violatedConsultant.setSessions(null);
    Session violatedSession = new EasyRandom().nextObject(Session.class);
    UserInfoResponseDTO userInfoResponseDTO =
        new EasyRandom().nextObject(UserInfoResponseDTO.class);
    userInfoResponseDTO.getUser().setRooms(emptyList());

    when(this.consultantRepository.findAll()).thenReturn(singletonList(violatedConsultant));
    when(this.sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(singletonList(violatedSession));
    when(this.rocketChatService.getUserInfo(any())).thenReturn(userInfoResponseDTO);

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(1));
    ViolationDTO resultViolation = violations.iterator().next();
    assertThat(resultViolation.getIdentifier(), is(violatedSession.getConsultant().getId()));
    assertThat(resultViolation.getViolationType(), is(CONSULTANT));
    assertThat(
        resultViolation.getReason(),
        is(
            "Missing room with id "
                + violatedSession.getGroupId()
                + " in rocket chat and Missing feedback room with id "
                + violatedSession.getFeedbackGroupId()
                + " in rocket chat"));
  }

  @Test
  public void
      generateViolations_Should_returnViolationMessageOfFeedbackRoom_When_oneViolatedFeedbackSessionExists() {
    Consultant violatedConsultant = new EasyRandom().nextObject(Consultant.class);
    Session violatedSession = new EasyRandom().nextObject(Session.class);
    violatedConsultant.setSessions(singleton(violatedSession));
    UserInfoResponseDTO userInfoResponseDTO =
        new EasyRandom().nextObject(UserInfoResponseDTO.class);
    userInfoResponseDTO
        .getUser()
        .setRooms(singletonList(new UserRoomDTO(violatedSession.getGroupId())));

    when(this.consultantRepository.findAll()).thenReturn(singletonList(violatedConsultant));
    when(this.sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(singletonList(violatedSession));
    when(this.rocketChatService.getUserInfo(any())).thenReturn(userInfoResponseDTO);

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(1));
    ViolationDTO resultViolation = violations.iterator().next();
    assertThat(resultViolation.getIdentifier(), is(violatedSession.getConsultant().getId()));
    assertThat(resultViolation.getViolationType(), is(CONSULTANT));
    assertThat(
        resultViolation.getReason(),
        is(
            "Missing feedback room with id "
                + violatedSession.getFeedbackGroupId()
                + " in rocket chat"));
  }

  @Test
  public void
      generateViolations_Should_returnViolationMessageOfRoom_When_oneViolatedStandardSessionExists() {
    Consultant violatedConsultant = new EasyRandom().nextObject(Consultant.class);
    Session violatedSession = new EasyRandom().nextObject(Session.class);
    violatedConsultant.setSessions(singleton(violatedSession));
    UserInfoResponseDTO userInfoResponseDTO =
        new EasyRandom().nextObject(UserInfoResponseDTO.class);
    userInfoResponseDTO
        .getUser()
        .setRooms(singletonList(new UserRoomDTO(violatedSession.getFeedbackGroupId())));

    when(this.consultantRepository.findAll()).thenReturn(singletonList(violatedConsultant));
    when(this.sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(singletonList(violatedSession));
    when(this.rocketChatService.getUserInfo(any())).thenReturn(userInfoResponseDTO);

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(1));
    ViolationDTO resultViolation = violations.iterator().next();
    assertThat(resultViolation.getIdentifier(), is(violatedSession.getConsultant().getId()));
    assertThat(resultViolation.getViolationType(), is(CONSULTANT));
    assertThat(
        resultViolation.getReason(),
        is("Missing room with id " + violatedSession.getGroupId() + " in rocket chat"));
  }

  @Test
  public void generateViolations_Should_returnNoViolation_When_allRoomsExist() {
    Consultant violatedConsultant = new EasyRandom().nextObject(Consultant.class);
    Session violatedSession = new EasyRandom().nextObject(Session.class);
    violatedConsultant.setSessions(singleton(violatedSession));
    UserInfoResponseDTO userInfoResponseDTO =
        new EasyRandom().nextObject(UserInfoResponseDTO.class);
    userInfoResponseDTO
        .getUser()
        .setRooms(
            asList(
                new UserRoomDTO(violatedSession.getGroupId()),
                new UserRoomDTO(violatedSession.getFeedbackGroupId())));

    when(this.consultantRepository.findAll()).thenReturn(singletonList(violatedConsultant));
    when(this.sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(singletonList(violatedSession));
    when(this.rocketChatService.getUserInfo(any())).thenReturn(userInfoResponseDTO);

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(0));
  }

  @Test
  public void generateViolations_Should_returnViolation_When_userDoesNotExistInRocketChat() {
    Consultant violatedConsultant = new EasyRandom().nextObject(Consultant.class);
    Session violatedSession = new EasyRandom().nextObject(Session.class);
    violatedConsultant.setSessions(singleton(violatedSession));
    UserInfoResponseDTO userInfoResponseDTO =
        new EasyRandom().nextObject(UserInfoResponseDTO.class);
    userInfoResponseDTO
        .getUser()
        .setRooms(
            asList(
                new UserRoomDTO(violatedSession.getGroupId()),
                new UserRoomDTO(violatedSession.getFeedbackGroupId())));

    when(this.consultantRepository.findAll()).thenReturn(singletonList(violatedConsultant));
    when(this.sessionRepository.findByConsultantAndStatus(any(), any()))
        .thenReturn(singletonList(violatedSession));
    when(this.rocketChatService.getUserInfo(any()))
        .thenThrow(
            new InternalServerErrorException(
                "message",
                new RuntimeException("caused " + "message"),
                LogService::logRocketChatError));

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(1));
    assertThat(violations.get(0).getReason(), is("caused message"));
  }
}

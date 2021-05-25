package de.caritas.cob.userservice.api.admin.report.rule;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.admin.report.builder.ViolationByConsultantBuilder;
import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import de.caritas.cob.userservice.api.model.ViolationDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserRoomDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Violation rule to find consultants without necessary rocket chat room for directly assigned
 * sessions.
 */
@Component
@RequiredArgsConstructor
public class MissingRocketChatRoomForConsultantViolationReportRule implements ViolationReportRule {

  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull RocketChatService rocketChatService;

  /**
   * Generates all violations for {@link Consultant} without required rocket chat room assignment.
   *
   * @return the generated violations
   */
  @Override
  @Transactional
  public List<ViolationDTO> generateViolations() {
    return StreamSupport.stream(this.consultantRepository.findAll().spliterator(), false)
        .map(consultant -> this.sessionRepository
            .findByConsultantAndStatus(consultant, SessionStatus.IN_PROGRESS))
        .flatMap(Collection::stream)
        .map(this::fromMissingSession)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private ViolationDTO fromMissingSession(Session session) {
    UserInfoResponseDTO userInfoWithRooms;
    try {
      userInfoWithRooms = this.rocketChatService
          .getUserInfo(session.getConsultant().getRocketChatId());
    } catch (Exception e) {
      return ViolationByConsultantBuilder.getInstance(session.getConsultant())
          .withReason(e.getCause().getMessage())
          .build();
    }
    List<UserRoomDTO> rooms = userInfoWithRooms.getUser().getRooms();
    if (rooms == null) {
      rooms = new ArrayList<UserRoomDTO>(0);
    }
    List<String> rocketChatRoomsOfUser = rooms.stream()
        .map(UserRoomDTO::getRoomId)
        .collect(Collectors.toList());

    String violationMessage = buildPossibleViolationMessage(session, rocketChatRoomsOfUser);
    if (isNotBlank(violationMessage)) {
      return ViolationByConsultantBuilder.getInstance(session.getConsultant())
          .withReason(violationMessage)
          .build();
    }
    return null;
  }

  private String buildPossibleViolationMessage(Session session,
      List<String> rocketChatRoomsOfUser) {
    String violationMessage = "";

    if (isGroupMissing(session.getGroupId(), rocketChatRoomsOfUser)) {
      violationMessage += "Missing room with id " + session.getGroupId() + " in rocket chat";
    }
    if (areBothRoomsMissing(session, rocketChatRoomsOfUser)) {
      violationMessage += " and ";
    }
    if (isGroupMissing(session.getFeedbackGroupId(), rocketChatRoomsOfUser)) {
      violationMessage += "Missing feedback room with id " + session.getFeedbackGroupId() + " in "
          + "rocket chat";
    }
    return violationMessage;
  }

  private boolean isGroupMissing(String groupId, List<String> rocketChatRooms) {
    return isNotBlank(groupId) && !rocketChatRooms.contains(groupId);
  }

  private boolean areBothRoomsMissing(Session session, List<String> rocketChatRoomsOfUser) {
    return isGroupMissing(session.getGroupId(), rocketChatRoomsOfUser) && isGroupMissing(
        session.getFeedbackGroupId(), rocketChatRoomsOfUser);
  }

}

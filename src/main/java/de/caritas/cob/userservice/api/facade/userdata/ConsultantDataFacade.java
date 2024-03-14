package de.caritas.cob.userservice.api.facade.userdata;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.adapters.web.dto.AbsenceDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.GroupSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.UserDtoMapper;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.service.ConsultantService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultantDataFacade {

  private final @NonNull ConsultantService consultantService;
  private final @NonNull AccountManaging accountManager;
  private final @NonNull UserDtoMapper userDtoMapper;

  /**
   * Updates a {@link Consultant} with the absence data from a (@Link AbsenceDTO).
   *
   * @param absence {@link AbsenceDTO}
   */
  public Consultant updateConsultantAbsent(Consultant consultant, AbsenceDTO absence) {
    consultant.setAbsent(isTrue(absence.getAbsent()));

    if (isNotBlank(absence.getMessage())) {
      consultant.setAbsenceMessage(Helper.removeHTMLFromText(absence.getMessage()));
    } else {
      consultant.setAbsenceMessage(null);
    }
    return this.consultantService.saveConsultant(consultant);
  }

  public void addConsultantDisplayNameToSessionList(GroupSessionListResponseDTO groupSessionList) {
    groupSessionList
        .getSessions()
        .forEach(
            session -> {
              var consultant = session.getConsultant();
              if (nonNull(consultant) && nonNull(consultant.getUsername())) {
                accountManager
                    .findConsultantByUsername(consultant.getUsername())
                    .ifPresent(
                        consultantMap ->
                            consultant.setDisplayName(userDtoMapper.displayNameOf(consultantMap)));
              }
            });
  }

  public void addConsultantDisplayNameToSessionList(UserSessionListResponseDTO userSessionsDTO) {
    userSessionsDTO
        .getSessions()
        .forEach(
            session -> {
              var consultant = session.getConsultant();
              if (nonNull(consultant) && nonNull(consultant.getUsername())) {
                accountManager
                    .findConsultantByUsername(consultant.getUsername())
                    .ifPresent(
                        consultantMap ->
                            consultant.setDisplayName(userDtoMapper.displayNameOf(consultantMap)));
              }
            });
  }

  public void addConsultantDisplayNameToSessionList(
      List<ConsultantSessionResponseDTO> consultantSessionResponseDTOs) {
    consultantSessionResponseDTOs.stream()
        .forEach(
            session -> {
              var chatOwner = session.getConsultant();
              if (nonNull(chatOwner) && nonNull(chatOwner.getUsername())) {
                try {
                  Optional<Map<String, Object>> consultantByUsername =
                      accountManager.findConsultantByUsername(chatOwner.getUsername());
                  if (consultantByUsername.isPresent()) {
                    chatOwner.setDisplayName(
                        userDtoMapper.displayNameOf(consultantByUsername.get()));
                  }
                } catch (Exception e) {
                  log.error("Error while fetching consultant by username: {}", e.getMessage());
                }
              }
            });
  }
}

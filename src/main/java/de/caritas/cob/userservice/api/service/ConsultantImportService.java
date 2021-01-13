package de.caritas.cob.userservice.api.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.admin.service.consultant.create.ConsultantCreatorService;
import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.ImportException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultantImportService {

  @Value("${consultant.import.filename}")
  private String importFilename;
  @Value("${consultant.import.protocol.filename}")
  private String protocolFilename;

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull AgencyServiceHelper agencyServiceHelper;
  private final @NonNull SessionService sessionService;
  private final @NonNull UserHelper userHelper;
  private final @NonNull ConsultantCreatorService consultantCreatorService;

  private static final String DELIMITER = ",";
  private static final String AGENCY_ROLE_DELIMITER = ";";
  private static final String YES = "ja";
  private static final boolean FORMAL_LANGUAGE_DEFAULT = true;
  private static final boolean TEAM_CONSULTANT_DEFAULT = false;
  private static final String NEWLINE_CHAR = "\r\n";
  private String protocolFile;

  public void startImport() {

    this.protocolFile = protocolFilename + "." + System.currentTimeMillis();

    Reader in;
    List<CSVRecord> records;
    String logMessage;
    Consultant consultant = null;
    String rocketChatUserId = null;

    try {
      in = new FileReader(importFilename);
      records = CSVFormat.DEFAULT.parse(in).getRecords();
    } catch (Exception exception) {
      throw new InternalServerErrorException(exception.getMessage());
    }

    for (CSVRecord record : records) {

      try {

        ImportRecord importRecord = getImportRecord(record);

        // Check if username is valid
        if (importRecord.getConsultantId() == null
            && !userHelper.isUsernameValid(importRecord.getUsername())) {
          writeToImportLog(String.format("Username length is invalid. Skipping import for %s",
              importRecord.getUsername()));
          continue;
        }

        String[] agencyRoleSetArray = importRecord.getAgenciesAndRoleSets().split(DELIMITER);

        HashSet<String> roles = new HashSet<>();
        HashSet<Long> agencyIds = new HashSet<>();
        List<Boolean> formalLanguageList = new ArrayList<>();
        for (String agencyRoleSet : agencyRoleSetArray) {

          if (!agencyRoleSet.contains(AGENCY_ROLE_DELIMITER)) {
            throw new ImportException(
                String.format("Consultant %s could not be imported: Invalid agency roleset %s",
                    importRecord.getUsername(), agencyRoleSet));
          }
          String[] agencyRoleArray = agencyRoleSet.split(AGENCY_ROLE_DELIMITER);

          AgencyDTO agency;
          try {
            agency = agencyServiceHelper.getAgencyWithoutCaching(Long.valueOf(agencyRoleArray[0]));
          } catch (AgencyServiceHelperException agencyServiceHelperException) {
            throw new ImportException(
                String.format("Consultant %s could not be imported: Invalid agency id %s",
                    importRecord.getUsername(), agencyRoleArray[0]));
          }

          if (agency == null) {
            throw new ImportException(
                String.format("Consultant %s could not be imported: Invalid agency id %s",
                    importRecord.getUsername(), agencyRoleArray[0]));
          }

          agencyIds.add(Long.valueOf(agencyRoleArray[0]));

          Optional<ConsultingType> consultingType = Optional.of(agency.getConsultingType());

          ConsultingTypeSettings consultingTypeSettings =
              consultingTypeManager.getConsultingTypeSettings(consultingType.get());

          if (!consultingTypeSettings.getRoles().getConsultant().getRoleNames()
              .containsKey(agencyRoleArray[1])) {
            throw new ImportException(String.format(
                "Consultant %s could not be imported: invalid role set %s for agency id %s and consulting type %s",
                importRecord.getUsername(), agencyRoleArray[1], agencyRoleArray[0],
                consultingType.get().getValue()));
          }

          for (Map.Entry<String, List<String>> roleSet : consultingTypeSettings.getRoles()
              .getConsultant().getRoleNames().entrySet()) {
            if (roleSet.getKey().equals(agencyRoleArray[1])) {
              roles.addAll(roleSet.getValue());
              break;
            }
          }

          formalLanguageList.add(consultingTypeSettings.isLanguageFormal());

          if (isTrue(agency.getTeamAgency())) {
            importRecord.setTeamConsultant(true);
          }

        }

        if (formalLanguageList.size() == 1) {
          importRecord.setFormalLanguage(formalLanguageList.get(0));
        } else {
          if (formalLanguageList.contains(Boolean.TRUE)
              && formalLanguageList.contains(Boolean.FALSE)) {
            importRecord.setFormalLanguage(FORMAL_LANGUAGE_DEFAULT);
          } else {
            importRecord.setFormalLanguage(formalLanguageList.get(0));
          }
        }

        if (importRecord.getConsultantId() == null) {
          Optional<Consultant> consultantOptional = consultantService
              .findConsultantByUsernameOrEmail(importRecord.getUsername(), importRecord.getEmail());

          if (consultantOptional.isPresent()) {
            writeToImportLog(
                String.format(
                    "Consultant with username %s (%s) exists and won't be "
                        + "imported.", importRecord.getUsername(), importRecord.getUsernameEncoded()));
            continue;
          }

          // Check if decoded username is already taken
          if (!keycloakAdminClientService.isUsernameAvailable(importRecord.getUsername())) {
            writeToImportLog(String.format(
                "Could not create Keycloak user for old id %s - username or e-mail address is already taken.",
                importRecord.getIdOld()));
            continue;
          }

        } else {

          Optional<Consultant> currentConsultant =
              consultantService.getConsultant(importRecord.getConsultantId());

          if (currentConsultant.isPresent()) {
            if (!importRecord.getUsername()
                .equals(userHelper.decodeUsername(currentConsultant.get().getUsername()))) {
              writeToImportLog(String.format(
                  "Username of consultant with id %s has changed (From %s to %s). Name changing currently not implemented. Skipped entry.",
                  importRecord.getConsultantId(),
                  userHelper.decodeUsername(currentConsultant.get().getUsername()),
                  importRecord.getUsername()));
              continue;
            }

            consultant = currentConsultant.get();
            rocketChatUserId = currentConsultant.get().getRocketChatId();

          } else {
            writeToImportLog(String.format("Consultant with id %s not found. Skipped entry.",
                importRecord.getConsultantId()));
            continue;
          }
        }

        logMessage = "=== BEGIN === " + importRecord.getUsername() + " ===";
        writeToImportLog(logMessage);

        if (importRecord.getConsultantId() == null) {
          consultant = this.consultantCreatorService.createNewConsultant(importRecord, roles);

          logMessage = "Keycloak-ID: " + consultant.getId();
          writeToImportLog(logMessage);

          logMessage = "Roles: " + String.join(",", roles);
          writeToImportLog(logMessage);

          logMessage = "RocketChat-ID: " + consultant.getRocketChatId();
          writeToImportLog(logMessage);
        }

        // create relations to agencies
        Set<ConsultantAgency> consultantAgencies = new HashSet<>();
        for (Long agencyId : agencyIds) {
          if (!consultantAgencyService.isConsultantInAgency(consultant.getId(), agencyId)) {
            consultantAgencies.add(consultantAgencyService
                .saveConsultantAgency(getConsultantAgency(consultant, agencyId)));
          }
        }
        consultant.setConsultantAgencies(consultantAgencies);

        logMessage = "Agencies: " + agencyIds.stream().map(String::valueOf)
            .collect(Collectors.joining(","));
        writeToImportLog(logMessage);

        // Enquiries
        List<ConsultantSessionResponseDTO> consultantSessionResponseDtoList =
            sessionService.getSessionsForConsultant(consultant, SessionStatus.NEW.getValue());
        if (isNotEmpty(consultantSessionResponseDtoList)) {
          for (ConsultantSessionResponseDTO consultantSessionResponseDTO : consultantSessionResponseDtoList) {
            try {
              rocketChatService
                  .addTechnicalUserToGroup(consultantSessionResponseDTO.getSession().getGroupId());
              // Add user to Rocket.Chat group
              rocketChatService.addUserToGroup(rocketChatUserId,
                  consultantSessionResponseDTO.getSession().getGroupId());
              logMessage = String.format("Consultant added to rc group %s (enquiry).",
                  consultantSessionResponseDTO.getSession().getGroupId());

              // Add user to Rocket.Chat feedback group if feedback group is existing
              if (consultantSessionResponseDTO.getSession().getFeedbackGroupId() != null) {
                rocketChatService.addUserToGroup(rocketChatUserId,
                    consultantSessionResponseDTO.getSession().getFeedbackGroupId());
                logMessage += String.format("Consultant added to rc feedback group %s (enquiry).",
                    consultantSessionResponseDTO.getSession().getFeedbackGroupId());
              }
              writeToImportLog(logMessage);
              try {
                rocketChatService.removeTechnicalUserFromGroup(
                    consultantSessionResponseDTO.getSession().getGroupId());
              } catch (RocketChatRemoveUserFromGroupException e) {
                logMessage = String.format(
                    "ERROR: Technical user could not be removed from rc group %s (enquiry).",
                    consultantSessionResponseDTO.getSession().getGroupId());
                writeToImportLog(logMessage);
              }
            } catch (Exception e) {
              throw new ImportException(String.format(
                  "ERROR: Consultant could not be added to rc group %s: Technical user could not be added to group (enquiry).",
                  consultantSessionResponseDTO.getSession().getGroupId()));
            }
          }
        }

        // Team-sessions
        logMessage = "";
        List<ConsultantSessionResponseDTO> consultantTeamSessionResponseDtoList =
            sessionService.getTeamSessionsForConsultant(consultant);
        if (isNotEmpty(consultantTeamSessionResponseDtoList)) {
          for (ConsultantSessionResponseDTO consultantTeamSessionResponseDto : consultantTeamSessionResponseDtoList) {

            try {
              rocketChatService.addTechnicalUserToGroup(
                  consultantTeamSessionResponseDto.getSession().getGroupId());
              ConsultingTypeSettings consultingTypeSettings =
                  consultingTypeManager.getConsultingTypeSettings(ConsultingType
                      .valueOf(consultantTeamSessionResponseDto.getSession().getConsultingType())
                      .get());
              boolean isMainConsultant = keycloakAdminClientService
                  .userHasAuthority(consultant.getId(), Authority.VIEW_ALL_FEEDBACK_SESSIONS)
                  || roles.contains(UserRole.U25_MAIN_CONSULTANT.name());

              // Add user to Rocket.Chat group if it is no U25 session or if it is an U25 main
              // consultant
              if (!consultingTypeSettings.getConsultingType().equals(ConsultingType.U25)
                  || isMainConsultant) {
                rocketChatService.addUserToGroup(rocketChatUserId,
                    consultantTeamSessionResponseDto.getSession().getGroupId());
                logMessage = String.format("Consultant added to rc group %s (team-session).",
                    consultantTeamSessionResponseDto.getSession().getGroupId());
              }

              // Add user to Rocket.Chat feedback group if feedback group is existing and user has
              // the right to view all feedback sessions
              if (consultantTeamSessionResponseDto.getSession().getFeedbackGroupId() != null
                  && isMainConsultant) {

                rocketChatService.addUserToGroup(rocketChatUserId,
                    consultantTeamSessionResponseDto.getSession().getFeedbackGroupId());
                logMessage +=
                    String.format("Consultant added to rc feedback group %s (team-session).",
                        consultantTeamSessionResponseDto.getSession().getFeedbackGroupId());
              }

              writeToImportLog(logMessage);
              try {
                rocketChatService.removeTechnicalUserFromGroup(
                    consultantTeamSessionResponseDto.getSession().getGroupId());
              } catch (RocketChatRemoveUserFromGroupException e) {
                logMessage = String.format(
                    "ERROR: Technical user could not be removed from rc group %s (team-session).",
                    consultantTeamSessionResponseDto.getSession().getGroupId());
                writeToImportLog(logMessage);
              }
            } catch (Exception e) {
              throw new ImportException(String.format(
                  "ERROR: Consultant could not be added to rc group %s: Technical user could not be added to group (team-session).",
                  consultantTeamSessionResponseDto.getSession().getGroupId()));
            }
          }
        }

        logMessage = "=== END === " + importRecord.getUsername() + " ===" + NEWLINE_CHAR;
        writeToImportLog(logMessage);

      } catch (ImportException wontImportException) {
        writeToImportLog(wontImportException.getMessage());
        break;
      } catch (Exception fileNotFoundException) {
        fileNotFoundException.printStackTrace();
        break;
      }

    }

    try {
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeToImportLog(String message) {
    if (message.length() < 1) {
      return;
    }

    try {
      Files.write(Paths.get(protocolFile), (message + NEWLINE_CHAR).getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private ConsultantAgency getConsultantAgency(Consultant consultant, Long agencyId) {

    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(agencyId);
    consultantAgency.setConsultant(consultant);
    consultantAgency.setCreateDate(LocalDateTime.now());
    consultantAgency.setUpdateDate(LocalDateTime.now());
    return consultantAgency;
  }

  private ImportRecord getImportRecord(CSVRecord record) {
    ImportRecord importRecord = new ImportRecord();
    importRecord
        .setConsultantId((record.get(0).trim().equals(StringUtils.EMPTY)) ? null : record.get(0));
    importRecord.setIdOld(
        (record.get(1).trim().equals(StringUtils.EMPTY)) ? null : Long.valueOf(record.get(1)));
    importRecord.setUsername(StringUtils.trim(record.get(2)));
    importRecord.setUsernameEncoded(userHelper.encodeUsername(StringUtils.trim(record.get(2))));
    importRecord.setFirstName(StringUtils.trim(record.get(3)));
    importRecord.setLastName(StringUtils.trim(record.get(4)));
    String email = StringUtils.deleteWhitespace(record.get(5));
    // If there is more than one email addresses...than catch the first one
    if (email.contains(DELIMITER)) {
      email = email.substring(0, email.indexOf(DELIMITER)).trim();
    }
    if (!EmailValidator.getInstance().isValid(email)) {
      throw new ImportException(
          String.format("Consultant %s could not be imported: Invalid email address",
              importRecord.getUsername()));
    }
    importRecord.setEmail(email);
    importRecord.setAbsent(record.get(6).equals(YES));
    String absenceMessage =
        (record.get(7).trim().equals(StringUtils.EMPTY)) ? null : record.get(7).trim();
    if (absenceMessage != null) {
      absenceMessage = absenceMessage.replace("<br>", "\r\n");
    }
    importRecord.setAbsenceMessage(absenceMessage);
    importRecord.setAgenciesAndRoleSets(record.get(8));
    return importRecord;
  }

  @Getter
  @Setter
  public static class ImportRecord {

    String consultantId;
    Long idOld;
    String username;
    String usernameEncoded;
    String firstName;
    String lastName;
    String email;
    boolean isAbsent;
    String absenceMessage;
    String agenciesAndRoleSets;
    boolean formalLanguage = FORMAL_LANGUAGE_DEFAULT;
    boolean isTeamConsultant = TEAM_CONSULTANT_DEFAULT;

  }

}

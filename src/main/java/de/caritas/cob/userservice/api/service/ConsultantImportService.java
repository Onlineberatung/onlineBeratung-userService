package de.caritas.cob.userservice.api.service;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.authorization.Authority;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.ImportException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import lombok.Getter;
import lombok.Setter;

@Service
public class ConsultantImportService {

  @Value("${consultant.import.filename}")
  private String importFilename;
  @Value("${consultant.import.protocol.filename}")
  private String protocolFilename;

  @Autowired
  private KeycloakAdminClientHelper keycloakAdminClientHelper;
  @Autowired
  private ConsultantService consultantService;
  @Autowired
  private ConsultantAgencyService consultantAgencyService;
  @Autowired
  private RocketChatService rocketChatService;
  @Autowired
  private ConsultingTypeManager consultingTypeManager;
  @Autowired
  private AgencyServiceHelper agencyServiceHelper;
  @Autowired
  private SessionService sessionService;
  @Autowired
  private UserHelper userHelper;

  private final String DELIMITER = ",";
  private final String AGENCY_ROLE_DELIMITER = ";";
  private final String YES = "ja";
  private final boolean FORMAL_LANGUAGE_DEFAULT = true;
  private final boolean TEAM_CONSULTANT_DEFAULT = false;
  private final String NEWLINE_CHAR = "\r\n";
  private final String IMPORT_LOG_CHARSET = "UTF-8";
  private String protocolFile;

  public void startImport() {

    protocolFile = protocolFilename + "." + System.currentTimeMillis();

    Reader in;
    Iterable<CSVRecord> records = null;
    String logMessage;
    String keycloakUserId;
    Consultant consultant = null;
    String rocketChatUserId = null;

    try {
      in = new FileReader(importFilename);
      records = CSVFormat.DEFAULT.parse(in);
    } catch (Exception exception) {
      throw new InternalServerErrorException(exception);
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

        HashSet<String> roles = new HashSet<String>();
        HashSet<Long> agencyIds = new HashSet<Long>();
        List<Boolean> formalLanguageList = new ArrayList<Boolean>();
        for (String agencyRoleSet : agencyRoleSetArray) {

          if (!agencyRoleSet.contains(AGENCY_ROLE_DELIMITER)) {
            throw new ImportException(
                String.format("Consultant %s could not be imported: Invalid agency roleset %s",
                    importRecord.getUsername(), agencyRoleSet));
          }
          String[] agencyRoleArray = agencyRoleSet.split(AGENCY_ROLE_DELIMITER);

          AgencyDTO agency = null;
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
              consultingTypeManager.getConsultantTypeSettings(consultingType.get());

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
                String.format("Consultant with username %s exists and won't be imported.",
                    importRecord.getUsername()));
            continue;
          }

          // Check if decoded username is already taken
          if (!userHelper.isUsernameAvailable(importRecord.getUsername())) {
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
          // Create keycloak user
          UserDTO userDto = getUserDTO(importRecord.getUsername(), importRecord.getEmail());
          KeycloakCreateUserResponseDTO response;
          try {
            response = keycloakAdminClientHelper.createKeycloakUser(userDto,
                importRecord.getFirstName(), importRecord.getLastName());
          } catch (KeycloakException keycloakException) {
            throw new ImportException(String.format("ERROR: Keycloak user could not be created: %s",
                keycloakException.getMessage()));
          } catch (Exception ex) {
            throw new ImportException(
                String.format("ERROR: Keycloak user could not be created: %s", ex.getMessage()));
          }

          if (response.getUserId() == null) {
            throw new ImportException("ERROR: Keycloak user id is missing");
          }

          keycloakUserId = response.getUserId();

          logMessage = "Keycloak-ID: " + keycloakUserId;
          writeToImportLog(logMessage);

          // Set keycloak password
          String password = userHelper.getRandomPassword();
          keycloakAdminClientHelper.updatePassword(keycloakUserId, password);

          // Set consultant role
          for (String roleName : roles) {
            keycloakAdminClientHelper.updateRole(keycloakUserId, roleName);
          }

          logMessage = "Roles: " + roles.stream().collect(Collectors.joining(","));
          writeToImportLog(logMessage);

          // Get the Rocket.Chat ID
          rocketChatUserId =
              rocketChatService.getUserID(importRecord.getUsernameEncoded(), password, true);

          logMessage = "RocketChat-ID: " + rocketChatUserId;
          writeToImportLog(logMessage);

          // create consultant in db
          consultant = consultantService.saveConsultant(getConsultant(keycloakUserId,
              importRecord.getUsernameEncoded(), importRecord.getFirstName(),
              importRecord.getLastName(), importRecord.getEmail(), importRecord.isAbsent,
              importRecord.getAbsenceMessage(), importRecord.isTeamConsultant(),
              importRecord.getIdOld(), rocketChatUserId, importRecord.isFormalLanguage()));
        }

        // create relations to agencies
        Set<ConsultantAgency> consultantAgencies = new HashSet<ConsultantAgency>();
        for (Long agencyId : agencyIds) {
          if (!consultantAgencyService.isConsultantInAgency(consultant.getId(), agencyId)) {
            consultantAgencies.add(consultantAgencyService
                .saveConsultantAgency(getConsultantAgency(consultant, agencyId)));
          }
        }
        consultant.setConsultantAgencies(consultantAgencies);

        logMessage = "Agencies: " + agencyIds.stream().map(agencyId -> String.valueOf(agencyId))
            .collect(Collectors.joining(","));
        writeToImportLog(logMessage);

        // Enquiries
        List<ConsultantSessionResponseDTO> consultantSessionResponseDtoList =
            sessionService.getSessionsForConsultant(consultant, SessionStatus.NEW.getValue());
        if (consultantSessionResponseDtoList != null
            && consultantSessionResponseDtoList.size() > 0) {
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
        if (consultantTeamSessionResponseDtoList != null
            && consultantTeamSessionResponseDtoList.size() > 0) {
          for (ConsultantSessionResponseDTO consultantTeamSessionResponseDto : consultantTeamSessionResponseDtoList) {

            try {
              rocketChatService.addTechnicalUserToGroup(
                  consultantTeamSessionResponseDto.getSession().getGroupId());
              ConsultingTypeSettings consultingTypeSettings =
                  consultingTypeManager.getConsultantTypeSettings(ConsultingType
                      .valueOf(consultantTeamSessionResponseDto.getSession().getConsultingType())
                      .get());
              boolean isMainConsultant = keycloakAdminClientHelper
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

        logMessage = "=== END === " + importRecord.getUsername() + " ===";
        writeToImportLog(logMessage);

      } catch (ImportException wontImportException) {
        writeToImportLog(wontImportException.getMessage());
        break;
      } catch (FileNotFoundException fileNotFoundException) {
        fileNotFoundException.printStackTrace();
        break;
      } catch (IOException ioException) {
        ioException.printStackTrace();
        break;
      } catch (Exception exception) {
        exception.printStackTrace();
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
      Files.write(Paths.get(protocolFile), (message + NEWLINE_CHAR).getBytes(IMPORT_LOG_CHARSET),
          StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private UserDTO getUserDTO(String username, String email) {
    UserDTO userDto = new UserDTO();
    userDto.setUsername(userHelper.encodeUsername(username));
    userDto.setEmail(email);
    return userDto;
  }

  private Consultant getConsultant(String consultantId, String username, String firstName,
      String lastName, String email, boolean isAbsent, String absenceMessage,
      boolean isTeamConsultant, Long idOld, String rocketChatUserId, boolean languageFormal) {
    Consultant consultant = new Consultant();
    consultant.setId(consultantId);
    consultant.setUsername(username);
    consultant.setFirstName(firstName);
    consultant.setLastName(lastName);
    consultant.setAbsent(isAbsent);
    consultant.setAbsenceMessage(absenceMessage);
    consultant.setIdOld(idOld);
    consultant.setEmail(email);
    consultant.setTeamConsultant(isTeamConsultant);
    consultant.setRocketChatId(rocketChatUserId);
    consultant.setLanguageFormal(languageFormal);
    return consultant;
  }

  private ConsultantAgency getConsultantAgency(Consultant consultant, Long agencyId) {

    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(agencyId);
    consultantAgency.setConsultant(consultant);
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
    if (email.indexOf(DELIMITER) != -1) {
      email = email.substring(0, email.indexOf(DELIMITER)).trim();
    }
    if (!EmailValidator.getInstance().isValid(email)) {
      throw new ImportException(
          String.format("Consultant %s could not be imported: Invalid email address",
              importRecord.getUsername()));
    }
    importRecord.setEmail(email);
    importRecord.setAbsent((record.get(6).equals(YES)) ? true : false);
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
  private class ImportRecord {

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

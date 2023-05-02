package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.helper.SessionDataProvider.fromUserDTO;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.ImportException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.RocketChatRoomNameGenerator;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.message.MessageServiceProvider;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/** Imports the askers from the created CSV file of the old Caritas system. */
@Service
@RequiredArgsConstructor
public class AskerImportService {

  @Value("${asker.import.filename}")
  private String importFilenameAsker;

  @Value("${asker.import.withoutsession.filename}")
  private String importFilenameAskerWithoutSession;

  @Value("${asker.import.protocol.filename}")
  private String protocolFilename;

  @Value("${rocket.systemuser.username}")
  private String ROCKET_CHAT_SYSTEM_USER_USERNAME;

  @Value("${rocket.systemuser.password}")
  private String ROCKET_CHAT_SYSTEM_USER_PASSWORD;

  @Value("${asker.import.welcome.message.filename}")
  private String welcomeMsgFilename;

  @Value("${asker.import.welcome.message.filename.replace.value}")
  private String welcomeMsgFilenameReplaceValue;

  private final String NEWLINE_CHAR = "\r\n";
  private final String IMPORT_CHARSET = "UTF-8";
  private final String IMPORT_LOG_CHARSET = "UTF-8";
  private final String DUMMY_POSTCODE = "00000";
  private final @NonNull IdentityClient identityClient;
  private final @NonNull UserService userService;
  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull SessionDataService sessionDataService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull MessageServiceProvider messageServiceProvider;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull AgencyService agencyService;
  private final @NonNull UserHelper userHelper;
  private final @NonNull UserAgencyService userAgencyService;
  private final @NonNull RocketChatCredentialsProvider rocketChatCredentialsProvider;
  private final RocketChatRoomNameGenerator rocketChatRoomNameGenerator =
      new RocketChatRoomNameGenerator();

  /** Imports askers without session by a predefined import list (for the format see readme.md) */
  public void startImportForAskersWithoutSession() {

    String protocolFile = protocolFilename + "." + System.currentTimeMillis();
    Reader in;
    Iterable<CSVRecord> records = null;

    try {

      in = new FileReader(importFilenameAskerWithoutSession);
      records = CSVFormat.DEFAULT.parse(in);

    } catch (Exception exception) {
      writeToImportLog(
          String.format(
              "Error while reading import file: %s",
              org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception)),
          protocolFile);
      return;
    }

    for (CSVRecord csvRecord : records) {

      try {
        ImportRecordAskerWithoutSession record = getImportRecordAskerWithoutSession(csvRecord);

        // Check if username is valid
        if (!userHelper.isUsernameValid(record.getUsername())) {
          writeToImportLog(
              String.format(
                  "Username length is invalid. Skipping import for user %s", record.getUsername()),
              protocolFile);
          continue;
        }

        // Get the agency
        AgencyDTO agencyDTO =
            agencyService.getAgencyWithoutCaching(Long.valueOf(record.getAgencyId()));

        if (agencyDTO == null) {
          throw new ImportException(
              String.format(
                  "Could not get consulting type (agency) for user %s", record.getUsername()));
        }

        // Check if decoded username is already taken
        if (!identityClient.isUsernameAvailable(record.getUsername())) {
          writeToImportLog(
              String.format(
                  "Could not create Keycloak user %s - username or e-mail address is already taken.",
                  record.getUsername()),
              protocolFile);
          continue;
        }

        UserDTO userDTO =
            convertAskerWithoutSessionToUserDTO(record, agencyDTO.getConsultingType());

        // Create Keycloak user
        KeycloakCreateUserResponseDTO response = identityClient.createKeycloakUser(userDTO, "", "");
        String keycloakUserId = response.getUserId();

        if (record.getEmail() == null || record.getEmail().equals(StringUtils.EMPTY)) {
          userDTO.setEmail(userHelper.getDummyEmail(keycloakUserId));
          identityClient.updateDummyEmail(keycloakUserId, userDTO);
        }

        // Set Keycloak password
        identityClient.updatePassword(keycloakUserId, record.getPassword());

        // Set asker/user role
        identityClient.updateUserRole(keycloakUserId);

        // Create user in MariaDB
        ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
            consultingTypeManager.getConsultingTypeSettings(agencyDTO.getConsultingType());
        User dbUser =
            userService.createUser(
                keycloakUserId,
                record.getIdOld(),
                record.getUsernameEncoded(),
                userDTO.getEmail(),
                extendedConsultingTypeResponseDTO.getLanguageFormal());
        if (dbUser.getUserId() == null || dbUser.getUserId().equals(StringUtils.EMPTY)) {
          throw new ImportException(
              String.format("Could not create user %s in mariaDB", record.getUsername()));
        }

        // Log in user to Rocket.Chat
        ResponseEntity<LoginResponseDTO> rcUserResponse =
            rocketChatService.loginUserFirstTime(record.getUsernameEncoded(), record.getPassword());
        String rcUserToken = rcUserResponse.getBody().getData().getAuthToken();
        String rcUserId = rcUserResponse.getBody().getData().getUserId();
        if (rcUserToken == null
            || rcUserToken.equals(StringUtils.EMPTY)
            || rcUserId == null
            || rcUserId.equals(StringUtils.EMPTY)) {
          throw new ImportException(
              String.format("Could not log in user %s into Rocket.Chat", record.getUsername()));
        }

        // Log out user from Rocket.Chat
        RocketChatCredentials rocketChatUserCredentials =
            RocketChatCredentials.builder()
                .rocketChatToken(rcUserToken)
                .rocketChatUserId(rcUserId)
                .build();
        rocketChatService.logoutUser(rocketChatUserCredentials);

        // Update rcUserId in user table
        dbUser.setRcUserId(rcUserId);
        User updatedUser = userService.saveUser(dbUser);
        if (updatedUser.getUserId() == null || updatedUser.getUserId().equals(StringUtils.EMPTY)) {
          throw new ImportException(
              String.format(
                  "Could not update Rocket.Chat user id for user %s", record.getUsername()));
        }

        // Create user-agency-relation
        UserAgency userAgency = getUserAgency(dbUser, agencyDTO.getId());
        userAgencyService.saveUserAgency(userAgency);

        writeToImportLog(
            String.format(
                "User with old id %s and username %s imported. New id: %s",
                record.getIdOld(), record.getUsername(), dbUser.getUserId()),
            protocolFile);

      } catch (ImportException importException) {
        writeToImportLog(importException.getMessage(), protocolFile);
        break;
      } catch (InternalServerErrorException serviceException) {
        writeToImportLog(serviceException.getMessage(), protocolFile);
        break;
      } catch (RocketChatLoginException rcLoginException) {
        writeToImportLog(rcLoginException.getMessage(), protocolFile);
        break;
      } catch (CustomValidationHttpStatusException e) {
        writeToImportLog(
            String.format(
                "Could not create Keycloak user for user %s - username or e-mail address is already taken.",
                getImportRecordAskerWithoutSession(csvRecord).getUsername()),
            protocolFile);
        break;
      } catch (Exception exception) {
        writeToImportLog(
            org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception),
            protocolFile);
        break;
      }
    }
  }

  /** Imports askers by a predefined import list (for the format see readme.md) */
  public void startImport() {

    String protocolFile = protocolFilename + "." + System.currentTimeMillis();
    Reader in;
    Iterable<CSVRecord> records = null;
    String systemUserId;
    String systemUserToken;

    // Read in asker import file, log in Rocket.Chat system message user to get the token and read
    // in welcome messages
    try {
      in = new FileReader(importFilenameAsker);
      records = CSVFormat.DEFAULT.parse(in);

      ResponseEntity<LoginResponseDTO> rcSystemUserResonse =
          rocketChatCredentialsProvider.loginUser(
              ROCKET_CHAT_SYSTEM_USER_USERNAME, ROCKET_CHAT_SYSTEM_USER_PASSWORD);
      systemUserId = rcSystemUserResonse.getBody().getData().getUserId();
      systemUserToken = rcSystemUserResonse.getBody().getData().getAuthToken();

      if (rcSystemUserResonse == null
          || rcSystemUserResonse.getStatusCode() != HttpStatus.OK
          || systemUserId == null
          || systemUserToken == null) {
        throw new ImportException("Could not log in Rocket.Chat system message user.");
      }

    } catch (ImportException importExcetion) {
      writeToImportLog(importExcetion.getMessage(), protocolFile);
      return;
    } catch (Exception exception) {
      writeToImportLog(
          String.format(
              "Error while reading import file or logging in Rocket.Chat system message user: %s",
              org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception)),
          protocolFile);
      return;
    }

    for (CSVRecord csvRecord : records) {

      try {
        ImportRecordAsker record = getImportRecordAsker(csvRecord);

        // Check if username is valid
        if (!userHelper.isUsernameValid(record.getUsername())) {
          writeToImportLog(
              String.format(
                  "Username length is invalid. Skipping import for user %s", record.getUsername()),
              protocolFile);
          continue;
        }

        // Get the agency for the consulting type
        AgencyDTO agencyDTO =
            agencyService.getAgencyWithoutCaching(Long.valueOf(record.getAgencyId()));

        if (agencyDTO == null) {
          throw new ImportException(
              String.format(
                  "Could not get consulting type (agency) for user %s", record.getUsername()));
        }

        // Check if consultant exists and is in agency
        Optional<Consultant> consultant = consultantService.getConsultant(record.getConsultantId());
        if (!consultant.isPresent()) {
          writeToImportLog(
              String.format(
                  "Consultant with id %s does not exist. Skipping import of user %s",
                  record.getConsultantId(), record.getUsername()),
              protocolFile);
          continue;
        }
        if (!consultant.get().getConsultantAgencies().stream()
            .anyMatch(agency -> Objects.equals(agency.getAgencyId(), record.getAgencyId()))) {
          writeToImportLog(
              String.format(
                  "Consultant with id %s is not in agency %s. Skipping import of user %s",
                  record.getConsultantId(), record.getAgencyId(), record.getUsername()),
              protocolFile);
          continue;
        }

        UserDTO userDTO = convertAskerToUserDTO(record, agencyDTO.getConsultingType());

        // Check if decoded username is already taken
        if (!identityClient.isUsernameAvailable(record.getUsername())) {
          writeToImportLog(
              String.format(
                  "Could not create Keycloak user %s - username or e-mail address is already taken.",
                  record.getUsername()),
              protocolFile);
          continue;
        }

        // Create Keycloak user
        KeycloakCreateUserResponseDTO response = identityClient.createKeycloakUser(userDTO, "", "");
        String keycloakUserId = response.getUserId();

        if (record.getEmail() == null || record.getEmail().equals(StringUtils.EMPTY)) {
          userDTO.setEmail(userHelper.getDummyEmail(keycloakUserId));
          identityClient.updateDummyEmail(keycloakUserId, userDTO);
        }

        // Set Keycloak password
        identityClient.updatePassword(keycloakUserId, record.getPassword());

        // Set asker/user role
        identityClient.updateUserRole(keycloakUserId);

        // Create user in MariaDB
        ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
            consultingTypeManager.getConsultingTypeSettings(agencyDTO.getConsultingType());
        User dbUser =
            userService.createUser(
                keycloakUserId,
                record.getIdOld(),
                record.getUsernameEncoded(),
                userDTO.getEmail(),
                extendedConsultingTypeResponseDTO.getLanguageFormal());
        if (dbUser.getUserId() == null || dbUser.getUserId().equals(StringUtils.EMPTY)) {
          throw new ImportException(
              String.format("Could not create user %s in mariaDB", record.getUsername()));
        }

        // Initialize Session (need session id for Rocket.Chat group name)
        Session session =
            sessionService.initializeSession(dbUser, userDTO, isTrue(agencyDTO.getTeamAgency()));
        if (session.getId() == null) {
          throw new ImportException(
              String.format("Could not create session for user %s", record.getUsername()));
        }

        // Log in user to Rocket.Chat
        ResponseEntity<LoginResponseDTO> rcUserResponse =
            rocketChatService.loginUserFirstTime(record.getUsernameEncoded(), record.getPassword());
        String rcUserToken = rcUserResponse.getBody().getData().getAuthToken();
        String rcUserId = rcUserResponse.getBody().getData().getUserId();
        if (rcUserToken == null
            || rcUserToken.equals(StringUtils.EMPTY)
            || rcUserId == null
            || rcUserId.equals(StringUtils.EMPTY)) {
          throw new ImportException(
              String.format("Could not log in user %s into Rocket.Chat", record.getUsername()));
        }

        // Create Rocket.Chat group
        RocketChatCredentials rocketChatUserCredentials =
            RocketChatCredentials.builder()
                .rocketChatToken(rcUserToken)
                .rocketChatUserId(rcUserId)
                .build();
        String rcGroupId =
            rocketChatService
                .createPrivateGroup(
                    rocketChatRoomNameGenerator.generateGroupName(session),
                    rocketChatUserCredentials)
                .get()
                .getGroup()
                .getId();
        if (rcGroupId == null || rcGroupId.equals(StringUtils.EMPTY)) {
          throw new ImportException(
              String.format(
                  "Could not create Rocket.Chat group for user %s", record.getUsername()));
        }

        // Log out user from Rocket.Chat
        rocketChatService.logoutUser(rocketChatUserCredentials);

        // Update rcUserId in user table
        dbUser.setRcUserId(rcUserId);
        User updatedUser = userService.saveUser(dbUser);
        if (updatedUser.getUserId() == null || updatedUser.getUserId().equals(StringUtils.EMPTY)) {
          throw new ImportException(
              String.format(
                  "Could not update Rocket.Chat user id for user %s", record.getUsername()));
        }

        List<ConsultantAgency> agencyList =
            consultantAgencyService.findConsultantsByAgencyId(record.getAgencyId());

        // Create feedback group and add consultants if enabled for this agency/consulting type
        if (extendedConsultingTypeResponseDTO.getInitializeFeedbackChat().booleanValue()) {
          String rcFeedbackGroupId =
              rocketChatService
                  .createPrivateGroupWithSystemUser(
                      rocketChatRoomNameGenerator.generateFeedbackGroupName(session))
                  .get()
                  .getGroup()
                  .getId();
          if (rcFeedbackGroupId == null || rcFeedbackGroupId.equals(StringUtils.EMPTY)) {
            throw new ImportException(
                String.format(
                    "Could not create Rocket.Chat feedback group for user %s",
                    record.getUsername()));
          }

          // Add the assigned consultant and all consultants of the session's agency to the feedback
          // group that have the right to view all feedback sessions
          for (ConsultantAgency agency : agencyList) {
            if (identityClient.userHasAuthority(
                    agency.getConsultant().getId(), AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS)
                || agency.getConsultant().getId().equals(record.getConsultantId())) {
              rocketChatService.addUserToGroup(
                  agency.getConsultant().getRocketChatId(), rcFeedbackGroupId);
            }
          }

          // Remove all system messages from feedback group
          try {
            rocketChatService.removeSystemMessages(
                rcFeedbackGroupId, nowInUtc().minusHours(Helper.ONE_DAY_IN_HOURS), nowInUtc());
          } catch (RocketChatRemoveSystemMessagesException e) {
            throw new ImportException(
                String.format(
                    "Could not remove system messages from feedback group id %s for user %s",
                    rcFeedbackGroupId, record.getUsername()));
          }

          // Update the session's feedback group id
          sessionService.updateFeedbackGroupId(session, rcFeedbackGroupId);
        }

        // Update session data by Rocket.Chat group id and consultant id
        session.setConsultant(consultant.get());
        session.setGroupId(rcGroupId);
        session.setEnquiryMessageDate(nowInUtc());
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setCreateDate(nowInUtc());
        session.setUpdateDate(nowInUtc());
        Session updatedSession = sessionService.saveSession(session);
        if (updatedSession.getId() == null) {
          throw new ImportException(
              String.format("Could update session for user %s", record.getUsername()));
        }

        // Add consultant(s) to Rocket.Chat group
        if (isTrue(agencyDTO.getTeamAgency())) {
          if (agencyList != null) {
            for (ConsultantAgency agency : agencyList) {
              // If feedback chat enabled add all main consultants and the assigned consultant. If
              // it is a "normal" team session add all consultants.
              if (extendedConsultingTypeResponseDTO.getInitializeFeedbackChat().booleanValue()) {
                if (identityClient.userHasAuthority(
                        agency.getConsultant().getId(), AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS)
                    || agency.getConsultant().getId().equals(record.getConsultantId())) {
                  rocketChatService.addUserToGroup(
                      agency.getConsultant().getRocketChatId(), rcGroupId);
                }
              } else {
                rocketChatService.addUserToGroup(
                    agency.getConsultant().getRocketChatId(), rcGroupId);
              }
            }
          }

        } else {
          rocketChatService.addUserToGroup(consultant.get().getRocketChatId(), rcGroupId);
        }

        // Add system message user to Rocket.Chat group
        rocketChatService.addUserToGroup(systemUserId, rcGroupId);

        // Send welcome message
        messageServiceProvider.postWelcomeMessageIfConfigured(
            rcGroupId,
            dbUser,
            extendedConsultingTypeResponseDTO,
            CreateEnquiryExceptionInformation.builder().build());

        // Remove all system messages from group
        try {
          rocketChatService.removeSystemMessages(
              rcGroupId, nowInUtc().minusHours(Helper.ONE_DAY_IN_HOURS), nowInUtc());
        } catch (RocketChatRemoveSystemMessagesException e) {
          throw new ImportException(
              String.format(
                  "Could not remove system messages from group id %s for user %s",
                  rcGroupId, record.getUsername()));
        }

        // Save session data
        sessionDataService.saveSessionData(session, fromUserDTO(userDTO));

        writeToImportLog(
            String.format(
                "User with old id %s and username %s imported. New id: %s",
                record.getIdOld(), record.getUsername(), dbUser.getUserId()),
            protocolFile);

      } catch (ImportException importException) {
        writeToImportLog(importException.getMessage(), protocolFile);
        break;
      } catch (InternalServerErrorException
          | RocketChatPostWelcomeMessageException serviceException) {
        writeToImportLog(serviceException.getMessage(), protocolFile);
        break;
      } catch (RocketChatLoginException rcLoginException) {
        writeToImportLog(rcLoginException.getMessage(), protocolFile);
        break;
      } catch (RocketChatCreateGroupException rcCreateGroupException) {
        writeToImportLog(rcCreateGroupException.getMessage(), protocolFile);
        break;
      } catch (Exception exception) {
        writeToImportLog(
            org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(exception),
            protocolFile);
        break;
      }
    }

    try {
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeToImportLog(String message, String protocolFile) {
    try {
      Files.write(
          Paths.get(protocolFile),
          (message + NEWLINE_CHAR).getBytes(IMPORT_LOG_CHARSET),
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private UserDTO convertAskerToUserDTO(ImportRecordAsker record, int consultingTypeId) {
    return new UserDTO(
        record.getUsernameEncoded(),
        record.getPostcode(),
        record.getAgencyId(),
        record.getPassword(),
        record.getEmail(),
        new Date().toString(),
        Integer.toString(consultingTypeId));
  }

  private UserDTO convertAskerWithoutSessionToUserDTO(
      ImportRecordAskerWithoutSession record, int consultingTypeId) {
    return new UserDTO(
        record.getUsernameEncoded(),
        DUMMY_POSTCODE,
        record.getAgencyId(),
        record.getPassword(),
        record.getEmail(),
        new Date().toString(),
        Integer.toString(consultingTypeId));
  }

  /**
   * Returns a {@link ImportRecordAsker} object for the given CSV record line. Generates a random
   * password if no password is specified.
   */
  private ImportRecordAsker getImportRecordAsker(CSVRecord record) {
    ImportRecordAsker importRecord = new ImportRecordAsker();
    importRecord.setIdOld(
        (record.get(0).trim().equals(StringUtils.EMPTY)) ? null : Long.valueOf(record.get(0)));
    importRecord.setUsername(StringUtils.trim(record.get(1)));
    importRecord.setUsernameEncoded(
        new UsernameTranscoder().encodeUsername(StringUtils.trim(record.get(1))));
    String email =
        StringUtils.deleteWhitespace(
            record.get(2).trim().equals(StringUtils.EMPTY) ? "" : record.get(2).trim());
    if (!email.equals(StringUtils.EMPTY) && !EmailValidator.getInstance().isValid(email)) {
      throw new ImportException(
          String.format(
              "Asker with old id %s could not be imported: Invalid email address",
              importRecord.getIdOld()));
    }
    importRecord.setEmail(email);
    importRecord.setConsultantId(StringUtils.deleteWhitespace(record.get(3)));
    importRecord.setPostcode(StringUtils.deleteWhitespace(record.get(4)));
    importRecord.setAgencyId(Long.valueOf(record.get(5)));
    importRecord.setPassword(
        record.get(6).trim().equals(StringUtils.EMPTY)
            ? userHelper.getRandomPassword()
            : record.get(6));

    return importRecord;
  }

  /**
   * Returns a {@link ImportRecordAskerWithoutSession} object for the given CSV record line.
   * Generates a random password if no password is specified.
   */
  private ImportRecordAskerWithoutSession getImportRecordAskerWithoutSession(CSVRecord record) {
    ImportRecordAskerWithoutSession importRecord = new ImportRecordAskerWithoutSession();
    importRecord.setIdOld(
        (record.get(0).trim().equals(StringUtils.EMPTY)) ? null : Long.valueOf(record.get(0)));
    importRecord.setUsername(StringUtils.trim(record.get(1)));
    importRecord.setUsernameEncoded(
        new UsernameTranscoder().encodeUsername(StringUtils.trim(record.get(1))));
    String email =
        StringUtils.deleteWhitespace(
            record.get(2).trim().equals(StringUtils.EMPTY) ? "" : record.get(2).trim());
    if (!email.equals(StringUtils.EMPTY) && !EmailValidator.getInstance().isValid(email)) {
      throw new ImportException(
          String.format(
              "Asker with old id %s could not be imported: Invalid email address",
              importRecord.getIdOld()));
    }
    importRecord.setEmail(email);
    importRecord.setAgencyId(Long.valueOf(record.get(3)));
    importRecord.setPassword(
        record.get(4).trim().equals(StringUtils.EMPTY)
            ? userHelper.getRandomPassword()
            : record.get(4));

    return importRecord;
  }

  private UserAgency getUserAgency(User user, Long agencyId) {

    UserAgency userAgency = new UserAgency();
    userAgency.setAgencyId(agencyId);
    userAgency.setUser(user);
    return userAgency;
  }

  @Getter
  @Setter
  private class ImportRecordAsker {

    Long idOld = null;
    String username;
    String usernameEncoded;
    String email;
    String consultantId;
    String postcode;
    Long agencyId;
    String password;
  }

  @Getter
  @Setter
  private class ImportRecordAskerWithoutSession {

    Long idOld = null;
    String username;
    String usernameEncoded;
    String email;
    Long agencyId;
    String password;
  }
}

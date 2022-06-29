package de.caritas.cob.userservice.api.adapters.rocketchat;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.RocketChatUnauthorizedException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteUserException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupsListAllException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetUserIdException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.StandardResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupAddUserBodyDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupCleanHistoryDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupCreateBodyDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDeleteBodyDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDeleteResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupRemoveUserBodyDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupsListAllResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LdapLoginDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.logout.LogoutResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.SetRoomReadOnlyBodyDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserDeleteBodyDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UsersListReponseDTO;
import de.caritas.cob.userservice.api.service.LogService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service for Rocket.Chat functionalities.
 */
@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class RocketChatService {

  private static final String ERROR_MESSAGE = "Error during rollback: Rocket.Chat group with id "
      + "%s could not be deleted";
  private static final String RC_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  private static final String CHAT_ROOM_ERROR_MESSAGE = "Could not get Rocket.Chat rooms for user id %s";
  private static final String GROUPS_LIST_ALL_ERROR_MESSAGE = "Could not get all rocket chat groups";
  private static final String USERS_LIST_ERROR_MESSAGE = "Could not get users list from Rocket.Chat";
  private static final String USER_LIST_GET_FIELD_SELECTION = "{\"_id\":1}";
  private final LocalDateTime localDateTime1900 = LocalDateTime.of(1900, 1, 1, 0, 0);


  private final LocalDateTime localDateTimeFuture = nowInUtc().plusYears(1L);
  private final @NonNull RestTemplate restTemplate;
  private final @NonNull RocketChatCredentialsProvider rcCredentialHelper;
  @Value("${rocket.chat.header.auth.token}")
  private String rocketChatHeaderAuthToken;
  @Value("${rocket.chat.header.user.id}")
  private String rocketChatHeaderUserId;
  @Value("${rocket.chat.api.group.create.url}")
  private String rocketChatApiGroupCreateUrl;
  @Value("${rocket.chat.api.group.delete.url}")
  private String rocketChatApiGroupDeleteUrl;
  @Value("${rocket.chat.api.group.add.user}")
  private String rocketChatApiGroupAddUserUrl;
  @Value("${rocket.chat.api.group.remove.user}")
  private String rocketChatApiGroupRemoveUserUrl;
  @Value("${rocket.chat.api.group.get.member}")
  private String rocketChatApiGetGroupMembersUrl;
  @Value("${rocket.chat.api.group.list.all}")
  private String rocketChatApiGetGroupsListAll;
  @Value("${rocket.chat.api.subscriptions.get}")
  private String rocketChatApiSubscriptionsGet;
  @Value("${rocket.chat.api.rooms.get}")
  private String rocketChatApiRoomsGet;
  @Value("${rocket.chat.api.user.login}")
  private String rocketChatApiUserLogin;
  @Value("${rocket.chat.api.user.logout}")
  private String rocketChatApiUserLogout;
  @Value("${rocket.chat.api.user.info}")
  private String rocketChatApiUserInfo;
  @Value("${rocket.chat.api.user.update}")
  private String rocketChatApiUserUpdate;
  @Value("${rocket.chat.api.user.delete}")
  private String rocketChatApiUserDelete;
  @Value("${rocket.chat.api.user.list}")
  private String rocketChatApiUsersListGet;
  @Value("${rocket.chat.api.rooms.clean.history}")
  private String rocketChatApiCleanRoomHistory;
  @Value("${rocket.chat.api.group.set.readOnly}")
  private String rocketChatApiGroupSetReadOnly;

  private boolean rotatingTokensInitialized = false;

  @PostConstruct
  @Scheduled(cron = "${rocket.credentialscheduler.cron}")
  @Profile("!testing")
  public void updateCredentials() {
    if (rotatingTokensInitialized) {
      log.debug("rotating tokens");
    } else {
      log.debug("initialize tokens");
      rotatingTokensInitialized = true;
    }

    try {
      rcCredentialHelper.updateCredentials();
    } catch (RocketChatLoginException e) {
      log.warn("Unauthorized: {}", e.getMessage());
    }
  }

  /**
   * Creation of a private Rocket.Chat group.
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return the group id
   */
  public Optional<GroupResponseDTO> createPrivateGroup(String name,
      RocketChatCredentials rocketChatCredentials) throws RocketChatCreateGroupException {

    GroupResponseDTO response;

    try {

      var headers = getStandardHttpHeaders(rocketChatCredentials);
      var groupCreateBodyDto = new GroupCreateBodyDTO(name, false);
      HttpEntity<GroupCreateBodyDTO> request =
          new HttpEntity<>(groupCreateBodyDto, headers);
      response =
          restTemplate.postForObject(rocketChatApiGroupCreateUrl, request, GroupResponseDTO.class);

    } catch (RestClientResponseException ex) {
      throw new RocketChatCreateGroupException(ex);
    }

    if (!isCreateGroupResponseSuccess(response)) {
      throw new RocketChatCreateGroupException(
          String.format("Rocket.Chat group with name %s could not be created", name));
    }

    return Optional.ofNullable(response);

  }

  private boolean isCreateGroupResponseSuccess(GroupResponseDTO response) {
    return nonNull(response) && response.isSuccess() && isGroupIdAvailable(response);
  }

  /**
   * Creates a private Rocket.Chat group with the system user (credentials).
   *
   * @param groupName the Rocket.Chat group name
   * @return an {@link Optional} of a {@link GroupResponseDTO}
   * @throws RocketChatCreateGroupException on failure
   */
  public Optional<GroupResponseDTO> createPrivateGroupWithSystemUser(String groupName)
      throws RocketChatCreateGroupException {

    try {
      RocketChatCredentials systemUserCredentials = rcCredentialHelper.getSystemUser();
      return this.createPrivateGroup(groupName, systemUserCredentials);
    } catch (RocketChatUserNotInitializedException e) {
      throw new RocketChatCreateGroupException(e);
    }
  }

  /**
   * Deletion of a Rocket.Chat group as system user.
   *
   * @param groupId the Rocket.Chat group id
   * @return true, if successfully
   */
  public boolean deleteGroupAsSystemUser(String groupId) {
    try {
      RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
      return rollbackGroup(groupId, systemUser);
    } catch (RocketChatUserNotInitializedException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logRocketChatError);
    }
  }

  /**
   * Deletion of a Rocket.Chat group as technical user.
   *
   * @param groupId the Rocket.Chat group id
   * @throws RocketChatDeleteGroupException when deletion of group fails
   */
  public void deleteGroupAsTechnicalUser(String groupId) throws RocketChatDeleteGroupException {
    try {
      this.addTechnicalUserToGroup(groupId);
      RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
      rollbackGroup(groupId, technicalUser);
    } catch (Exception e) {
      throw new RocketChatDeleteGroupException(e);
    }
  }

  /**
   * Deletion of a Rocket.Chat group.
   *
   * @param groupId               the group id
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return true, if successfully
   */
  public boolean rollbackGroup(String groupId, RocketChatCredentials rocketChatCredentials) {

    GroupDeleteResponseDTO response = null;

    try {

      var headers = getStandardHttpHeaders(rocketChatCredentials);
      var groupDeleteBodyDto = new GroupDeleteBodyDTO(groupId);
      HttpEntity<GroupDeleteBodyDTO> request =
          new HttpEntity<>(groupDeleteBodyDto, headers);
      response = restTemplate.postForObject(rocketChatApiGroupDeleteUrl, request,
          GroupDeleteResponseDTO.class);

    } catch (Exception ex) {
      log.error(
          "Rocket.Chat Error: Error during rollback: Rocket.Chat group with id {} could not be "
              + "deleted", groupId, ex
      );
    }

    if (response != null && response.isSuccess()) {
      return true;
    } else {
      log.error(
          "Rocket.Chat Error: Error during rollback: Rocket.Chat group with id {} could not be "
              + "deleted (Error: unknown / ErrorType: unknown)", groupId
      );

      return false;
    }

  }

  private boolean isGroupIdAvailable(GroupResponseDTO response) {
    return nonNull(response.getGroup()) && nonNull(response.getGroup().getId());
  }

  private HttpHeaders getStandardHttpHeaders(RocketChatCredentials rocketChatCredentials) {

    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add(rocketChatHeaderAuthToken, rocketChatCredentials.getRocketChatToken());
    httpHeaders.add(rocketChatHeaderUserId, rocketChatCredentials.getRocketChatUserId());
    return httpHeaders;
  }

  /**
   * Retrieves the userId for the given credentials.
   *
   * @param username   the username
   * @param password   the password
   * @param firstLogin true, if first login in Rocket.Chat. This requires a special API call.
   * @return the userid
   * @throws RocketChatLoginException on failure
   */
  public String getUserID(String username, String password, boolean firstLogin)
      throws RocketChatLoginException {

    ResponseEntity<LoginResponseDTO> response;

    if (firstLogin) {
      response = loginUserFirstTime(username, password);
    } else {
      response = this.rcCredentialHelper.loginUser(username, password);
    }

    var rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatUserId(response.getBody().getData().getUserId())
        .rocketChatToken(response.getBody().getData().getAuthToken()).build();

    logoutUser(rocketChatCredentials);

    return rocketChatCredentials.getRocketChatUserId();
  }

  /**
   * Initial login to synchronize ldap and Rocket.Chat user.
   *
   * @param username the username
   * @param password the password
   * @return the login result
   * @throws RocketChatLoginException on failure
   */
  public ResponseEntity<LoginResponseDTO> loginUserFirstTime(String username, String password)
      throws RocketChatLoginException {

    try {
      var headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      var ldapLoginDTO = new LdapLoginDTO();
      ldapLoginDTO.setLdap(true);
      ldapLoginDTO.setUsername(username);
      ldapLoginDTO.setLdapPass(password);

      HttpEntity<LdapLoginDTO> request = new HttpEntity<>(ldapLoginDTO, headers);

      return restTemplate.postForEntity(rocketChatApiUserLogin, request, LoginResponseDTO.class);
    } catch (Exception ex) {
      throw new RocketChatLoginException(
          String.format("Could not login user (%s) in Rocket.Chat for the first time", username));
    }
  }

  /**
   * Performs a logout with the given credentials and returns true on success.
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return true if logout was successful
   */
  public boolean logoutUser(RocketChatCredentials rocketChatCredentials) {

    try {
      var headers = getStandardHttpHeaders(rocketChatCredentials);

      HttpEntity<Void> request = new HttpEntity<>(headers);

      ResponseEntity<LogoutResponseDTO> response =
          restTemplate.postForEntity(rocketChatApiUserLogout, request, LogoutResponseDTO.class);

      return response.getStatusCode() == HttpStatus.OK;

    } catch (Exception ex) {
      log.error(
          "Rocket.Chat Error: Could not log out user id ({}) from Rocket.Chat",
          rocketChatCredentials.getRocketChatUserId(), ex
      );

      return false;
    }
  }

  /**
   * Adds the provided user to the Rocket.Chat group with given groupId.
   *
   * @param rcUserId  Rocket.Chat userId
   * @param rcGroupId Rocket.Chat roomId
   */
  public void addUserToGroup(String rcUserId, String rcGroupId)
      throws RocketChatAddUserToGroupException {

    GroupResponseDTO response;
    try {
      RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
      var header = getStandardHttpHeaders(technicalUser);
      var body = new GroupAddUserBodyDTO(rcUserId, rcGroupId);
      HttpEntity<GroupAddUserBodyDTO> request = new HttpEntity<>(body, header);

      response =
          restTemplate.postForObject(rocketChatApiGroupAddUserUrl, request, GroupResponseDTO.class);

    } catch (Exception ex) {
      throw new RocketChatAddUserToGroupException(String.format(
          "Could not add user %s to Rocket.Chat group with id %s", rcUserId, rcGroupId));
    }

    if (nonNull(response) && !response.isSuccess()) {
      var error = "Could not add user %s to Rocket.Chat group with id %s";
      throw new RocketChatAddUserToGroupException(String.format(error, rcUserId, rcGroupId));
    }
  }

  /**
   * Adds the technical user to the given Rocket.Chat group id.
   *
   * @param rcGroupId the rocket chat group id
   */
  public void addTechnicalUserToGroup(String rcGroupId)
      throws RocketChatAddUserToGroupException, RocketChatUserNotInitializedException {
    this.addUserToGroup(rcCredentialHelper.getTechnicalUser().getRocketChatUserId(), rcGroupId);
  }

  /**
   * Removes the provided user from the Rocket.Chat group with given groupId.
   *
   * @param rcUserId  Rocket.Chat userId
   * @param rcGroupId Rocket.Chat roomId
   * @throws RocketChatRemoveUserFromGroupException on failure
   */
  public void removeUserFromGroup(String rcUserId, String rcGroupId)
      throws RocketChatRemoveUserFromGroupException {

    GroupResponseDTO response;
    try {
      RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
      var header = getStandardHttpHeaders(technicalUser);
      var body = new GroupRemoveUserBodyDTO(rcUserId, rcGroupId);
      HttpEntity<GroupRemoveUserBodyDTO> request =
          new HttpEntity<>(body, header);

      response = restTemplate.postForObject(rocketChatApiGroupRemoveUserUrl, request,
          GroupResponseDTO.class);

    } catch (Exception ex) {
      throw new RocketChatRemoveUserFromGroupException(String.format(
          "Could not remove user %s from Rocket.Chat group with id %s", rcUserId, rcGroupId));
    }

    if (response != null && !response.isSuccess()) {
      var error = "Could not remove user %s from Rocket.Chat group with id %s";
      throw new RocketChatRemoveUserFromGroupException(String.format(error, rcUserId, rcGroupId));
    }
  }

  /**
   * Removes the technical user from the given Rocket.Chat group id.
   *
   * @param rcGroupId the rocket chat group id
   */
  public void removeTechnicalUserFromGroup(String rcGroupId)
      throws RocketChatRemoveUserFromGroupException, RocketChatUserNotInitializedException {
    this.removeUserFromGroup(rcCredentialHelper.getTechnicalUser().getRocketChatUserId(),
        rcGroupId);
  }

  /**
   * Get all standard members (all users except system user and technical user) of a rocket chat
   * group.
   *
   * @param rcGroupId the rocket chat group id
   * @return all standard members of that group
   */
  public List<GroupMemberDTO> getStandardMembersOfGroup(String rcGroupId)
      throws RocketChatGetGroupMembersException, RocketChatUserNotInitializedException {

    List<GroupMemberDTO> groupMemberList =
        new ArrayList<>(getMembersOfGroup(rcGroupId));

    if (groupMemberList.isEmpty()) {
      throw new RocketChatGetGroupMembersException(
          String.format("Group member list from group with id %s is empty", rcGroupId));
    }

    Iterator<GroupMemberDTO> groupMemberListIterator = groupMemberList.iterator();
    while (groupMemberListIterator.hasNext()) {
      var groupMemberDTO = groupMemberListIterator.next();
      if (groupMemberDTO.get_id()
          .equals(rcCredentialHelper.getTechnicalUser().getRocketChatUserId())
          || groupMemberDTO.get_id()
          .equals(rcCredentialHelper.getSystemUser().getRocketChatUserId())) {
        groupMemberListIterator.remove();
      }
    }

    return groupMemberList;

  }

  /**
   * Removes all users from the given group except system user and technical user.
   *
   * @param rcGroupId the rocket chat group id
   */
  public void removeAllStandardUsersFromGroup(String rcGroupId)
      throws RocketChatGetGroupMembersException, RocketChatRemoveUserFromGroupException, RocketChatUserNotInitializedException {
    List<GroupMemberDTO> groupMemberList = getMembersOfGroup(rcGroupId);

    if (groupMemberList.isEmpty()) {
      throw new RocketChatGetGroupMembersException(
          String.format("Group member list from group with id %s is empty", rcGroupId));
    }

    for (GroupMemberDTO member : groupMemberList) {
      if (!member.get_id().equals(rcCredentialHelper.getTechnicalUser().getRocketChatUserId())
          && !member.get_id().equals(rcCredentialHelper.getSystemUser().getRocketChatUserId())) {
        removeUserFromGroup(member.get_id(), rcGroupId);
      }
    }
  }

  /**
   * Returns the group/room members of the given Rocket.Chat group id.
   *
   * @param rcGroupId the rocket chat id
   * @return al members of the group
   */
  public List<GroupMemberDTO> getMembersOfGroup(String rcGroupId)
      throws RocketChatGetGroupMembersException {

    ResponseEntity<GroupMemberResponseDTO> response;
    try {
      RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
      var header = getStandardHttpHeaders(systemUser);
      HttpEntity<GroupAddUserBodyDTO> request = new HttpEntity<>(header);

      response = restTemplate.exchange(buildGetGroupMembersPath(rcGroupId),
          HttpMethod.GET, request, GroupMemberResponseDTO.class);

    } catch (Exception ex) {
      throw new RocketChatGetGroupMembersException(String.format("Could not get Rocket.Chat group"
          + " members for room id %s", rcGroupId), ex);
    }

    if (response.getStatusCode() == HttpStatus.OK && nonNull(response.getBody())) {
      return Arrays.asList(response.getBody().getMembers());
    } else {
      var error = "Could not get Rocket.Chat group members for room id %s";
      throw new RocketChatGetGroupMembersException(String.format(error, rcGroupId));
    }
  }

  private String buildGetGroupMembersPath(String rcGroupId) {
    return UriComponentsBuilder
        .fromUriString(rocketChatApiGetGroupMembersUrl)
        .queryParam("roomId", rcGroupId)
        .queryParam("count", 0)
        .build().encode().toUriString();
  }

  /**
   * Removes all messages from the specified Rocket.Chat group written by the technical user from
   * the last 24 hours (avoiding time zone failures).
   *
   * @param rcGroupId the rocket chat group id
   * @param oldest    the oldest message time
   * @param latest    the latest message time
   */
  public void removeSystemMessages(String rcGroupId, LocalDateTime oldest, LocalDateTime latest)
      throws RocketChatRemoveSystemMessagesException, RocketChatUserNotInitializedException {
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    this.removeMessages(rcGroupId, new String[]{technicalUser.getRocketChatUsername()}, oldest,
        latest);
  }

  /**
   * Removes all messages (from every user) from a Rocket.Chat group.
   *
   * @param rcGroupId the rocket chat group id
   */
  public void removeAllMessages(String rcGroupId)
      throws RocketChatRemoveSystemMessagesException {
    removeMessages(rcGroupId, null, localDateTime1900, localDateTimeFuture);
  }

  private void removeMessages(String rcGroupId, String[] users, LocalDateTime oldest,
      LocalDateTime latest) throws RocketChatRemoveSystemMessagesException {

    StandardResponseDTO response;
    try {
      RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
      var header = getStandardHttpHeaders(technicalUser);
      var body = new GroupCleanHistoryDTO(rcGroupId,
          oldest.format(DateTimeFormatter.ofPattern(RC_DATE_TIME_PATTERN)),
          latest.format(DateTimeFormatter.ofPattern(RC_DATE_TIME_PATTERN)),
          (isNotEmpty(users)) ? users : new String[]{});
      HttpEntity<GroupCleanHistoryDTO> request = new HttpEntity<>(body, header);

      response = restTemplate.postForObject(rocketChatApiCleanRoomHistory, request,
          StandardResponseDTO.class);

    } catch (Exception ex) {
      throw new RocketChatRemoveSystemMessagesException(
          String.format("Could not clean history of Rocket.Chat group id %s", rcGroupId));
    }

    if (nonNull(response) && !response.isSuccess()) {
      throw new RocketChatRemoveSystemMessagesException(
          String.format("Could not clean history of Rocket.Chat group id %s", rcGroupId));
    }
  }

  /**
   * Returns the subscriptions for the given user id.
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return the subscriptions of the user
   */
  public List<SubscriptionsUpdateDTO> getSubscriptionsOfUser(
      RocketChatCredentials rocketChatCredentials) {

    ResponseEntity<SubscriptionsGetDTO> response;

    try {
      var header = getStandardHttpHeaders(rocketChatCredentials);
      HttpEntity<Void> request = new HttpEntity<>(header);

      response = restTemplate.exchange(rocketChatApiSubscriptionsGet, HttpMethod.GET, request,
          SubscriptionsGetDTO.class);

    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
        throw new RocketChatUnauthorizedException(rocketChatCredentials.getRocketChatUserId(), ex);
      }
      throw new InternalServerErrorException(ex.getMessage(), LogService::logRocketChatError);
    }

    if (response.getStatusCode() == HttpStatus.OK && nonNull(response.getBody())) {
      return Arrays.asList(response.getBody().getUpdate());
    } else {
      var error = "Could not get Rocket.Chat subscriptions for user id %s";
      throw new InternalServerErrorException(error, LogService::logRocketChatError);
    }
  }

  /**
   * Returns the rooms for the given user id.
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return the rooms for the user
   */
  public List<RoomsUpdateDTO> getRoomsOfUser(RocketChatCredentials rocketChatCredentials) {

    ResponseEntity<RoomsGetDTO> response;

    try {
      var header = getStandardHttpHeaders(rocketChatCredentials);
      HttpEntity<Void> request = new HttpEntity<>(header);
      response =
          restTemplate.exchange(rocketChatApiRoomsGet, HttpMethod.GET, request, RoomsGetDTO.class);

    } catch (Exception ex) {
      throw new InternalServerErrorException(String.format(
          CHAT_ROOM_ERROR_MESSAGE, rocketChatCredentials.getRocketChatUserId()), ex,
          LogService::logRocketChatError);
    }

    if (response.getStatusCode() == HttpStatus.OK && nonNull(response.getBody())) {
      return Arrays.asList(response.getBody().getUpdate());
    } else {
      var error = String.format(CHAT_ROOM_ERROR_MESSAGE,
          rocketChatCredentials.getRocketChatUserId());
      throw new InternalServerErrorException(error, LogService::logRocketChatError);
    }
  }

  /**
   * Returns the information of the given Rocket.Chat user.
   *
   * @param rcUserId Rocket.Chat user id
   * @return the dto containing the user infos
   */
  public UserInfoResponseDTO getUserInfo(String rcUserId) {

    ResponseEntity<UserInfoResponseDTO> response;
    try {
      RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
      var header = getStandardHttpHeaders(technicalUser);
      HttpEntity<Void> request = new HttpEntity<>(header);

      var fields = "{\"userRooms\":1}";
      String url = rocketChatApiUserInfo + "?userId=" + rcUserId + "&fields={fields}";
      response = restTemplate
          .exchange(url, HttpMethod.GET, request, UserInfoResponseDTO.class, fields);

    } catch (RestClientResponseException | RocketChatUserNotInitializedException ex) {
      throw new InternalServerErrorException(
          String.format("Could not get Rocket.Chat user info of user id %s", rcUserId), ex,
          LogService::logRocketChatError);

    }

    if (isResponseNotSuccess(response)) {
      throw new InternalServerErrorException(
          String.format(
              "Could not get Rocket.Chat user info of user id %s.%n Status: %s.%n error: %s.%n error type: %s",
              rcUserId, response.getStatusCodeValue(), response.getBody().getError(),
              response.getBody().getErrorType()),
          LogService::logRocketChatError);
    }

    return response.getBody();
  }

  private boolean isResponseNotSuccess(ResponseEntity<UserInfoResponseDTO> response) {
    UserInfoResponseDTO responseBody = response.getBody();
    return isNull(responseBody) || response.getStatusCode() != HttpStatus.OK || !responseBody
        .isSuccess();
  }

  /**
   * Updates the user data of the given Rocket.Chat user.
   *
   * @param requestDTO the input dto
   * @return the dto containing the user infos
   */
  public UserInfoResponseDTO updateUser(UserUpdateRequestDTO requestDTO) {
    try {
      return updateUserData(requestDTO).getBody();
    } catch (RestClientResponseException | RocketChatUserNotInitializedException ex) {
      throw new InternalServerErrorException(
          String.format("Could not update Rocket.Chat user of user id %s", requestDTO.getUserId()),
          ex, LogService::logRocketChatError);
    }
  }

  private ResponseEntity<UserInfoResponseDTO> updateUserData(UserUpdateRequestDTO requestDTO)
      throws RocketChatUserNotInitializedException {
    HttpEntity<UserUpdateRequestDTO> request = buildRocketChatUserUpdateRequestEntity(requestDTO);

    ResponseEntity<UserInfoResponseDTO> response = restTemplate
        .exchange(rocketChatApiUserUpdate, HttpMethod.POST, request, UserInfoResponseDTO.class);

    if (isResponseNotSuccess(response)) {
      throw new InternalServerErrorException(
          String.format(
              "Could not get Rocket.Chat user info of user id %s.%n Status: %s.%n error: %s.%n error type: %s",
              requestDTO.getUserId(), response.getStatusCodeValue(), response.getBody().getError(),
              response.getBody().getErrorType()),
          LogService::logRocketChatError);
    }

    return response;
  }

  private HttpEntity<UserUpdateRequestDTO> buildRocketChatUserUpdateRequestEntity(
      UserUpdateRequestDTO requestDTO) throws RocketChatUserNotInitializedException {
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    var header = getStandardHttpHeaders(technicalUser);
    return new HttpEntity<>(requestDTO, header);
  }

  /**
   * Deletes the user data of the given Rocket.Chat user.
   *
   * @param rcUserId Rocket.Chat user id
   * @throws RocketChatDeleteUserException when deletion of user fails
   */
  public void deleteUser(String rcUserId) throws RocketChatDeleteUserException {
    try {
      deleteUserData(rcUserId);
    } catch (Exception e) {
      throw new RocketChatDeleteUserException(e);
    }
  }

  private void deleteUserData(String rcUserId) throws RocketChatUserNotInitializedException {

    var requestDTO = new UserDeleteBodyDTO(rcUserId);
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    var header = getStandardHttpHeaders(technicalUser);
    HttpEntity<UserDeleteBodyDTO> request = new HttpEntity<>(requestDTO, header);

    ResponseEntity<UserInfoResponseDTO> response = restTemplate
        .exchange(rocketChatApiUserDelete, HttpMethod.POST, request, UserInfoResponseDTO.class);

    if (isResponseNotSuccess(response)) {
      throw new InternalServerErrorException(
          String.format(
              "Could not delete Rocket.Chat user with user id %s.%n Status: %s.%n error: %s.%n "
                  + "error type: %s",
              rcUserId, response.getStatusCodeValue(), response.getBody().getError(),
              response.getBody().getErrorType()), LogService::logRocketChatError);
    }
  }

  /**
   * Sets the Rocket.Chat room with the given id read only.
   *
   * @param rcRoomId the Rocket.Chat room id
   * @throws RocketChatUserNotInitializedException if technical user isn´t initialized
   */
  public void setRoomReadOnly(String rcRoomId) throws RocketChatUserNotInitializedException {
    setRoomState(rcRoomId, true);
  }

  /**
   * Sets the Rocket.Chat room with the given id writeable.
   *
   * @param rcRoomId the Rocket.Chat room id
   * @throws RocketChatUserNotInitializedException if technical user isn´t initialized
   */
  public void setRoomWriteable(String rcRoomId) throws RocketChatUserNotInitializedException {
    setRoomState(rcRoomId, false);
  }

  private void setRoomState(String rcRoomId, boolean readOnly)
      throws RocketChatUserNotInitializedException {
    var requestDTO = new SetRoomReadOnlyBodyDTO(rcRoomId, readOnly);
    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
    var header = getStandardHttpHeaders(systemUser);
    HttpEntity<SetRoomReadOnlyBodyDTO> request = new HttpEntity<>(requestDTO, header);

    ResponseEntity<GroupResponseDTO> response = restTemplate
        .exchange(rocketChatApiGroupSetReadOnly, HttpMethod.POST, request, GroupResponseDTO.class);

    GroupResponseDTO responseBody = response.getBody();
    if (nonNull(responseBody) && !responseBody.isSuccess()) {
      log.error(
          "Rocket.Chat Error: Mark group with id {} as read only failed with reason {}",
          rcRoomId, responseBody.getError()
      );
    }
  }

  /**
   * Returns all private Rocket.Chat groups which are inactive (= no messages written) since given
   * date.
   *
   * @param dateTimeSinceInactive the date and time since when the groups should be inactive
   * @return a {@link List} of {@link GroupDTO} instances
   */
  public List<GroupDTO> fetchAllInactivePrivateGroupsSinceGivenDate(
      LocalDateTime dateTimeSinceInactive)
      throws RocketChatGetGroupsListAllException {

    final var GROUP_RESPONSE_LAST_MESSAGE_TIMESTAMP_FIELD = "lastMessage.ts";
    final var GROUP_RESPONSE_GROUP_TYPE_FIELD = "t";
    final var GROUP_RESPONSE_GROUP_TYPE_PRIVATE = "p";

    DBObject mongoDbQuery = QueryBuilder
        .start(GROUP_RESPONSE_LAST_MESSAGE_TIMESTAMP_FIELD)
        .lessThan(QueryBuilder
            .start("$date")
            .is(dateTimeSinceInactive.format(DateTimeFormatter.ofPattern(RC_DATE_TIME_PATTERN)))
            .get())
        .and(QueryBuilder
            .start(GROUP_RESPONSE_GROUP_TYPE_FIELD)
            .is(GROUP_RESPONSE_GROUP_TYPE_PRIVATE)
            .get())
        .get();

    return getGroupsListAll(mongoDbQuery);
  }

  /**
   * Returns a list of all Rocket.Chat groups.
   *
   * @param mongoDbQuery mongoDB Query as {@link DBObject} created with {@link QueryBuilder}
   * @return a {@link List} of {@link GroupDTO} instances
   * @throws RocketChatGetGroupsListAllException when request fails
   */
  private List<GroupDTO> getGroupsListAll(DBObject mongoDbQuery)
      throws RocketChatGetGroupsListAllException {

    ResponseEntity<GroupsListAllResponseDTO> response;
    try {
      var technicalUser = rcCredentialHelper.getTechnicalUser();
      var header = getStandardHttpHeaders(technicalUser);
      HttpEntity<GroupAddUserBodyDTO> request = new HttpEntity<>(header);
      var url = rocketChatApiGetGroupsListAll + "?query={query}";
      response = restTemplate.exchange(url,
          HttpMethod.GET,
          request,
          GroupsListAllResponseDTO.class,
          mongoDbQuery.toString());
    } catch (Exception ex) {
      throw new RocketChatGetGroupsListAllException(GROUPS_LIST_ALL_ERROR_MESSAGE, ex);
    }

    if (response.getStatusCode() == HttpStatus.OK && nonNull(response.getBody())) {
      return Arrays.asList(response.getBody().getGroups());
    } else {
      throw new RocketChatGetGroupsListAllException(GROUPS_LIST_ALL_ERROR_MESSAGE);
    }
  }

  /**
   * Returns the id of a Rocket.Chat user by username.
   *
   * @param username the username to search for
   * @return the Rocket.Chat user id
   * @throws RocketChatGetUserIdException when request fails
   */
  public String getRocketChatUserIdByUsername(String username)
      throws RocketChatGetUserIdException {

    ResponseEntity<UsersListReponseDTO> response;
    try {
      var technicalUser = rcCredentialHelper.getTechnicalUser();
      var header = getStandardHttpHeaders(technicalUser);
      HttpEntity<UsersListReponseDTO> request = new HttpEntity<>(header);
      response = restTemplate.exchange(buildUsersListGetUrl(),
          HttpMethod.GET,
          request,
          UsersListReponseDTO.class,
          buildUsernameQuery(username),
          USER_LIST_GET_FIELD_SELECTION);
    } catch (Exception ex) {
      throw new RocketChatGetUserIdException(USERS_LIST_ERROR_MESSAGE, ex);
    }

    if (response.getStatusCode() == HttpStatus.OK && nonNull(response.getBody())) {
      return extractUserIdFromResponse(response.getBody().getUsers());
    } else {
      throw new RocketChatGetUserIdException(USERS_LIST_ERROR_MESSAGE);
    }
  }

  private String extractUserIdFromResponse(RocketChatUserDTO[] users)
      throws RocketChatGetUserIdException {

    if (users.length == 1) {
      return users[0].getId();
    }

    throw new RocketChatGetUserIdException(
        String.format("Found %s users by username", users.length));

  }

  private String buildUsersListGetUrl() {
    return rocketChatApiUsersListGet + "?query={query}&fields={fields}";
  }

  private String buildUsernameQuery(String username) {
    return String.format("{\"username\":{\"$eq\":\"%s\"}}", username.toLowerCase());
  }
}

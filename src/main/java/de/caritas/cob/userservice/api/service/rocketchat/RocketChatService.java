package de.caritas.cob.userservice.api.service.rocketchat;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.UnauthorizedException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteUserException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.rocketchat.StandardResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupAddUserBodyDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupCleanHistoryDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupCreateBodyDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDeleteBodyDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDeleteResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupRemoveUserBodyDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LdapLoginDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.logout.LogoutResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketchat.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.model.rocketchat.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserDeleteBodyDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserUpdateDataDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.service.LogService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for Rocket.Chat functionalities.
 */
@Getter
@Service
@RequiredArgsConstructor
public class RocketChatService {

  private static final String ERROR_MESSAGE = "Error during rollback: Rocket.Chat group with id "
      + "%s could not be deleted";
  private static final String RC_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  private static final String CHAT_ROOM_ERROR_MESSAGE = "Could not get Rocket.Chat rooms for user id %s";
  private final LocalDateTime localDateTime1900 = LocalDateTime.of(1900, 1, 1, 0, 0);

  private final LocalDateTime localDateTimeFuture = nowInUtc().plusYears(1L);

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
  @Value("${rocket.chat.api.rooms.clean.history}")
  private String rocketChatApiCleanRoomHistory;

  private final @NonNull RestTemplate restTemplate;
  private final @NonNull RocketChatCredentialsProvider rcCredentialHelper;

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

      HttpHeaders headers = getStandardHttpHeaders(rocketChatCredentials);
      GroupCreateBodyDTO groupCreateBodyDto = new GroupCreateBodyDTO(name, false);
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

      HttpHeaders headers = getStandardHttpHeaders(rocketChatCredentials);
      GroupDeleteBodyDTO groupDeleteBodyDto = new GroupDeleteBodyDTO(groupId);
      HttpEntity<GroupDeleteBodyDTO> request =
          new HttpEntity<>(groupDeleteBodyDto, headers);
      response = restTemplate.postForObject(rocketChatApiGroupDeleteUrl, request,
          GroupDeleteResponseDTO.class);

    } catch (Exception ex) {
      LogService.logRocketChatError(String.format(ERROR_MESSAGE, groupId), ex);
    }

    if (response != null && response.isSuccess()) {
      return true;
    } else {
      LogService.logRocketChatError(String.format(ERROR_MESSAGE, groupId), "unknown", "unknown");
      return false;
    }

  }

  private boolean isGroupIdAvailable(GroupResponseDTO response) {
    return nonNull(response.getGroup()) && nonNull(response.getGroup().getId());
  }

  private HttpHeaders getStandardHttpHeaders(RocketChatCredentials rocketChatCredentials) {

    HttpHeaders httpHeaders = new HttpHeaders();
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

    RocketChatCredentials rocketChatCredentials =
        RocketChatCredentials.builder().rocketChatUserId(response.getBody().getData().getUserId())
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
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      LdapLoginDTO ldapLoginDTO = new LdapLoginDTO();
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
      HttpHeaders headers = getStandardHttpHeaders(rocketChatCredentials);

      HttpEntity<Void> request = new HttpEntity<>(headers);

      ResponseEntity<LogoutResponseDTO> response =
          restTemplate.postForEntity(rocketChatApiUserLogout, request, LogoutResponseDTO.class);

      return response.getStatusCode() == HttpStatus.OK;

    } catch (Exception ex) {
      LogService.logRocketChatError(String.format("Could not log out user id (%s) from Rocket.Chat",
          rocketChatCredentials.getRocketChatUserId()), ex);

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
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      GroupAddUserBodyDTO body = new GroupAddUserBodyDTO(rcUserId, rcGroupId);
      HttpEntity<GroupAddUserBodyDTO> request = new HttpEntity<>(body, header);

      response =
          restTemplate.postForObject(rocketChatApiGroupAddUserUrl, request, GroupResponseDTO.class);

    } catch (Exception ex) {
      throw new RocketChatAddUserToGroupException(String.format(
          "Could not add user %s to Rocket.Chat group with id %s", rcUserId, rcGroupId));
    }

    if (nonNull(response) && !response.isSuccess()) {
      String error = "Could not add user %s to Rocket.Chat group with id %s";
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
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      GroupRemoveUserBodyDTO body = new GroupRemoveUserBodyDTO(rcUserId, rcGroupId);
      HttpEntity<GroupRemoveUserBodyDTO> request =
          new HttpEntity<>(body, header);

      response = restTemplate.postForObject(rocketChatApiGroupRemoveUserUrl, request,
          GroupResponseDTO.class);

    } catch (Exception ex) {
      throw new RocketChatRemoveUserFromGroupException(String.format(
          "Could not remove user %s from Rocket.Chat group with id %s", rcUserId, rcGroupId));
    }

    if (response != null && !response.isSuccess()) {
      String error = "Could not remove user %s from Rocket.Chat group with id %s";
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
      GroupMemberDTO groupMemberDTO = groupMemberListIterator.next();
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
      HttpHeaders header = getStandardHttpHeaders(systemUser);
      HttpEntity<GroupAddUserBodyDTO> request = new HttpEntity<>(header);

      response = restTemplate.exchange(rocketChatApiGetGroupMembersUrl + "?roomId=" + rcGroupId,
          HttpMethod.GET, request, GroupMemberResponseDTO.class);

    } catch (Exception ex) {
      throw new RocketChatGetGroupMembersException(String.format("Could not get Rocket.Chat group"
          + " members for room id %s", rcGroupId), ex);
    }

    if (response.getStatusCode() == HttpStatus.OK && !Objects.isNull(response.getBody())) {
      return Arrays.asList(response.getBody().getMembers());
    } else {
      String error = "Could not get Rocket.Chat group members for room id %s";
      throw new RocketChatGetGroupMembersException(String.format(error, rcGroupId));
    }
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
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      GroupCleanHistoryDTO body = new GroupCleanHistoryDTO(rcGroupId,
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

    ResponseEntity<SubscriptionsGetDTO> response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(rocketChatCredentials);
      HttpEntity<Void> request = new HttpEntity<>(header);

      response = restTemplate.exchange(rocketChatApiSubscriptionsGet, HttpMethod.GET, request,
          SubscriptionsGetDTO.class);

    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
        throw new UnauthorizedException(String.format(
            "Could not get Rocket.Chat subscriptions for user ID %s: Token is not active (401 Unauthorized)",
            rocketChatCredentials.getRocketChatUserId()));
      }
      throw new InternalServerErrorException(ex.getMessage(), LogService::logRocketChatError);
    }

    if (response.getStatusCode() == HttpStatus.OK && nonNull(response.getBody())) {
      return Arrays.asList(response.getBody().getUpdate());
    } else {
      String error = "Could not get Rocket.Chat subscriptions for user id %s";
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
      HttpHeaders header = getStandardHttpHeaders(rocketChatCredentials);
      HttpEntity<Void> request = new HttpEntity<>(header);

      response =
          restTemplate.exchange(rocketChatApiRoomsGet, HttpMethod.GET, request, RoomsGetDTO.class);

    } catch (Exception ex) {
      throw new InternalServerErrorException(String.format(
          CHAT_ROOM_ERROR_MESSAGE, rocketChatCredentials.getRocketChatUserId()),
          LogService::logRocketChatError);
    }

    if (response.getStatusCode() == HttpStatus.OK && nonNull(response.getBody())) {
      return Arrays.asList(response.getBody().getUpdate());
    } else {
      String error = String.format(CHAT_ROOM_ERROR_MESSAGE,
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
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      HttpEntity<Void> request = new HttpEntity<>(header);

      String fields = "{\"userRooms\":1}";
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
    return isNull(response.getBody()) || response.getStatusCode() != HttpStatus.OK
        || !response.getBody().isSuccess();
  }

  /**
   * Updates the user data of the given Rocket.Chat user.
   *
   * @param rcUserId Rocket.Chat user id
   * @return the dto containing the user infos
   */
  public UserInfoResponseDTO updateUser(String rcUserId, UpdateConsultantDTO updateConsultantDTO) {
    try {
      return updateUserData(rcUserId, updateConsultantDTO).getBody();
    } catch (RestClientResponseException | RocketChatUserNotInitializedException ex) {
      throw new InternalServerErrorException(
          String.format("Could not update Rocket.Chat user of user id %s", rcUserId), ex,
          LogService::logRocketChatError);
    }
  }

  private ResponseEntity<UserInfoResponseDTO> updateUserData(String rcUserId,
      UpdateConsultantDTO updateConsultantDTO) throws RocketChatUserNotInitializedException {

    UserUpdateRequestDTO requestDTO = buildUserUpdateRequestDTO(rcUserId, updateConsultantDTO);
    HttpEntity<UserUpdateRequestDTO> request = buildRocketChatUserUpdateRequestEntity(requestDTO);

    ResponseEntity<UserInfoResponseDTO> response = restTemplate
        .exchange(rocketChatApiUserUpdate, HttpMethod.POST, request, UserInfoResponseDTO.class);

    if (isResponseNotSuccess(response)) {
      throw new InternalServerErrorException(
          String.format(
              "Could not get Rocket.Chat user info of user id %s.%n Status: %s.%n error: %s.%n error type: %s",
              rcUserId, response.getStatusCodeValue(), response.getBody().getError(),
              response.getBody().getErrorType()),
          LogService::logRocketChatError);
    }

    return response;
  }

  private UserUpdateRequestDTO buildUserUpdateRequestDTO(String rcUserId,
      UpdateConsultantDTO updateConsultantDTO) {
    UserUpdateDataDTO userUpdateDataDTO = new UserUpdateDataDTO(updateConsultantDTO.getEmail(),
        updateConsultantDTO.getFirstname().concat(" ").concat(updateConsultantDTO.getLastname()));
    return new UserUpdateRequestDTO(rcUserId, userUpdateDataDTO);
  }

  private HttpEntity<UserUpdateRequestDTO> buildRocketChatUserUpdateRequestEntity(
      UserUpdateRequestDTO requestDTO) throws RocketChatUserNotInitializedException {
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    HttpHeaders header = getStandardHttpHeaders(technicalUser);
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

  private void deleteUserData(String rcUserId)
      throws RocketChatUserNotInitializedException {

    UserDeleteBodyDTO requestDTO = new UserDeleteBodyDTO(rcUserId);
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    HttpHeaders header = getStandardHttpHeaders(technicalUser);
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

}

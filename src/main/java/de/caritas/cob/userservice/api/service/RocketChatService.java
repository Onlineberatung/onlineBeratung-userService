package de.caritas.cob.userservice.api.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.UnauthorizedException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGetRoomsException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGetSubscriptionsException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGetUserInfoException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGroupCountersException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatLoginException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatRemoveUserException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.model.rocketChat.StandardResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupAddUserBodyDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupCleanHistoryDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupCounterResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupCreateBodyDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupDeleteBodyDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupDeleteResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupMemberResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupRemoveUserBodyDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.login.LdapLoginDTO;
import de.caritas.cob.userservice.api.model.rocketChat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.logout.LogoutResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsGetDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketChat.subscriptions.SubscriptionsGetDTO;
import de.caritas.cob.userservice.api.model.rocketChat.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketChat.user.UserDeleteBodyDTO;
import de.caritas.cob.userservice.api.model.rocketChat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.service.helper.RocketChatCredentialsHelper;
import lombok.Getter;

/**
 * 
 * Service for Rocket.Chat functionalities
 * 
 */
@Getter
@Service
public class RocketChatService {

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

  @Value("${rocket.chat.api.group.get.counters}")
  private String rocketChatApiGetGroupCounters;

  @Value("${rocket.chat.api.subscriptions.get}")
  private String rocketChatApiSubscriptionsGet;

  @Value("${rocket.chat.api.rooms.get}")
  private String rocketChatApiRoomsGet;

  @Value("${rocket.chat.api.user.login}")
  private String rocketChatApiUserLogin;

  @Value("${rocket.chat.api.user.logout}")
  private String rocketChatApiUserLogout;

  @Value("${rocket.chat.api.user.delete}")
  private String rocketChatApiUserDelete;

  @Value("${rocket.chat.api.user.info}")
  private String rocketChatApiUserInfo;

  @Value("${rocket.chat.api.rooms.clean.history}")
  private String rocketChatApiCleanRoomHistory;

  private String rcDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  private final LocalDateTime localDateTime1900 = LocalDateTime.of(1900, 01, 01, 00, 00);
  private final LocalDateTime localDateTimeFuture = LocalDateTime.now().plusYears(1L);

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private LogService logService;

  @Autowired
  private RocketChatCredentialsHelper rcCredentialHelper;

  /**
   * Creation of a private Rocket.Chat group.
   * 
   * @param name
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return the group id
   */
  public Optional<GroupResponseDTO> createPrivateGroup(String name,
      RocketChatCredentials rocketChatCredentials) {

    GroupResponseDTO response = null;

    try {

      HttpHeaders headers = getStandardHttpHeaders(rocketChatCredentials);
      GroupCreateBodyDTO groupCreateBodyDto = new GroupCreateBodyDTO(name, false);
      HttpEntity<GroupCreateBodyDTO> request =
          new HttpEntity<GroupCreateBodyDTO>(groupCreateBodyDto, headers);
      response =
          restTemplate.postForObject(rocketChatApiGroupCreateUrl, request, GroupResponseDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(
          String.format("Rocket.Chat group with name %s could not be created", name), ex);
      throw new RocketChatCreateGroupException(ex);
    }

    if (response != null && response.isSuccess() && isGroupIdAvailable(response)) {
      return Optional.of(response);
    } else {
      logService.logRocketChatError(
          String.format("Rocket.Chat group with name %s could not be created", name),
          response.getError(), response.getErrorType());
      return Optional.empty();
    }

  }

  /**
   * Creates a private Rocket.Chat group with the system user (credentials)
   * 
   * @param groupName
   * @return
   */
  public Optional<GroupResponseDTO> createPrivateGroupWithSystemUser(String groupName) {

    RocketChatCredentials systemUserCredentials = rcCredentialHelper.getSystemUser();

    return this.createPrivateGroup(groupName, systemUserCredentials);
  }

  /**
   * Deletion of a Rocket.Chat group as system user.
   *
   * @param groupId
   * @return true, if successfully
   */
  public boolean deleteGroupAsSystemUser(String groupId) {
    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();
    return deleteGroup(groupId, systemUser);
  }

  /**
   * Deletion of a Rocket.Chat group.
   * 
   * @param groupId
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return true, if successfully
   */
  public boolean deleteGroup(String groupId, RocketChatCredentials rocketChatCredentials) {

    GroupDeleteResponseDTO response = null;

    try {

      HttpHeaders headers = getStandardHttpHeaders(rocketChatCredentials);
      GroupDeleteBodyDTO groupDeleteBodyDto = new GroupDeleteBodyDTO(groupId);
      HttpEntity<GroupDeleteBodyDTO> request =
          new HttpEntity<GroupDeleteBodyDTO>(groupDeleteBodyDto, headers);
      response = restTemplate.postForObject(rocketChatApiGroupDeleteUrl, request,
          GroupDeleteResponseDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(
          String.format("Rocket.Chat group with id %s could not be deleted", groupId), ex);
      throw new RocketChatDeleteGroupException(ex);
    }

    if (response != null && response.isSuccess()) {
      return true;
    } else {
      logService.logRocketChatError(
          String.format("Rocket.Chat group with id %s could not be deleted", groupId), "unknown",
          "unknown");
      return false;
    }

  }

  /**
   * Returns true if the group id is available in the {@link GroupResponseDTO}
   * 
   * @param response
   * @return true, if group id is available
   */
  private boolean isGroupIdAvailable(GroupResponseDTO response) {
    return response.getGroup() != null && response.getGroup().getId() != null;
  }


  /**
   * Returns a HttpHeaders instance with standard settings (Rocket.Chat-Token, Rocket.Chat-User-ID,
   * MediaType)
   * 
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return a HttpHeaders instance with the standard settings
   */
  private HttpHeaders getStandardHttpHeaders(RocketChatCredentials rocketChatCredentials) {

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
    httpHeaders.add(rocketChatHeaderAuthToken, rocketChatCredentials.getRocketChatToken());
    httpHeaders.add(rocketChatHeaderUserId, rocketChatCredentials.getRocketChatUserId());
    return httpHeaders;
  }

  /**
   * Retrieves the userId for the given credentials
   * 
   * @param username
   * @param password
   * @param firstLogin true, if first login in Rocket.Chat. This requires a special API call.
   * @return
   */
  public String getUserID(String username, String password, boolean firstLogin) {

    ResponseEntity<LoginResponseDTO> response = null;

    if (firstLogin) {
      response = loginUserFirstTime(username, password);
    } else {
      response = loginUser(username, password);
    }

    RocketChatCredentials rocketChatCredentials =
        RocketChatCredentials.builder().RocketChatUserId(response.getBody().getData().getUserId())
            .RocketChatToken(response.getBody().getData().getAuthToken()).build();

    logoutUser(rocketChatCredentials);

    return rocketChatCredentials.getRocketChatUserId();
  }

  public ResponseEntity<LoginResponseDTO> loginUserFirstTime(String username, String password) {

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      LdapLoginDTO ldapLoginDTO = new LdapLoginDTO();
      ldapLoginDTO.setLdap(true);
      ldapLoginDTO.setUsername(username);
      ldapLoginDTO.setLdapPass(password);

      HttpEntity<LdapLoginDTO> request = new HttpEntity<LdapLoginDTO>(ldapLoginDTO, headers);

      ResponseEntity<LoginResponseDTO> response =
          restTemplate.postForEntity(rocketChatApiUserLogin, request, LoginResponseDTO.class);

      return response;

    } catch (

    Exception ex) {
      logService.logRocketChatError(
          String.format("Could not login user (%s) in Rocket.Chat for the first time", username),
          ex);
      throw new RocketChatLoginException(ex);
    }
  }

  /**
   * Performs a login with the given credentials and returns the Result
   * 
   * @param username
   * @param password
   * @return
   */
  public ResponseEntity<LoginResponseDTO> loginUser(String username, String password) {

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
      map.add("username", username);
      map.add("password", password);

      HttpEntity<MultiValueMap<String, String>> request =
          new HttpEntity<MultiValueMap<String, String>>(map, headers);

      ResponseEntity<LoginResponseDTO> response =
          restTemplate.postForEntity(rocketChatApiUserLogin, request, LoginResponseDTO.class);

      return response;

    } catch (Exception ex) {
      logService.logRocketChatError(
          String.format("Could not login user (%s) in Rocket.Chat", username), ex);
      throw new RocketChatLoginException(ex);
    }
  }

  /**
   * Performs a logout with the given credentials and returns true on success.
   * 
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return
   */
  public boolean logoutUser(RocketChatCredentials rocketChatCredentials) {

    try {
      HttpHeaders headers = getStandardHttpHeaders(rocketChatCredentials);

      HttpEntity<Void> request = new HttpEntity<Void>(headers);

      ResponseEntity<LogoutResponseDTO> response =
          restTemplate.postForEntity(rocketChatApiUserLogout, request, LogoutResponseDTO.class);

      return response != null && response.getStatusCode() == HttpStatus.OK ? true : false;

    } catch (Exception ex) {
      logService.logRocketChatError(String.format("Could not log out user id (%s) from Rocket.Chat",
          rocketChatCredentials.getRocketChatUserId()), ex);

      return false;
    }
  }

  /**
   * Adds the provided user to the Rocket.Chat group with given groupId
   * 
   * @param rcUserId Rocket.Chat userId
   * @param rcGroupId Rocket.Chat roomId
   */
  public void addUserToGroup(String rcUserId, String rcGroupId) {

    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    GroupResponseDTO response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      GroupAddUserBodyDTO body = new GroupAddUserBodyDTO(rcUserId, rcGroupId);
      HttpEntity<GroupAddUserBodyDTO> request = new HttpEntity<GroupAddUserBodyDTO>(body, header);

      response =
          restTemplate.postForObject(rocketChatApiGroupAddUserUrl, request, GroupResponseDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(String.format(
          "Could not add user %s to Rocket.Chat group with id %s", rcUserId, rcGroupId), ex);
      throw new RocketChatAddUserToGroupException(ex);
    }

    if (response != null && !response.isSuccess()) {
      String error = "Could not add user %s to Rocket.Chat group with id %s";
      logService.logRocketChatError(String.format(error, rcUserId, rcGroupId), response.getError(),
          response.getErrorType());
      throw new RocketChatAddUserToGroupException(String.format(error, rcUserId, rcGroupId));
    }
  }

  /**
   * Adds the technical user to the given Rocket.Chat group id
   * 
   * @param rcGroupId
   * @return
   */
  public boolean addTechnicalUserToGroup(String rcGroupId) {
    this.addUserToGroup(rcCredentialHelper.getTechnicalUser().getRocketChatUserId(), rcGroupId);
    return true;
  }

  /**
   * Removes the provided user from the Rocket.Chat group with given groupId
   * 
   * @param rcUserId Rocket.Chat userId
   * @param rcGroupId Rocket.Chat roomId
   * 
   */
  public void removeUserFromGroup(String rcUserId, String rcGroupId) {

    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    GroupResponseDTO response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      GroupRemoveUserBodyDTO body = new GroupRemoveUserBodyDTO(rcUserId, rcGroupId);
      HttpEntity<GroupRemoveUserBodyDTO> request =
          new HttpEntity<GroupRemoveUserBodyDTO>(body, header);

      response = restTemplate.postForObject(rocketChatApiGroupRemoveUserUrl, request,
          GroupResponseDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(String.format(
          "Could not remove user %s from Rocket.Chat group with id %s", rcUserId, rcGroupId), ex);
      throw new RocketChatRemoveUserFromGroupException(ex);
    }

    if (response != null && !response.isSuccess()) {
      String error = "Could not remove user %s from Rocket.Chat group with id %s";
      logService.logRocketChatError(String.format(error, rcUserId, rcGroupId), response.getError(),
          response.getErrorType());
      throw new RocketChatRemoveUserFromGroupException(String.format(error, rcUserId, rcGroupId));
    }
  }

  /**
   * Removes the technical user from the given Rocket.Chat group id
   * 
   * @param rcGroupId
   * @return
   */
  public boolean removeTechnicalUserFromGroup(String rcGroupId) {
    this.removeUserFromGroup(rcCredentialHelper.getTechnicalUser().getRocketChatUserId(),
        rcGroupId);
    return true;
  }

  /**
   * Get all standard members (all users except system user and technical user) of a rocket chat
   * group
   * 
   * @param rcGroupId
   */
  public List<GroupMemberDTO> getStandardMembersOfGroup(String rcGroupId) {

    List<GroupMemberDTO> groupMemberList =
        new ArrayList<GroupMemberDTO>(getMembersOfGroup(rcGroupId));

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
   * @param rcGroupId
   */
  public void removeAllStandardUsersFromGroup(String rcGroupId) {
    List<GroupMemberDTO> groupMemberList = getMembersOfGroup(rcGroupId);

    if (groupMemberList.isEmpty()) {
      throw new RocketChatGetGroupMembersException(
          String.format("Group member list from group with id %s is empty", rcGroupId));
    }

    groupMemberList.forEach(member -> {
      if (!member.get_id().equals(rcCredentialHelper.getTechnicalUser().getRocketChatUserId())
          && !member.get_id().equals(rcCredentialHelper.getSystemUser().getRocketChatUserId())) {
        removeUserFromGroup(member.get_id(), rcGroupId);
      }
    });
  }

  /**
   * Returns the group/room members of the given Rocket.Chat group id
   * 
   * @param rcGroupId
   * @return
   */
  public List<GroupMemberDTO> getMembersOfGroup(String rcGroupId) {

    ResponseEntity<GroupMemberResponseDTO> response = null;

    RocketChatCredentials systemUser = rcCredentialHelper.getSystemUser();

    try {
      HttpHeaders header = getStandardHttpHeaders(systemUser);
      HttpEntity<GroupAddUserBodyDTO> request = new HttpEntity<GroupAddUserBodyDTO>(header);

      response = restTemplate.exchange(rocketChatApiGetGroupMembersUrl + "?roomId=" + rcGroupId,
          HttpMethod.GET, request, GroupMemberResponseDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(
          String.format("Could not get Rocket.Chat group members for room id %s", rcGroupId), ex);
      throw new RocketChatGetGroupMembersException(ex);
    }

    if (response != null && response.getStatusCode() == HttpStatus.OK) {
      return Arrays.asList(response.getBody().getMembers());
    } else {
      String error = "Could not get Rocket.Chat group members for room id %s";
      logService.logRocketChatError(String.format(error, rcGroupId), response.getBody().getError(),
          response.getBody().getErrorType());
      throw new RocketChatGetGroupMembersException(String.format(error, rcGroupId));
    }
  }

  /**
   * Returns the group/room counters of the given Rocket.Chat group and user id.
   * 
   * @param rcGroupId
   * @param rcUserId
   * @return
   */
  public GroupCounterResponseDTO getGroupCounters(String rcGroupId, String rcUserId) {

    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    ResponseEntity<GroupCounterResponseDTO> response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      HttpEntity<?> request = new HttpEntity<>(header);

      response = restTemplate.exchange(
          rocketChatApiGetGroupCounters + "?roomId=" + rcGroupId + "&userId=" + rcUserId,
          HttpMethod.GET, request, GroupCounterResponseDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(
          String.format("Could not get Rocket.Chat group counters for room id %s and user id %s",
              rcGroupId, rcUserId),
          ex);
      throw new RocketChatGroupCountersException(ex);
    }

    if (response != null && response.getStatusCode() == HttpStatus.OK
        && response.getBody().getSuccess()) {
      return response.getBody();
    } else {
      String error =
          "Could not get Rocket.Chat group counters for room id %s and user id %s. Rocket.Chat response: %s";
      logService.logRocketChatError(String.format(error, rcGroupId, rcUserId,
          response != null ? response.getStatusCode().toString() : "empty"));
      throw new RocketChatGroupCountersException(String.format(error, rcGroupId, rcUserId,
          response != null ? response.getStatusCode().toString() : "empty"));
    }
  }

  /**
   * Deletes an user from Rocket.Chat. Returns true on success or a
   * {@link RocketChatRemoveUserException} on error.
   * 
   * @param rcUserId
   * @return
   */
  public boolean deleteUser(String rcUserId) {

    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    StandardResponseDTO response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      UserDeleteBodyDTO body = new UserDeleteBodyDTO(rcUserId);
      HttpEntity<UserDeleteBodyDTO> request = new HttpEntity<UserDeleteBodyDTO>(body, header);

      response =
          restTemplate.postForObject(rocketChatApiUserDelete, request, StandardResponseDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(
          String.format("Could not remove user %s from Rocket.Chat", rcUserId), ex);
      throw new RocketChatRemoveUserException(ex);
    }

    if (response != null && !response.isSuccess()) {
      logService.logRocketChatError(String.format("Could not remove user %s from Rocket.Chat: %s",
          rcUserId, response.getError()));

      return false;
    }

    return true;
  }

  /**
   * Removes all messages from the specified Rocket.Chat group written by the technical user from
   * the last 24 hours (avoiding time zone failures).
   * 
   * @param rcGroupId
   * @return
   */
  public boolean removeSystemMessages(String rcGroupId, LocalDateTime oldest,
      LocalDateTime latest) {
    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    return this.removeMessages(rcGroupId, new String[] {technicalUser.getRocketChatUsername()},
        oldest, latest);
  }

  /**
   * Removes all messages (from every user) from a Rocket.Chat group
   * 
   * @param rcGroupId
   * @return
   */
  public boolean removeAllMessages(String rcGroupId) {
    return removeMessages(rcGroupId, null, localDateTime1900, localDateTimeFuture);
  }

  /**
   * Removes all messages from the specified Rocket.Chat group written by the given user name array
   * which have been written between oldest and latest ({@link LocalDateTime}.
   * 
   * @param rcGroupId Rocket.Chat group id
   * @param users Array of usernames (String); null for all users
   * @param oldest {@link LocalDateTime}
   * @param latest {@link LocalDateTime}
   * @return
   */
  private boolean removeMessages(String rcGroupId, String[] users, LocalDateTime oldest,
      LocalDateTime latest) {

    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();

    StandardResponseDTO response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      GroupCleanHistoryDTO body = new GroupCleanHistoryDTO(rcGroupId,
          oldest.format(DateTimeFormatter.ofPattern(rcDateTimePattern)),
          latest.format(DateTimeFormatter.ofPattern(rcDateTimePattern)),
          (users != null && users.length > 0) ? users : new String[] {});
      HttpEntity<GroupCleanHistoryDTO> request = new HttpEntity<GroupCleanHistoryDTO>(body, header);

      response = restTemplate.postForObject(rocketChatApiCleanRoomHistory, request,
          StandardResponseDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(
          String.format("Could not clean history of Rocket.Chat group id %s", rcGroupId), ex);
      throw new RocketChatRemoveSystemMessagesException(ex);
    }

    if (response != null && !response.isSuccess()) {
      logService.logRocketChatError(
          String.format("Could not clean history of Rocket.Chat group id %s: %s", rcGroupId,
              response.getError()));

      return false;
    }

    return true;
  }

  /**
   * Returns the subscriptions for the given user id
   * 
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return
   */
  public List<SubscriptionsUpdateDTO> getSubscriptionsOfUser(
      RocketChatCredentials rocketChatCredentials) {

    ResponseEntity<SubscriptionsGetDTO> response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(rocketChatCredentials);
      HttpEntity<Void> request = new HttpEntity<Void>(header);

      response = restTemplate.exchange(rocketChatApiSubscriptionsGet, HttpMethod.GET, request,
          SubscriptionsGetDTO.class);

    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
        throw new UnauthorizedException(String.format(
            "Could not get Rocket.Chat subscriptions for user ID %s: Token is not active (401 Unauthorized)",
            rocketChatCredentials.getRocketChatUserId()));
      }
      logService.logRocketChatError(
          String.format("Could not get Rocket.Chat subscriptions for user id %s",
              rocketChatCredentials.getRocketChatUserId()),
          ex);
      throw new RocketChatGetSubscriptionsException(ex);
    }

    if (response != null && response.getStatusCode() == HttpStatus.OK) {
      return Arrays.asList(response.getBody().getUpdate());
    } else {
      String error = "Could not get Rocket.Chat subscriptions for user id %s";
      logService.logRocketChatError(
          String.format(error, rocketChatCredentials.getRocketChatUserId()),
          response.getBody().getMessage(), response.getBody().getStatus());
      throw new RocketChatGetSubscriptionsException(
          String.format(error, rocketChatCredentials.getRocketChatUserId()));
    }
  }

  /**
   * Returns the rooms for the given user id
   * 
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return
   */
  public List<RoomsUpdateDTO> getRoomsOfUser(RocketChatCredentials rocketChatCredentials) {

    ResponseEntity<RoomsGetDTO> response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(rocketChatCredentials);
      HttpEntity<Void> request = new HttpEntity<Void>(header);

      response =
          restTemplate.exchange(rocketChatApiRoomsGet, HttpMethod.GET, request, RoomsGetDTO.class);

    } catch (Exception ex) {
      logService.logRocketChatError(String.format("Could not get Rocket.Chat rooms for user id %s",
          rocketChatCredentials.getRocketChatUserId()), ex);
      throw new RocketChatGetRoomsException(ex);
    }

    if (response != null && response.getStatusCode() == HttpStatus.OK) {
      return Arrays.asList(response.getBody().getUpdate());
    } else {
      String error = "Could not get Rocket.Chat rooms for user id %s";
      logService.logRocketChatError(
          String.format(error, rocketChatCredentials.getRocketChatUserId()),
          response.getBody().getMessage(), response.getBody().getStatus());
      throw new RocketChatGetRoomsException(
          String.format(error, rocketChatCredentials.getRocketChatUserId()));
    }
  }

  /**
   * Returns the information of the given Rocket.Chat user
   * 
   * @param rcUserId Rocket.Chat user id
   * @return
   * 
   * @throws {@link RocketChatGetUserInfoException}
   */
  public UserInfoResponseDTO getUserInfo(String rcUserId) {

    RocketChatCredentials technicalUser = rcCredentialHelper.getTechnicalUser();
    ResponseEntity<UserInfoResponseDTO> response = null;

    try {
      HttpHeaders header = getStandardHttpHeaders(technicalUser);
      HttpEntity<Void> request = new HttpEntity<Void>(header);

      response = restTemplate.exchange(rocketChatApiUserInfo + "?userId=" + rcUserId,
          HttpMethod.GET, request, UserInfoResponseDTO.class);

    } catch (Exception ex) {
      throw new RocketChatGetUserInfoException(
          String.format("Could not get Rocket.Chat user info of user id %s", rcUserId), ex);
    }

    if (response == null || response.getBody() == null || response.getStatusCode() != HttpStatus.OK
        || !response.getBody().isSuccess()) {
      throw new RocketChatGetUserInfoException(
          String.format("Could not get Rocket.Chat user info of user id %s", rcUserId));
    }

    return response.getBody();
  }

}

package de.caritas.cob.userservice.api.config.auth;

import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ANONYMOUS_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ASSIGN_CONSULTANT_TO_PEER_SESSION;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.CONSULTANT_CREATE;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.CONSULTANT_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.CONSULTANT_UPDATE;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.CREATE_NEW_CHAT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.START_CHAT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.STOP_CHAT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.TECHNICAL_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.UPDATE_CHAT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.USER_DEFAULT;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.USE_FEEDBACK;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_AGENCY_CONSULTANTS;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS;
import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.VIEW_ALL_PEER_SESSIONS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Definition of all authorities and of the role-authority-mapping. */
@AllArgsConstructor
@Getter
public enum Authority {
  ANONYMOUS(UserRole.ANONYMOUS, singletonList(ANONYMOUS_DEFAULT)),
  USER(UserRole.USER, List.of(USER_DEFAULT, ASSIGN_CONSULTANT_TO_SESSION)),
  CONSULTANT(
      UserRole.CONSULTANT,
      List.of(CONSULTANT_DEFAULT, ASSIGN_CONSULTANT_TO_SESSION, VIEW_AGENCY_CONSULTANTS)),
  PEER_CONSULTANT(UserRole.PEER_CONSULTANT, singletonList(USE_FEEDBACK)),
  MAIN_CONSULTANT(
      UserRole.MAIN_CONSULTANT,
      List.of(
          VIEW_ALL_FEEDBACK_SESSIONS,
          VIEW_ALL_PEER_SESSIONS,
          ASSIGN_CONSULTANT_TO_ENQUIRY,
          ASSIGN_CONSULTANT_TO_PEER_SESSION)),
  TECHNICAL(UserRole.TECHNICAL, singletonList(TECHNICAL_DEFAULT)),
  NOTIFICATIONS_TECHNICAL(
      UserRole.NOTIFICATIONS_TECHNICAL, singletonList(AuthorityValue.NOTIFICATIONS_TECHNICAL)),
  GROUP_CHAT_CONSULTANT(
      UserRole.GROUP_CHAT_CONSULTANT,
      List.of(CONSULTANT_DEFAULT, CREATE_NEW_CHAT, START_CHAT, STOP_CHAT, UPDATE_CHAT)),
  USER_ADMIN(
      UserRole.USER_ADMIN,
      List.of(AuthorityValue.USER_ADMIN, CONSULTANT_UPDATE, CONSULTANT_CREATE)),
  SINGLE_TENANT_ADMIN(
      UserRole.SINGLE_TENANT_ADMIN, singletonList(AuthorityValue.SINGLE_TENANT_ADMIN)),
  TENANT_ADMIN(UserRole.TENANT_ADMIN, singletonList(AuthorityValue.TENANT_ADMIN)),

  RESTRICTED_CONSULTANT_ADMIN(
      UserRole.RESTRICTED_CONSULTANT_ADMIN, List.of(CONSULTANT_CREATE, CONSULTANT_UPDATE)),
  RESTRICTED_AGENCY_ADMIN(
      UserRole.RESTRICTED_AGENCY_ADMIN, singletonList(AuthorityValue.RESTRICTED_AGENCY_ADMIN));

  private final UserRole userRole;
  private final List<String> grantedAuthorities;

  public static List<String> getAuthoritiesByUserRole(UserRole userRole) {
    Optional<Authority> authorityByUserRole =
        Stream.of(values()).filter(authority -> authority.userRole.equals(userRole)).findFirst();

    return authorityByUserRole.isPresent()
        ? authorityByUserRole.get().getGrantedAuthorities()
        : emptyList();
  }

  public static class AuthorityValue {

    private AuthorityValue() {}

    public static final String PREFIX = "AUTHORIZATION_";
    public static final String ANONYMOUS_DEFAULT = PREFIX + "ANONYMOUS_DEFAULT";
    public static final String NOTIFICATIONS_TECHNICAL = PREFIX + "NOTIFICATIONS_TECHNICAL";
    public static final String USER_DEFAULT = PREFIX + "USER_DEFAULT";
    public static final String CONSULTANT_DEFAULT = PREFIX + "CONSULTANT_DEFAULT";
    public static final String USE_FEEDBACK = PREFIX + "USE_FEEDBACK";
    public static final String VIEW_ALL_FEEDBACK_SESSIONS = PREFIX + "VIEW_ALL_FEEDBACK_SESSIONS";
    public static final String VIEW_ALL_PEER_SESSIONS = PREFIX + "VIEW_ALL_PEER_SESSIONS";
    public static final String ASSIGN_CONSULTANT_TO_SESSION =
        PREFIX + "ASSIGN_CONSULTANT_TO_SESSION";
    public static final String ASSIGN_CONSULTANT_TO_ENQUIRY =
        PREFIX + "ASSIGN_CONSULTANT_TO_ENQUIRY";
    public static final String ASSIGN_CONSULTANT_TO_PEER_SESSION =
        PREFIX + "ASSIGN_CONSULTANT_TO_PEER_SESSION";
    public static final String VIEW_AGENCY_CONSULTANTS = PREFIX + "VIEW_AGENCY_CONSULTANTS";
    public static final String TECHNICAL_DEFAULT = PREFIX + "TECHNICAL_DEFAULT";
    public static final String CREATE_NEW_CHAT = PREFIX + "CREATE_NEW_CHAT";
    public static final String START_CHAT = PREFIX + "START_CHAT";
    public static final String STOP_CHAT = PREFIX + "STOP_CHAT";
    public static final String UPDATE_CHAT = PREFIX + "UPDATE_CHAT";
    public static final String USER_ADMIN = PREFIX + "USER_ADMIN";
    public static final String CONSULTANT_CREATE = PREFIX + "CONSULTANT_CREATE";
    public static final String CONSULTANT_UPDATE = PREFIX + "CONSULTANT_UPDATE";

    public static final String SINGLE_TENANT_ADMIN = PREFIX + "SINGLE_TENANT_ADMIN";
    public static final String TENANT_ADMIN = PREFIX + "TENANT_ADMIN";
    public static final String RESTRICTED_AGENCY_ADMIN = PREFIX + "RESTRICTED_AGENCY_ADMIN";
  }
}

package de.caritas.cob.userservice.api.authorization;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Definition of all authorities and of the role-authority-mapping.
 */
@AllArgsConstructor
@Getter
public enum Authority {

  ANONYMOUS(UserRole.ANONYMOUS, singletonList(AuthorityValue.ANONYMOUS_DEFAULT)),
  USER(UserRole.USER, singletonList(AuthorityValue.USER_DEFAULT)),
  CONSULTANT(UserRole.CONSULTANT, singletonList(AuthorityValue.CONSULTANT_DEFAULT)),
  U25_CONSULTANT(UserRole.U25_CONSULTANT, singletonList(AuthorityValue.USE_FEEDBACK)),
  U25_MAIN_CONSULTANT(UserRole.U25_MAIN_CONSULTANT, asList(
      AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.VIEW_ALL_PEER_SESSIONS,
      AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION, AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
      AuthorityValue.VIEW_AGENCY_CONSULTANTS)),
  TECHNICAL(UserRole.TECHNICAL, singletonList(AuthorityValue.TECHNICAL_DEFAULT)),
  GROUP_CHAT_CONSULTANT(UserRole.GROUP_CHAT_CONSULTANT, asList(
      AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.CREATE_NEW_CHAT,
      AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT)),
  USER_ADMIN(UserRole.USER_ADMIN, singletonList(AuthorityValue.USER_ADMIN));

  private final UserRole userRole;
  private final List<String> grantedAuthorities;

  public static List<String> getAuthoritiesByUserRole(UserRole userRole) {
    Optional<Authority> authorityByUserRole = Stream.of(values())
        .filter(authority -> authority.userRole.equals(userRole))
        .findFirst();

    return authorityByUserRole.isPresent() ? authorityByUserRole.get().getGrantedAuthorities()
        : emptyList();
  }

  public static class AuthorityValue {

    private AuthorityValue() {
    }

    public static final String PREFIX = "AUTHORIZATION_";
    public static final String ANONYMOUS_DEFAULT = PREFIX + "ANONYMOUS_DEFAULT";
    public static final String USER_DEFAULT = PREFIX + "USER_DEFAULT";
    public static final String CONSULTANT_DEFAULT = PREFIX + "CONSULTANT_DEFAULT";
    public static final String USE_FEEDBACK = PREFIX + "USE_FEEDBACK";
    public static final String VIEW_ALL_FEEDBACK_SESSIONS = PREFIX + "VIEW_ALL_FEEDBACK_SESSIONS";
    public static final String VIEW_ALL_PEER_SESSIONS = PREFIX + "VIEW_ALL_PEER_SESSIONS";
    public static final String ASSIGN_CONSULTANT_TO_SESSION =
        PREFIX + "ASSIGN_CONSULTANT_TO_SESSION";
    public static final String ASSIGN_CONSULTANT_TO_ENQUIRY =
        PREFIX + "ASSIGN_CONSULTANT_TO_ENQUIRY";
    public static final String VIEW_AGENCY_CONSULTANTS = PREFIX + "VIEW_AGENCY_CONSULTANTS";
    public static final String TECHNICAL_DEFAULT = PREFIX + "TECHNICAL_DEFAULT";
    public static final String CREATE_NEW_CHAT = PREFIX + "CREATE_NEW_CHAT";
    public static final String START_CHAT = PREFIX + "START_CHAT";
    public static final String STOP_CHAT = PREFIX + "STOP_CHAT";
    public static final String UPDATE_CHAT = PREFIX + "UPDATE_CHAT";
    public static final String USER_ADMIN = PREFIX + "USER_ADMIN";
  }

}

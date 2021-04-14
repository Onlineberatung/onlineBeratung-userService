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
public enum Authorities {

  CONSULTANT(UserRole.CONSULTANT, singletonList(Authority.CONSULTANT_DEFAULT)),
  USER(UserRole.USER, singletonList(Authority.USER_DEFAULT)),
  U25_CONSULTANT(UserRole.U25_CONSULTANT, singletonList(Authority.USE_FEEDBACK)),
  U25_MAIN_CONSULTANT(UserRole.U25_MAIN_CONSULTANT, asList(
      Authority.VIEW_ALL_FEEDBACK_SESSIONS, Authority.VIEW_ALL_PEER_SESSIONS,
      Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
      Authority.VIEW_AGENCY_CONSULTANTS)),
  TECHNICAL(UserRole.TECHNICAL, singletonList(Authority.TECHNICAL_DEFAULT)),
  GROUP_CHAT_CONSULTANT(UserRole.GROUP_CHAT_CONSULTANT, asList(
      Authority.CONSULTANT_DEFAULT, Authority.CREATE_NEW_CHAT,
      Authority.START_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT)),
  USER_ADMIN(UserRole.USER_ADMIN, singletonList(Authority.USER_ADMIN));

  private final UserRole userRole;
  private final List<String> grantedAuthorities;

  public static List<String> getAuthoritiesByUserRole(UserRole userRole) {
    Optional<Authorities> authorityByUserRole = Stream.of(values())
        .filter(authority -> authority.userRole.equals(userRole))
        .findFirst();

    return authorityByUserRole.isPresent() ? authorityByUserRole.get().getGrantedAuthorities()
        : emptyList();
  }

  public static class Authority {

    private Authority() {
    }

    public static final String PREFIX = "AUTHORIZATION_";
    public static final String CONSULTANT_DEFAULT = PREFIX + "CONSULTANT_DEFAULT";
    public static final String USER_DEFAULT = PREFIX + "USER_DEFAULT";
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

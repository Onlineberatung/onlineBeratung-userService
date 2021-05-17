package de.caritas.cob.userservice.api.service.user.anonymous;

import static java.lang.Integer.parseInt;
import static java.util.Collections.sort;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.LinkedList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registry to generate, hold and handle all current anonymous usernames.
 */
@Component
@RequiredArgsConstructor
public class AnonymousUsernameRegistry {

  private final @NonNull UserService userService;
  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();

  @Value("${anonymous.username.prefix}")
  private String usernamePrefix;

  private static final LinkedList<Integer> ID_REGISTRY = new LinkedList<>();

  /**
   * Generates an unique anonymous username.
   *
   * @return encoded unique anonymous username
   */
  public synchronized String generateUniqueUsername() {

    String username;
    do {
      username = generateUsername();
      ID_REGISTRY.add(obtainUsernameId(username));
    } while (isUsernameOccupied(username));


    return usernameTranscoder.encodeUsername(username);
  }

  private String generateUsername() {
    return usernamePrefix + obtainSmallestPossibleId();
  }

  private int obtainSmallestPossibleId() {

    var smallestId = 1;
    sort(ID_REGISTRY);

    for (int i : ID_REGISTRY) {
      if (smallestId < i) {
        return smallestId;
      }
      smallestId = i + 1;
    }

    return smallestId;
  }

  private boolean isUsernameOccupied(String username) {
    return userService.findUserByUsername(username)
        .stream()
        .count() > 0;
  }

  private int obtainUsernameId(String username) {
    return parseInt(substringAfter(username, usernamePrefix));
  }
}

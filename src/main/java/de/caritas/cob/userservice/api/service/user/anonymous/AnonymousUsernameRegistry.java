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

  /**
   * Removes a name from the registry.
   *
   * @param encodedUsername the encoded username to remove from the registry
   */
  public synchronized void removeRegistryIdByUsername(String encodedUsername) {
    var usernameParsed = false;
    var usernameId = -1;
    try {
      var decodedUsername = usernameTranscoder.decodeUsername(encodedUsername);
      usernameId = obtainUsernameId(decodedUsername);
      usernameParsed = true;
    } catch (Exception ex) {
      // do nothing
    }

    if (usernameParsed) {
      removeRegistryIdIfFound(usernameId);
    }
  }

  private void removeRegistryIdIfFound(int registryId) {
    var valueIndex = findIndexByRegistryId(registryId);
    if (valueIndex >= 0 && valueIndex < ID_REGISTRY.size()) {
      ID_REGISTRY.remove(valueIndex);
    }
  }

  private int findIndexByRegistryId(int registryId) {
    for (var valueIndex = 0; valueIndex < ID_REGISTRY.size(); valueIndex++) {
      if (ID_REGISTRY.get(valueIndex) == registryId) {
        return valueIndex;
      }
    }
    return -1;
  }
}

package de.caritas.cob.userservice.api.service.liveevents;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class UserIdsProvider {

  final @NonNull AuthenticatedUser authenticatedUser;

  abstract List<String> collectUserIds(String rcGroupId);

  boolean notInitiatingUser(String userId) {
    return !userId.equals(this.authenticatedUser.getUserId());
  }

}

package de.caritas.cob.userservice.api.service.liveevents;

import de.caritas.cob.userservice.api.port.out.ChatRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Factory to decide which {@link UserIdsProvider} to be used for collecting user ids. */
@Component
@RequiredArgsConstructor
public class UserIdsProviderFactory {

  private final @NonNull RelevantUserAccountIdsByChatProvider byChatProvider;
  private final @NonNull RelevantUserAccountIdsBySessionProvider bySessionProvider;
  private final @NonNull ChatRepository chatRepository;

  /**
   * Provides the relevant {@link UserIdsProvider}.
   *
   * @param rcGroupId the rocket chat group id
   * @return {@link RelevantUserAccountIdsByChatProvider} if the group id belongs to a chat and
   *     {@link RelevantUserAccountIdsBySessionProvider} if not
   */
  public UserIdsProvider byRocketChatGroup(String rcGroupId) {
    return isChat(rcGroupId) ? this.byChatProvider : this.bySessionProvider;
  }

  private boolean isChat(String rcGroupId) {
    return this.chatRepository.findByGroupId(rcGroupId).isPresent();
  }
}

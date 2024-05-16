package de.caritas.cob.userservice.api.service.liveevents;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.port.out.ChatRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserIdsProviderFactoryTest {

  @InjectMocks private UserIdsProviderFactory userIdsProviderFactory;

  @Mock private ChatRepository chatRepository;

  @Mock private RelevantUserAccountIdsByChatProvider byChatProvider;

  @Mock private RelevantUserAccountIdsBySessionProvider bySessionProvider;

  @Test
  public void buildUserIdsProvider_Should_returnByChatProvider_When_rcGroupIdIsAChat() {
    when(this.chatRepository.findByGroupId(any())).thenReturn(Optional.of(ACTIVE_CHAT));

    UserIdsProvider resultProvider = this.userIdsProviderFactory.byRocketChatGroup("group");

    assertThat(resultProvider, is(this.byChatProvider));
  }

  @Test
  public void buildUserIdsProvider_Should_returnBySessionProvider_When_rcGroupIdIsNotChat() {
    when(this.chatRepository.findByGroupId(any())).thenReturn(Optional.empty());

    UserIdsProvider resultProvider = this.userIdsProviderFactory.byRocketChatGroup("group");

    assertThat(resultProvider, is(this.bySessionProvider));
  }
}

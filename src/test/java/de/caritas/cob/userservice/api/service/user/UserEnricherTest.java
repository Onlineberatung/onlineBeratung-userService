package de.caritas.cob.userservice.api.service.user;

import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetUserIdException;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserEnricherTest {

  @InjectMocks
  UserEnricher userEnricher;

  @Mock
  RocketChatService rocketChatService;

  @Test
  void enrichUserWithRocketChatId_Should_ThrowInternalServiceError_WhenRocketChatRequestFailed()
      throws RocketChatGetUserIdException {
    doThrow(new RocketChatGetUserIdException("Error"))
        .when(rocketChatService).getRocketChatUserIdByUsername(any());
    Optional<User> user = Optional.of(USER);
    assertThrows(InternalServerErrorException.class,
        () -> userEnricher.enrichUserWithRocketChatId(user));
  }

  @Test
  void enrichUserWithRocketChatId_Should_EnrichUserWithRocketChatId()
      throws RocketChatGetUserIdException {

    User user = new User();
    user.setUsername(RC_USERNAME);

    when(rocketChatService.getRocketChatUserIdByUsername(user.getUsername())).thenReturn(RC_USER_ID);

    Optional<User> result = userEnricher.enrichUserWithRocketChatId(Optional.of(user));

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getRcUserId(), is(RC_USER_ID));
    verify(rocketChatService, times(1)).getRocketChatUserIdByUsername(user.getUsername());
  }

}

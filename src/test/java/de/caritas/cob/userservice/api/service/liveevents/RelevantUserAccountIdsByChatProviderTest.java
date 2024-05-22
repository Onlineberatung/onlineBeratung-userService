package de.caritas.cob.userservice.api.service.liveevents;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RelevantUserAccountIdsByChatProviderTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks private RelevantUserAccountIdsByChatProvider byChatProvider;

  @Mock private RocketChatService rocketChatService;

  @Mock private UserService userService;

  @Mock private ConsultantService consultantService;

  @Test
  void collectUserIds_Should_returnAllMergedDependingIds_When_rcGroupHasMembers() {
    List<GroupMemberDTO> groupMembers =
        asList(memberDTOWithRcId("rc1"), memberDTOWithRcId("rc2"), memberDTOWithRcId("rc3"));
    when(this.rocketChatService.getChatUsers(any())).thenReturn(groupMembers);
    when(this.consultantService.getConsultantByRcUserId(eq("rc1")))
        .thenReturn(Optional.of(consultantWithId("consultant1")));
    when(this.userService.findUserByRcUserId(eq("rc2")))
        .thenReturn(Optional.of(userWithId("user1")));
    when(this.userService.findUserByRcUserId(eq("rc3")))
        .thenReturn(Optional.of(userWithId("user2")));

    List<String> collectedUserIds = this.byChatProvider.collectUserIds("groupId");

    assertThat(collectedUserIds, hasSize(3));
    assertThat(collectedUserIds.get(0), is("consultant1"));
    assertThat(collectedUserIds.get(1), is("user1"));
    assertThat(collectedUserIds.get(2), is("user2"));
  }

  private GroupMemberDTO memberDTOWithRcId(String rcId) {
    GroupMemberDTO groupMemberDTO = new GroupMemberDTO();
    groupMemberDTO.set_id(rcId);
    return groupMemberDTO;
  }

  private Consultant consultantWithId(String consultantId) {
    Consultant consultant = new Consultant();
    consultant.setId(consultantId);
    return consultant;
  }

  private User userWithId(String userId) {
    var username = RandomStringUtils.randomAlphabetic(8);
    var email =
        RandomStringUtils.randomAlphabetic(4, 8)
            + "@"
            + RandomStringUtils.randomAlphabetic(4, 8)
            + ".com";

    return new User(userId, null, username, email, false);
  }

  @Test
  void
      collectUserIds_Should_returnAllMergedDependingIdsInsteadOfNotAvailableUser_When_rcGroupHasMembers() {
    List<GroupMemberDTO> groupMembers =
        asList(memberDTOWithRcId("rc1"), memberDTOWithRcId("rc2"), memberDTOWithRcId("rc3"));
    when(this.rocketChatService.getChatUsers(any())).thenReturn(groupMembers);
    when(this.consultantService.getConsultantByRcUserId(eq("rc1")))
        .thenReturn(Optional.of(consultantWithId("consultant1")));
    when(this.userService.findUserByRcUserId(eq("rc3")))
        .thenReturn(Optional.of(userWithId("user2")));

    List<String> collectedUserIds = this.byChatProvider.collectUserIds("groupId");

    assertThat(collectedUserIds, hasSize(2));
    assertThat(collectedUserIds.get(0), is("consultant1"));
    assertThat(collectedUserIds.get(1), is("user2"));
  }
}

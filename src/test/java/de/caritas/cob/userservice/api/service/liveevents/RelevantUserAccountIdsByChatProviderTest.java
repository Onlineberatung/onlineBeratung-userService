package de.caritas.cob.userservice.api.service.liveevents;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RelevantUserAccountIdsByChatProviderTest {

  private final static EasyRandom easyRandom = new EasyRandom();

  @InjectMocks
  private RelevantUserAccountIdsByChatProvider byChatProvider;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private UserService userService;

  @Mock
  private ConsultantService consultantService;

  @Test
  public void collectUserIds_Should_returnEmptyList_When_rocketChatServiceThrowsException()
      throws RocketChatGetGroupMembersException {
    when(this.rocketChatService.getMembersOfGroup(any()))
        .thenThrow(new RocketChatGetGroupMembersException(""));

    List<String> collectedUserIds = this.byChatProvider.collectUserIds("groupId");

    assertThat(collectedUserIds, hasSize(0));
  }

  @Test
  public void collectUserIds_Should_returnAllMergedDependingIds_When_rcGroupHasMembers()
      throws RocketChatGetGroupMembersException {
    List<GroupMemberDTO> groupMembers = asList(
        memberDTOWithRcId("rc1"), memberDTOWithRcId("rc2"), memberDTOWithRcId("rc3"));
    when(this.rocketChatService.getMembersOfGroup(any())).thenReturn(groupMembers);
    when(this.consultantService.getConsultantByRcUserId("rc1"))
        .thenReturn(Optional.of(consultantWithId("consultant1")));
    when(this.userService.findUserByRcUserId("rc2"))
        .thenReturn(Optional.of(userWithId("user1")));
    when(this.userService.findUserByRcUserId("rc3"))
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
    var email = RandomStringUtils.randomAlphabetic(4, 8) + "@"
        + RandomStringUtils.randomAlphabetic(4, 8) + ".com";

    return new User(userId, null, username, email, false);
  }

  @Test
  public void collectUserIds_Should_returnAllMergedDependingIdsInsteadOfNotAvailableUser_When_rcGroupHasMembers()
      throws RocketChatGetGroupMembersException {
    List<GroupMemberDTO> groupMembers = asList(
        memberDTOWithRcId("rc1"), memberDTOWithRcId("rc2"), memberDTOWithRcId("rc3"));
    when(this.rocketChatService.getMembersOfGroup(any())).thenReturn(groupMembers);
    when(this.consultantService.getConsultantByRcUserId("rc1"))
        .thenReturn(Optional.of(consultantWithId("consultant1")));
    when(this.userService.findUserByRcUserId("rc3"))
        .thenReturn(Optional.of(userWithId("user2")));

    List<String> collectedUserIds = this.byChatProvider.collectUserIds("groupId");

    assertThat(collectedUserIds, hasSize(2));
    assertThat(collectedUserIds.get(0), is("consultant1"));
    assertThat(collectedUserIds.get(1), is("user2"));
  }

}

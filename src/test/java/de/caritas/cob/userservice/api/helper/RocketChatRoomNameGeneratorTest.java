package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RocketChatRoomNameGeneratorTest {

  private RocketChatRoomNameGenerator rocketChatRoomNameGenerator;

  @BeforeEach
  public void setup() {
    this.rocketChatRoomNameGenerator = new RocketChatRoomNameGenerator();
  }

  @Test
  public void generateGroupName_Should_ReturnGroupNameContainingSessionIdAndTimestamp() {

    String groupName = rocketChatRoomNameGenerator.generateGroupName(SESSION);

    assertThat(groupName, startsWith(String.valueOf(SESSION.getId())));
    assertTrue(groupName.matches("^[0-9]+_[0-9]+"));
  }

  @Test
  public void
      generateFeedbackGroupName_Should_ReturnGroupNameContainingSessionIdAndTimestampAndFeedbackIdentifier() {

    String groupName = rocketChatRoomNameGenerator.generateFeedbackGroupName(SESSION);

    assertThat(groupName, startsWith(String.valueOf(SESSION.getId())));
    assertTrue(groupName.matches("^[0-9]+_feedback_[0-9]+"));
  }

  @Test
  public void
      generateGroupChatName_Should_ReturnGroupNameContainingSessionIdAndTimestampAndGroupChatIdentifier() {

    String groupName = rocketChatRoomNameGenerator.generateGroupChatName(ACTIVE_CHAT);

    assertThat(groupName, startsWith(String.valueOf(ACTIVE_CHAT.getId())));
    assertTrue(groupName.matches("^[0-9]+_group_chat_[0-9]+"));
  }
}

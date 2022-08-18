package de.caritas.cob.userservice.api.helper;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageHelperTest {

  private final String USERNAME = "benutzer1";
  private final String MESSAGE_WITH_USERNAME_PLACEHOLDER = "Hallo ${username}";
  private final String MESSAGE_WITH_REPLACED_USERNAME_PLACEHOLDER = "Hallo " + USERNAME;
  private final String PLACEHOLDER1 = "placeholder1";
  private final String PLACEHOLDER1_VALUE = "PLACEHOLDER1";
  private final String PLACEHOLDER2 = "placeholder2";
  private final String PLACEHOLDER2_VALUE = "PLACEHOLDER2";
  private final String MESSAGE_WITH_PLACEHOLDERS =
      "test ${" + PLACEHOLDER1 + "} test ${" + PLACEHOLDER2 + "}";
  private final String MESSAGE_WITH_REPLACED_PLACEHOLDERS =
      "test " + PLACEHOLDER1_VALUE + " test " + PLACEHOLDER2_VALUE;

  @Test
  public void replaceUsernameInMessage_Should_ReplaceUsernamePlaceholderInGivenMessage() {

    String result =
        MessageHelper.replaceUsernameInMessage(MESSAGE_WITH_USERNAME_PLACEHOLDER, USERNAME);

    assertEquals(MESSAGE_WITH_REPLACED_USERNAME_PLACEHOLDER, result);
  }

  @Test
  public void replacePlaceholderInMessage_Should_ReplaceAllPlaceholdersInMessage() {

    Map<String, Object> placeholderMap = new HashMap<String, Object>();
    placeholderMap.put(PLACEHOLDER1, PLACEHOLDER1_VALUE);
    placeholderMap.put(PLACEHOLDER2, PLACEHOLDER2_VALUE);

    String result =
        MessageHelper.replacePlaceholderInMessage(MESSAGE_WITH_PLACEHOLDERS, placeholderMap);

    assertEquals(MESSAGE_WITH_REPLACED_PLACEHOLDERS, result);
  }
}

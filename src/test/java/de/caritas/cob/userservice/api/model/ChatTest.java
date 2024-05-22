package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

public class ChatTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    Chat chat = easyRandom.nextObject(Chat.class);

    assertThat(chat, is(chat));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoChatInstance() {
    Chat chat = easyRandom.nextObject(Chat.class);

    boolean equals = chat.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_sessionIdsAreDifferent() {
    Chat chat = easyRandom.nextObject(Chat.class);
    chat.setId(1L);
    Chat otherChat = easyRandom.nextObject(Chat.class);
    otherChat.setId(2L);

    boolean equals = chat.equals(otherChat);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_sessionIdsAreEqual() {
    Chat chat = easyRandom.nextObject(Chat.class);
    chat.setId(1L);
    Chat otherChat = easyRandom.nextObject(Chat.class);
    otherChat.setId(1L);

    boolean equals = chat.equals(otherChat);

    assertThat(equals, is(true));
  }

  @Test
  public void nextDateShouldThrowExceptionWhenChatIsRepetitiveButNoStartDate() {
    assertThrows(
        InternalServerErrorException.class,
        () -> {
          var chat = new Chat();
          chat.setRepetitive(true);

          chat.nextStart();
        });
  }

  @Test
  public void nextDateShouldReturnCorrectNextStartDateWhenChatIsRepetitive() {
    var chat = easyRandom.nextObject(Chat.class);
    chat.setRepetitive(true);

    assertThat(chat.nextStart(), is(notNullValue()));
  }
}

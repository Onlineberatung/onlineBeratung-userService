package de.caritas.cob.userservice.api.repository.chat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.Test;

public class ChatTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    Chat chat = new EasyRandom().nextObject(Chat.class);

    assertThat(chat, is(chat));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoChatInstance() {
    Chat chat = new EasyRandom().nextObject(Chat.class);

    boolean equals = chat.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_sessionIdsAreDifferent() {
    Chat chat = new EasyRandom().nextObject(Chat.class);
    chat.setId(1L);
    Chat otherChat = new EasyRandom().nextObject(Chat.class);
    otherChat.setId(2L);

    boolean equals = chat.equals(otherChat);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_sessionIdsAreEqual() {
    Chat chat = new EasyRandom().nextObject(Chat.class);
    chat.setId(1L);
    Chat otherChat = new EasyRandom().nextObject(Chat.class);
    otherChat.setId(1L);

    boolean equals = chat.equals(otherChat);

    assertThat(equals, is(true));
  }

}

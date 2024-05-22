package de.caritas.cob.userservice.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

public class ChatAgencyTest {

  @Test
  public void equals_Should_returnTrue_When_objectIsSameReference() {
    ChatAgency chatAgency = new EasyRandom().nextObject(ChatAgency.class);

    assertThat(chatAgency, is(chatAgency));
  }

  @Test
  public void equals_Should_returnFalse_When_objectIsNoChatAgencyInstance() {
    ChatAgency chatAgency = new EasyRandom().nextObject(ChatAgency.class);

    boolean equals = chatAgency.equals(new Object());

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnFalse_When_sessionIdsAreDifferent() {
    ChatAgency chatAgency = new EasyRandom().nextObject(ChatAgency.class);
    chatAgency.setId(1L);
    ChatAgency otherChatAgency = new EasyRandom().nextObject(ChatAgency.class);
    otherChatAgency.setId(2L);

    boolean equals = chatAgency.equals(otherChatAgency);

    assertThat(equals, is(false));
  }

  @Test
  public void equals_Should_returnTrue_When_sessionIdsAreEqual() {
    ChatAgency chatAgency = new EasyRandom().nextObject(ChatAgency.class);
    chatAgency.setId(1L);
    ChatAgency otherChatAgency = new EasyRandom().nextObject(ChatAgency.class);
    otherChatAgency.setId(1L);

    boolean equals = chatAgency.equals(otherChatAgency);

    assertThat(equals, is(true));
  }
}

package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.deserializer;

import static de.caritas.cob.userservice.messageservice.generated.web.model.MessageType.FINISHED_CONVERSATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ForwardMessageDTO;
import java.util.Optional;
import org.junit.Test;

public class AliasMessageConverterTest {

  @Test
  public void
      convertStringToAliasMessageDTO_Should_returnOptionalEmpty_When_jsonStringCanNotBeConverted() {
    Optional<AliasMessageDTO> result =
        new AliasMessageConverter().convertStringToAliasMessageDTO("alias");

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void
      convertStringToForwardMessageDTO_Should_returnOptionalEmpty_When_jsonStringCanNotBeConverted() {
    Optional<ForwardMessageDTO> result =
        new AliasMessageConverter().convertStringToForwardMessageDTO("alias");

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void
      convertStringToAliasMessageDTO_Should_returnExpectedResult_When_jsonStringContainsMessageTypeFinishedConversation() {
    var result =
        new AliasMessageConverter()
            .convertStringToAliasMessageDTO("{\"messageType\":\"FINISHED_CONVERSATION\"}");

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getMessageType().toString(), is(FINISHED_CONVERSATION.toString()));
  }
}

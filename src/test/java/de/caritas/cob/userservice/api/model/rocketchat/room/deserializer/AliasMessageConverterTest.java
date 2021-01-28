package de.caritas.cob.userservice.api.model.rocketchat.room.deserializer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.api.model.AliasMessageDTO;
import de.caritas.cob.userservice.api.model.ForwardMessageDTO;
import java.util.Optional;
import org.junit.Test;

public class AliasMessageConverterTest {

  @Test
  public void convertStringToAliasMessageDTO_Should_returnOptionalEmpty_When_jsonStringCanNotBeConverted() {
    Optional<AliasMessageDTO> result = new AliasMessageConverter()
        .convertStringToAliasMessageDTO("alias");

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void convertStringToForwardMessageDTO_Should_returnOptionalEmpty_When_jsonStringCanNotBeConverted() {
    Optional<ForwardMessageDTO> result = new AliasMessageConverter()
        .convertStringToForwardMessageDTO("alias");

    assertThat(result.isPresent(), is(false));
  }

}

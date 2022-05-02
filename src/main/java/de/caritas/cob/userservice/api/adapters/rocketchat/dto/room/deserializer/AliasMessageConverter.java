package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.deserializer;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ForwardMessageDTO;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Converter to transform json string values into alias objects.
 */
@Slf4j
public class AliasMessageConverter {

  /**
   * Maps a given String to a {@link ForwardMessageDTO}.
   *
   * @param alias String
   * @return Optional of {@link ForwardMessageDTO}
   */
  public Optional<ForwardMessageDTO> convertStringToForwardMessageDTO(String alias) {
    try {
      return ofNullable(
          new ObjectMapper().readValue(decode(alias, UTF_8.name()),
              ForwardMessageDTO.class));
    } catch (IOException jsonParseEx) {
      // This is not an error any more due to restructuring of the alias object. This is not a
      // real error, but necessary due to legacy code
      return empty();
    }
  }

  /**
   * Maps a given String to a {@link AliasMessageDTO}.
   *
   * @param alias String
   * @return Optional of {@link AliasMessageDTO}
   */
  public Optional<AliasMessageDTO> convertStringToAliasMessageDTO(String alias) {
    try {
      return ofNullable(
          new ObjectMapper().readValue(decode(alias, UTF_8.name()),
              AliasMessageDTO.class));

    } catch (IOException jsonParseEx) {
      log.error("Internal Server Error: Could not convert alias String to AliasMessageDTO",
          jsonParseEx);

      return empty();
    }
  }

}

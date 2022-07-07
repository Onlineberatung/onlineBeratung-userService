package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.deserializer;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ForwardMessageDTO;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.adapters.web.dto.VideoCallMessageDTO;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Json Deserializer for the alias.
 */
public class AliasJsonDeserializer extends JsonDeserializer<AliasMessageDTO> {

  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();
  private final AliasMessageConverter aliasMessageConverter = new AliasMessageConverter();

  /**
   * Deserializes the Rocket.Chat custom alias object. If the structure of the alias object is the
   * representation only of the old {@link ForwardMessageDTO}, then the deserialization transforms
   * the old {@link ForwardMessageDTO} into the current used {@link AliasMessageDTO} containing the
   * {@link ForwardMessageDTO}. Otherwise the whole new {@link AliasMessageDTO} containing a {@link
   * ForwardMessageDTO} or a {@link VideoCallMessageDTO} will be transformed.
   *
   * @param jsonParser the json parser object containing the source object as a string
   * @param context    the current context
   * @return the generated/deserialized {@link AliasMessageDTO}
   */
  @Override
  public AliasMessageDTO deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException {

    String aliasValue = jsonParser.getValueAsString();
    if (StringUtils.isBlank(aliasValue)) {
      return null;
    }

    Optional<ForwardMessageDTO> forwardMessageDTO =
        aliasMessageConverter.convertStringToForwardMessageDTO(aliasValue);

    if (forwardMessageDTO.isPresent()) {
      return buildAliasMessageDTOByOldForwardDTO(forwardMessageDTO.get());
    }

    return buildAliasMessageDTOWithPossibleVideoCallMessageDTO(aliasValue);
  }

  private AliasMessageDTO buildAliasMessageDTOByOldForwardDTO(ForwardMessageDTO forwardMessageDTO) {
    forwardMessageDTO
        .setUsername(usernameTranscoder.decodeUsername(forwardMessageDTO.getUsername()));
    return new AliasMessageDTO().forwardMessageDTO(forwardMessageDTO);
  }

  private AliasMessageDTO buildAliasMessageDTOWithPossibleVideoCallMessageDTO(String aliasValue) {
    AliasMessageDTO alias = aliasMessageConverter.convertStringToAliasMessageDTO(aliasValue)
        .orElse(null);
    if (nonNull(alias)) {
      decodeUsernameOfForwardMessageDTOIfNonNull(alias);
      decodeUsernameOfVideoCallMessageDTOIfNonNull(alias);
    }
    return alias;
  }

  private void decodeUsernameOfForwardMessageDTOIfNonNull(AliasMessageDTO alias) {
    if (nonNull(alias.getForwardMessageDTO())) {
      alias.getForwardMessageDTO()
          .setUsername(
              usernameTranscoder.decodeUsername(alias.getForwardMessageDTO().getUsername()));
    }
  }

  private void decodeUsernameOfVideoCallMessageDTOIfNonNull(AliasMessageDTO alias) {
    if (nonNull(alias.getVideoCallMessageDTO())) {
      alias.getVideoCallMessageDTO().setInitiatorUserName(
          usernameTranscoder.decodeUsername(alias.getVideoCallMessageDTO().getInitiatorUserName()));
    }
  }

}


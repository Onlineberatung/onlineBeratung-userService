package de.caritas.cob.UserService.api.model;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POST MasterKey model
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "PostMasterKey")
public class MasterKeyDTO {

  @NotBlank(message = "{message.api.master.key.notBlank}")
  @ApiModelProperty(required = true, example = "sdj8wnFNASj324!ksldf9", position = 0)
  @JsonProperty("masterKey")
  private String masterKey;
}

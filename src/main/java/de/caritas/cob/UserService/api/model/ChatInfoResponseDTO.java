package de.caritas.cob.UserService.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the chat information for a user
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "ChatInfo")
public class ChatInfoResponseDTO {

  @ApiModelProperty(example = "153918", position = 0)
  private Long id;

  @ApiModelProperty(example = "xGklslk2JJKK", position = 1)
  private String groupId;

  @ApiModelProperty(required = true, example = "false", position = 2)
  private boolean active;

}

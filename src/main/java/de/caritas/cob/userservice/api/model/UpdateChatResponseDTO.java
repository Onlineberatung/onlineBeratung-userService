package de.caritas.cob.userservice.api.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response model class for a 200 ok on PUT /users/chat/{chatId}/update
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "UpdateChatResponse")
public class UpdateChatResponseDTO {
  private String groupId;
  private String chatLink;
}

package de.caritas.cob.userservice.api.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response model class for a 201 Created on POST /users/chat/new
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "CreateChatResponse")
public class CreateChatResponseDTO {
  private String groupId;
  private String chatLink;
}

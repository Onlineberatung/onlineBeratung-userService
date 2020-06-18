package de.caritas.cob.UserService.api.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the chat members
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "ChatMembers")
public class ChatMembersResponseDTO {

  private ChatMemberResponseDTO[] members;

}

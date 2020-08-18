package de.caritas.cob.userservice.api.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a chat member
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "ChatMember")
public class ChatMemberResponseDTO {

  private String _id;
  private String status;
  private String username;
  private String name;
  private String utcOffset;

}

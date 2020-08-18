package de.caritas.cob.userservice.api.model.messageService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Message model for the MessageService helper class
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageDTO {

  private String message;
}

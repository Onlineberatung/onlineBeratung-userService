package de.caritas.cob.UserService.api.model.rocketChat.login;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.caritas.cob.UserService.api.helper.EmptyObjectSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Special DTO for ldap login in Rocket.Chat
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LdapLoginDTO {

  String username;
  String ldapPass;
  Boolean ldap;
  @JsonSerialize(using = EmptyObjectSerializer.class)
  Object ldapOptions = new Object();

}

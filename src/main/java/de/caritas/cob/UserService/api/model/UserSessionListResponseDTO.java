package de.caritas.cob.UserService.api.model;

import java.util.List;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Represents the session list for a user
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "UserSessionList")
public class UserSessionListResponseDTO {

  List<UserSessionResponseDTO> sessions;

}

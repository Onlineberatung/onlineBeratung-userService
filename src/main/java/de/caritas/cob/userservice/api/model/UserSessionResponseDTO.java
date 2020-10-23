package de.caritas.cob.userservice.api.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.caritas.cob.userservice.api.model.chat.UserChatDTO;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Represents a session for a user
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "UserSession")
public class UserSessionResponseDTO {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private SessionDTO session;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private UserChatDTO chat;
  private AgencyDTO agency;
  private SessionConsultantForUserDTO consultant;

  @JsonIgnore
  private Date latestMessage;


  public UserSessionResponseDTO(SessionDTO session, AgencyDTO agency,
      SessionConsultantForUserDTO consultant) {
    this.session = session;
    this.agency = agency;
    this.consultant = consultant;
  }
}

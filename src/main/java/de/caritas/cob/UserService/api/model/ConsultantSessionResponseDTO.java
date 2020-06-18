package de.caritas.cob.UserService.api.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.caritas.cob.UserService.api.model.chat.UserChatDTO;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "ConsultantSession")
public class ConsultantSessionResponseDTO {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private SessionDTO session;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private UserChatDTO chat;
  private SessionUserDTO user;
  private SessionConsultantForConsultantDTO consultant;

  @JsonIgnore
  private Date latestMessage;


  public ConsultantSessionResponseDTO(SessionDTO session, SessionUserDTO user,
      SessionConsultantForConsultantDTO consultant) {
    this.session = session;
    this.user = user;
    this.consultant = consultant;
  }

  public ConsultantSessionResponseDTO(UserChatDTO chat,
      SessionConsultantForConsultantDTO consultant) {
    this.chat = chat;
    this.consultant = consultant;
  }
}

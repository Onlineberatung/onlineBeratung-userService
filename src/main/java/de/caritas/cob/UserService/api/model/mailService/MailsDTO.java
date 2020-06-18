package de.caritas.cob.UserService.api.model.mailService;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MailsDTO {

  private List<MailDTO> mails;

}

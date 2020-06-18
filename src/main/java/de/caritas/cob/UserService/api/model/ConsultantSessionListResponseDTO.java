package de.caritas.cob.UserService.api.model;

import java.util.List;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "ConsultantSessionList")
public class ConsultantSessionListResponseDTO {

  List<ConsultantSessionResponseDTO> sessions;
  private int offset;
  private int count;
  private int total;

}

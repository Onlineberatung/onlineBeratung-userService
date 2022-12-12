package de.caritas.cob.userservice.api.adapters.rocketchat.dto.message;

import java.util.List;
import lombok.Data;

@Data
public class MethodMessageWithParamList {

  private String msg = "method";

  private int id;

  private String method;

  private List<String> params;
}

package de.caritas.cob.userservice.api.adapters.rocketchat.dto.message;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class Message {

  private String msg;

  private int id;

  private String method;

  private List<Map<String, String>> params;
}

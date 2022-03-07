package de.caritas.cob.userservice.api.adapters.rocketchat.dto;

import java.util.Map;
import lombok.Data;

@Data
public class Message {

  private String msg;

  private int id;

  private String method;

  private Map<String, String> params;
}

package de.caritas.cob.userservice.api.adapters.rocketchat.dto;

import lombok.Data;

@Data
public class UpdateUser {

  private String userId;

  private User data;
}

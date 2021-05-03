package de.caritas.cob.userservice.api.conversation.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageableListRequest {

  private final String rcToken;
  private final Integer offset;
  private final Integer count;

}

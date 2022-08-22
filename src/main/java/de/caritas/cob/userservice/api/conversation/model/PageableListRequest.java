package de.caritas.cob.userservice.api.conversation.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageableListRequest {

  private final Integer offset;
  private final Integer count;
  private final String rcToken;
}

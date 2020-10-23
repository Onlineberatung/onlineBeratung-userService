package de.caritas.cob.userservice.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ApiModel(value = "Session")
public class SessionDTO {

  @ApiModelProperty(example = "153918", position = 0)
  private Long id;
  @ApiModelProperty(example = "100", position = 1)
  private Long agencyId;
  @ApiModelProperty(example = "0", position = 2)
  private int consultingType;
  @ApiModelProperty(example = "1", position = 3)
  private int status;
  @ApiModelProperty(example = "88046", position = 4)
  private String postcode;
  @ApiModelProperty(example = "xGklslk2JJKK", position = 5)
  private String groupId;
  @ApiModelProperty(example = "sdjfsd8fg9qhwe", position = 6)
  private String feedbackGroupId;
  @ApiModelProperty(example = "8ertjlasdKJA", position = 7)
  private String askerRcId;
  @ApiModelProperty(example = "Thanks for the answer", position = 8)
  private String lastMessage;
  @ApiModelProperty(example = "1539184948", position = 9)
  private Long messageDate;
  @ApiModelProperty(example = "false", position = 10)
  private boolean messagesRead;
  @ApiModelProperty(example = "false", position = 11)
  private boolean feedbackRead;
  @ApiModelProperty(example = "false", position = 12)
  private boolean isTeamSession;
  @ApiModelProperty(example = "false", position = 13)
  private boolean monitoring;
  @ApiModelProperty(position = 14)
  private SessionAttachmentDTO attachment;

  public SessionDTO(Long id, Long agencyId, int consultingType, int status, String postcode,
      String groupId, String feedbackGroupId, String askerRcId, Long messageDate,
      boolean isTeamSession, boolean isMonitoring) {
    this.id = id;
    this.agencyId = agencyId;
    this.consultingType = consultingType;
    this.status = status;
    this.postcode = postcode;
    this.groupId = groupId;
    this.feedbackGroupId = feedbackGroupId;
    this.askerRcId = askerRcId;
    this.messageDate = messageDate;
    this.isTeamSession = isTeamSession;
    this.monitoring = isMonitoring;
  }

}

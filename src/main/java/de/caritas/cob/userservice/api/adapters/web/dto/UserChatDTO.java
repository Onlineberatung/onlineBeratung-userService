package de.caritas.cob.userservice.api.adapters.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Represents the chat for the user
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "UserChat")
public class UserChatDTO {

  @ApiModelProperty(example = "153918", position = 0)
  private Long id;

  @ApiModelProperty(example = "Drugs", position = 1)
  private String topic;

  @ApiModelProperty(required = true, example = "2019-10-23", position = 2)
  private LocalDate startDate;

  @ApiModelProperty(required = true, example = "12:05", position = 3)
  private LocalTime startTime;

  @ApiModelProperty(required = true, example = "120", position = 4)
  private int duration;

  @ApiModelProperty(required = true, example = "true", position = 5)
  private boolean repetitive;

  @ApiModelProperty(required = true, example = "false", position = 6)
  private boolean active;

  @ApiModelProperty(required = true, example = "0", position = 7)
  private Integer consultingType;

  @ApiModelProperty(example = "Thanks for the answer", position = 8)
  private String lastMessage;

  @ApiModelProperty(example = "1539184948", position = 9)
  private Long messageDate;

  @ApiModelProperty(example = "false", position = 10)
  private boolean messagesRead;

  @ApiModelProperty(example = "xGklslk2JJKK", position = 11)
  private String groupId;

  @ApiModelProperty(position = 12)
  private SessionAttachmentDTO attachment;

  @ApiModelProperty(example = "false", position = 13)
  private boolean subscribed;

  @ApiModelProperty(example = "ajsasdkjsdfkj3, 23njds9f8jhi", position = 14)
  private String[] moderators;

  @JsonIgnore private LocalDateTime startDateWithTime;

  @ApiModelProperty private LastMessageDTO e2eLastMessage;

  @ApiModelProperty private String createdAt;

  @ApiModelProperty private List<AgencyDTO> assignedAgencies;

  @ApiModelProperty private String hintMessage;
}

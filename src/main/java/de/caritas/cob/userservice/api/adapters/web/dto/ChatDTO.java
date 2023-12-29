package de.caritas.cob.userservice.api.adapters.web.dto;

import static de.caritas.cob.userservice.api.helper.UserHelper.CHAT_MAX_DURATION;
import static de.caritas.cob.userservice.api.helper.UserHelper.CHAT_MIN_DURATION;
import static de.caritas.cob.userservice.api.helper.UserHelper.CHAT_TOPIC_MAX_LENGTH;
import static de.caritas.cob.userservice.api.helper.UserHelper.CHAT_TOPIC_MIN_LENGTH;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

/** Create new chat model */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ApiModel(value = "Chat")
public class ChatDTO {

  @Size(min = CHAT_TOPIC_MIN_LENGTH, max = CHAT_TOPIC_MAX_LENGTH)
  @NotBlank(message = "{chat.name.notBlank}")
  @ApiModelProperty(required = true, example = "Wöchentliche Drogenberatung", position = 0)
  @JsonProperty("topic")
  private String topic;

  @DateTimeFormat(iso = ISO.DATE)
  @NotNull(message = "{chat.startDate.invalid}")
  @ApiModelProperty(required = true, example = "2019-10-23", position = 1)
  @JsonProperty("startDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate startDate;

  @DateTimeFormat(pattern = "HH:mm")
  @NotNull(message = "{chat.startTime.invalid}")
  @ApiModelProperty(required = true, example = "12:05", position = 2)
  @JsonProperty("startTime")
  private LocalTime startTime;

  @NotNull(message = "{chat.duration.notNull}")
  @Min(value = CHAT_MIN_DURATION, message = "{chat.duration.invalid}")
  @Max(value = CHAT_MAX_DURATION, message = "{chat.duration.invalid}")
  @ApiModelProperty(required = true, example = "120", position = 3)
  @JsonProperty("duration")
  private int duration;

  @NotNull(message = "{chat.repetitive.notNull}")
  @ApiModelProperty(required = true, example = "true", position = 4)
  @JsonProperty("repetitive")
  private boolean repetitive;

  @ApiModelProperty(required = true, example = "5", position = 5)
  @Min(value = 0, message = "{chat.agencyId.invalid}")
  @JsonProperty("agencyId")
  private Long agencyId;

  @ApiModelProperty(required = true, example = "5", position = 6)
  @Length(max = 300, message = "{chat.hintMessage.invalid}")
  @JsonProperty("hintMessage")
  private String hintMessage;

  @Override
  public String toString() {
    return "ChatDTO [topic="
        + topic
        + ", agencyId="
        + agencyId
        + ", startDate="
        + startDate
        + ", startTime="
        + startTime
        + ", duration="
        + duration
        + ", repetitive="
        + repetitive
        + ", agencyId="
        + agencyId
        + ", hintMessage="
        + hintMessage
        + "]";
  }
}

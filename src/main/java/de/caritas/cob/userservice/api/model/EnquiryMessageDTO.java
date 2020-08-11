package de.caritas.cob.userservice.api.model;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "EnquiryMessage")
public class EnquiryMessageDTO {

  @NotBlank(message = "{enquiry.message.notBlank}")
  @ApiModelProperty(required = true, position = 0,
      example = "Lorem ipsum dolor sit amet, consetetur...")
  @JsonProperty("message")
  private String message;
}

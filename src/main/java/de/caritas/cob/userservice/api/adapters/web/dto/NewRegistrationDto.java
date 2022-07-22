package de.caritas.cob.userservice.api.adapters.web.dto;

import static de.caritas.cob.userservice.api.helper.UserHelper.AGENCY_ID_MAX;
import static de.caritas.cob.userservice.api.helper.UserHelper.AGENCY_ID_MIN;
import static de.caritas.cob.userservice.api.helper.UserHelper.CONSULTING_TYPE_REGEXP;
import static de.caritas.cob.userservice.api.helper.UserHelper.VALID_POSTCODE_REGEX;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Model for new consulting type registrations
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "NewRegistration")
@ToString
public class NewRegistrationDto implements UserRegistrationDTO {

  @NotBlank(message = "{user.custom.postcode.notNull}")
  @NotNull(message = "{user.custom.postcode.notNull}")
  @Pattern(regexp = VALID_POSTCODE_REGEX, message = "{user.custom.postcode.invalid}")
  @ApiModelProperty(required = true, example = "\"79098\"", position = 0)
  @JsonProperty("postcode")
  private String postcode;

  @NotNull(message = "{user.custom.agency.notNull}")
  @Min(value = AGENCY_ID_MIN, message = "{user.custom.agency.invalid}")
  @Max(value = AGENCY_ID_MAX, message = "{user.custom.agency.invalid}")
  @ApiModelProperty(required = true, example = "\"15\"", position = 1)
  @JsonProperty("agencyId")
  private Long agencyId;

  @NotBlank(message = "{user.consultingType.invalid}")
  @NotNull(message = "{user.consultingType.invalid}")
  @Pattern(regexp = CONSULTING_TYPE_REGEXP, message = "{user.consultingType.invalid}")
  @ApiModelProperty(required = true, example = "\"0\"", position = 2)
  @JsonProperty("consultingType")
  private String consultingType;

  @ApiModelProperty(hidden = true)
  private boolean newUserAccount;

  private String consultantId;

  @ApiModelProperty(required = false, example = "\"2\"")
  @JsonProperty("mainTopicId")
  private Integer mainTopicId;

  @ApiModelProperty(required = false, example = "\"MALE\"")
  @JsonProperty("userGender")
  private String userGender;


  @ApiModelProperty(required = false, example = "\"21\"")
  @JsonProperty("userAge")
  private Integer userAge;
}

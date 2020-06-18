package de.caritas.cob.UserService.api.model;

import static de.caritas.cob.UserService.api.helper.UserHelper.ADDICTIVE_DRUGS_REGEXP;
import static de.caritas.cob.UserService.api.helper.UserHelper.AGENCY_ID_MAX;
import static de.caritas.cob.UserService.api.helper.UserHelper.AGENCY_ID_MIN;
import static de.caritas.cob.UserService.api.helper.UserHelper.AGE_REGEXP;
import static de.caritas.cob.UserService.api.helper.UserHelper.CONSULTING_TYPE_REGEXP;
import static de.caritas.cob.UserService.api.helper.UserHelper.GENDER_REGEXP;
import static de.caritas.cob.UserService.api.helper.UserHelper.POSTCODE_MAX;
import static de.caritas.cob.UserService.api.helper.UserHelper.POSTCODE_MIN;
import static de.caritas.cob.UserService.api.helper.UserHelper.RELATION_REGEXP;
import static de.caritas.cob.UserService.api.helper.UserHelper.STATE_REGEXP;
import static de.caritas.cob.UserService.api.helper.UserHelper.TERMS_ACCEPTED_REGEXP;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.caritas.cob.UserService.api.model.jsonDeserializer.EncodeUsernameJsonDeserializer;
import de.caritas.cob.UserService.api.model.jsonDeserializer.UrlDecodePasswordJsonDeserializer;
import de.caritas.cob.UserService.api.model.validation.ValidAge;
import de.caritas.cob.UserService.api.model.validation.ValidPostcode;
import de.caritas.cob.UserService.api.model.validation.ValidState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User model
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "User")
@ValidAge
@ValidState
@ValidPostcode
public class UserDTO {

  @NotBlank(message = "{user.username.notBlank}")
  @ApiModelProperty(required = true, example = "max94", position = 0)
  @JsonDeserialize(using = EncodeUsernameJsonDeserializer.class)
  @JsonProperty("username")
  private String username;

  @NotNull(message = "{user.custom.postcode.notNull}")
  @Min(value = POSTCODE_MIN, message = "{user.custom.postcode.invalid}")
  @Max(value = POSTCODE_MAX, message = "{user.custom.postcode.invalid}")
  @ApiModelProperty(required = true, example = "\"79098\"", position = 1)
  @JsonProperty("postcode")
  private String postcode;

  @NotNull(message = "{user.custom.agency.notNull}")
  @Min(value = AGENCY_ID_MIN, message = "{user.custom.agency.invalid}")
  @Max(value = AGENCY_ID_MAX, message = "{user.custom.agency.invalid}")
  @ApiModelProperty(required = true, example = "\"15\"", position = 2)
  @JsonProperty("agencyId")
  private Long agencyId;

  @NotBlank(message = "{user.password.notBlank}")
  @ApiModelProperty(required = true, example = "pass@w0rd", position = 3)
  @JsonDeserialize(using = UrlDecodePasswordJsonDeserializer.class)
  @JsonProperty("password")
  private String password;

  @JsonInclude(value = Include.NON_NULL)
  @Email(message = "{user.email.invalid}")
  @ApiModelProperty(required = false, example = "max@mustermann.de", position = 4)
  @JsonProperty("email")
  private String email;

  @JsonInclude(value = Include.NON_NULL)
  @Pattern(regexp = ADDICTIVE_DRUGS_REGEXP, message = "{user.custom.addictiveDrugs.invalid}")
  @ApiModelProperty(required = false, example = "\"2,4\"", position = 5)
  @JsonProperty("addictiveDrugs")
  private String addictiveDrugs;

  @JsonInclude(value = Include.NON_NULL)
  @Pattern(regexp = RELATION_REGEXP, message = "{user.custom.relation.invalid}")
  @ApiModelProperty(required = false, example = "\"2\"", position = 6)
  @JsonProperty("relation")
  private String relation;

  @JsonInclude(value = Include.NON_NULL)
  @Pattern(regexp = AGE_REGEXP, message = "{user.custom.age.invalid}")
  @ApiModelProperty(required = false, example = "1", position = 7)
  @JsonProperty("age")
  private String age;

  @JsonInclude(value = Include.NON_NULL)
  @Pattern(regexp = GENDER_REGEXP, message = "{user.custom.gender.invalid}")
  @ApiModelProperty(required = false, example = "\"1\"", position = 8)
  @JsonProperty("gender")
  private String gender;

  @JsonInclude(value = Include.NON_NULL)
  @Pattern(regexp = STATE_REGEXP, message = "{user.custom.state.invalid}")
  @JsonProperty("state")
  @ApiModelProperty(required = false, example = "\"16\"", position = 9)
  private String state;

  @Pattern(regexp = TERMS_ACCEPTED_REGEXP, message = "{user.custom.termsAccepted.invalid}")
  @ApiModelProperty(required = true, example = "\"true\"", position = 10)
  @JsonProperty("termsAccepted")
  private String termsAccepted;

  @Pattern(regexp = CONSULTING_TYPE_REGEXP, message = "{user.consultingType.invalid}")
  @ApiModelProperty(required = true, example = "\"0\"", position = 11)
  @JsonProperty("consultingType")
  private String consultingType;

  public UserDTO(String email) {
    this.email = email;
  }

  public UserDTO(String username, String postcode, Long agencyId, String password, String email,
      String termsAccepted, String consultingType) {
    this.username = username;
    this.postcode = postcode;
    this.agencyId = agencyId;
    this.password = password;
    this.email = email;
    this.termsAccepted = termsAccepted;
    this.consultingType = consultingType;
  }

  public UserDTO(String age, String state, String consultingType) {
    this.age = age;
    this.state = state;
    this.consultingType = consultingType;
  }

}

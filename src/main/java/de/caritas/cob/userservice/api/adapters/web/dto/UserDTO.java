package de.caritas.cob.userservice.api.adapters.web.dto;

import static de.caritas.cob.userservice.api.helper.UserHelper.AGENCY_ID_MAX;
import static de.caritas.cob.userservice.api.helper.UserHelper.AGENCY_ID_MIN;
import static de.caritas.cob.userservice.api.helper.UserHelper.AGE_REGEXP;
import static de.caritas.cob.userservice.api.helper.UserHelper.CONSULTING_TYPE_REGEXP;
import static de.caritas.cob.userservice.api.helper.UserHelper.REFERER_REGEXP;
import static de.caritas.cob.userservice.api.helper.UserHelper.STATE_REGEXP;
import static de.caritas.cob.userservice.api.helper.UserHelper.TERMS_ACCEPTED_REGEXP;
import static de.caritas.cob.userservice.api.helper.UserHelper.VALID_POSTCODE_REGEX;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.web.dto.serialization.EncodeUsernameJsonDeserializer;
import de.caritas.cob.userservice.api.adapters.web.dto.serialization.UrlDecodePasswordJsonDeserializer;
import de.caritas.cob.userservice.api.adapters.web.dto.validation.ValidAge;
import de.caritas.cob.userservice.api.adapters.web.dto.validation.ValidState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Collection;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/** User model */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "User")
@ValidAge
@ValidState
@Builder
public class UserDTO implements UserRegistrationDTO {

  @NotBlank(message = "{user.username.notBlank}")
  @NotNull(message = "{user.username.notBlank}")
  @ApiModelProperty(required = true, example = "max94")
  @JsonDeserialize(using = EncodeUsernameJsonDeserializer.class)
  @JsonProperty("username")
  private String username;

  @NotBlank(message = "{user.custom.postcode.notNull}")
  @NotNull(message = "{user.custom.postcode.notNull}")
  @Pattern(regexp = VALID_POSTCODE_REGEX, message = "{user.custom.postcode.invalid}")
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
  @ApiModelProperty(example = "max@mustermann.de", position = 4)
  @JsonProperty("email")
  private String email;

  @JsonInclude(value = Include.NON_NULL)
  @Pattern(regexp = AGE_REGEXP, message = "{user.custom.age.invalid}")
  @ApiModelProperty(example = "1", position = 7)
  @JsonProperty("age")
  private String age;

  @JsonInclude(value = Include.NON_NULL)
  @Pattern(regexp = STATE_REGEXP, message = "{user.custom.state.invalid}")
  @JsonProperty("state")
  @ApiModelProperty(example = "\"16\"", position = 9)
  private String state;

  @Pattern(regexp = TERMS_ACCEPTED_REGEXP, message = "{user.custom.termsAccepted.invalid}")
  @ApiModelProperty(required = true, example = "\"true\"", position = 10)
  @JsonProperty("termsAccepted")
  private String termsAccepted;

  @Pattern(regexp = CONSULTING_TYPE_REGEXP, message = "{user.consultingType.invalid}")
  @ApiModelProperty(required = true, example = "\"0\"", position = 11)
  @JsonProperty("consultingType")
  private String consultingType;

  @JsonProperty("consultantId")
  private String consultantId;

  private boolean newUserAccount;

  @ApiModelProperty(required = false, example = "\"1\"", position = 12)
  @JsonProperty("tenantId")
  private Long tenantId;

  @ApiModelProperty(required = false, example = "\"2\"", position = 13)
  @JsonProperty("mainTopicId")
  private Long mainTopicId;

  @ApiModelProperty(required = false, example = "\"MALE\"", position = 14)
  @JsonProperty("gender")
  private String userGender;

  @ApiModelProperty(required = false, example = "\"[1,5]\"")
  @JsonProperty("topicIds")
  private Collection<Long> topicIds = Lists.newArrayList();

  @ApiModelProperty(required = false, example = "\"RELATIVE_COUNSELLING\"")
  @JsonProperty("counsellingRelation")
  private String counsellingRelation;

  private LanguageCode preferredLanguage;

  @ApiModelProperty(required = false, example = "\"referer\"")
  @Pattern(regexp = REFERER_REGEXP, message = "{user.custom.referer.invalid}")
  @JsonProperty("referer")
  private String referer;

  public Integer getUserAge() {
    return StringUtils.isNumeric(age) ? Integer.valueOf(age) : null;
  }

  public UserDTO(String email) {
    this.email = email;
  }

  public UserDTO(
      String username,
      String postcode,
      Long agencyId,
      String password,
      String email,
      String termsAccepted,
      String consultingTypeId) {
    this.username = username;
    this.postcode = postcode;
    this.agencyId = agencyId;
    this.password = password;
    this.email = email;
    this.termsAccepted = termsAccepted;
    this.consultingType = consultingTypeId;
  }

  public UserDTO(String age, String state, String consultingType) {
    this.age = age;
    this.state = state;
    this.consultingType = consultingType;
  }

  @JsonIgnore
  public boolean isConsultantSet() {
    return isNotBlank(consultantId);
  }

  @Override
  public String toString() {
    return "UserDTO{"
        + "username='"
        + username
        + '\''
        + ", postcode='"
        + postcode
        + '\''
        + ", agencyId="
        + agencyId
        + ", age='"
        + age
        + '\''
        + ", state='"
        + state
        + '\''
        + ", termsAccepted='"
        + termsAccepted
        + '\''
        + ", consultingType='"
        + consultingType
        + '\''
        + ", tenantId='"
        + tenantId
        + '\''
        + ", mainTopicId='"
        + mainTopicId
        + '\''
        + ", gender='"
        + userGender
        + '\''
        + ", topicIds='"
        + topicIds
        + '\''
        + ", counsellingRelation='"
        + counsellingRelation
        + '\''
        + '}';
  }
}

package de.caritas.cob.userservice.api.adapters.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.caritas.cob.userservice.api.adapters.web.dto.serialization.DecodeUsernameJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
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
@ApiModel(value = "UserData")
public class UserDataResponseDTO {

  @ApiModelProperty(example = "ajsd89-sdf9-sadk-as8j-asdf8jo")
  private String userId;

  @ApiModelProperty(example = "max.muster", position = 1)
  @JsonSerialize(using = DecodeUsernameJsonSerializer.class)
  private String userName;

  @ApiModelProperty(example = "Max", position = 2)
  private String firstName;

  @ApiModelProperty(example = "Mustermann", position = 3)
  private String lastName;

  @ApiModelProperty(example = "maxmuster@mann.com", position = 4)
  private String email;

  @ApiModelProperty(example = "true", position = 5)
  private boolean isAbsent;

  @ApiModelProperty(example = "true", position = 6)
  private boolean isFormalLanguage;

  @ApiModelProperty(position = 7)
  private Set<String> languages;

  @ApiModelProperty(example = "Bin mal weg...", position = 8)
  private String absenceMessage;

  @ApiModelProperty(example = "true", position = 9)
  private boolean isInTeamAgency;

  @ApiModelProperty(position = 10)
  private List<AgencyDTO> agencies;

  @ApiModelProperty(position = 11)
  private Set<String> userRoles;

  @ApiModelProperty(position = 12)
  private Set<String> grantedAuthorities;

  private LinkedHashMap<String, Object> consultingTypes;
  private boolean hasAnonymousConversations;
  private boolean hasArchive;
  private TwoFactorAuthDTO twoFactorAuth;
  private String displayName;
  private Boolean isDisplayNameEditable;

  @JsonIgnore private Boolean encourage2fa;

  private Boolean e2eEncryptionEnabled;

  private Boolean isWalkThroughEnabled;

  private Set<EmailToggle> emailToggles;

  private Boolean appointmentFeatureEnabled;

  private LanguageCode preferredLanguage;

  private LocalDateTime termsAndConditionsConfirmation;

  private LocalDateTime dataPrivacyConfirmation;

  private Boolean available;

  private EmailNotificationsDTO emailNotifications;

  private Set<SessionDTO> sessions;
}

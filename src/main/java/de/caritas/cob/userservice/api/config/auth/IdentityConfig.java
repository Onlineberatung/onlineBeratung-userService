package de.caritas.cob.userservice.api.config.auth;

import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import java.util.Arrays;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.util.UriComponentsBuilder;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "identity")
public class IdentityConfig implements IdentityClientConfig {

  private static final char PATH_SEPARATOR = '/';

  @NotBlank private String errorMessageDuplicatedEmail;

  @NotBlank private String errorMessageDuplicatedUsername;

  @URL private String openidConnectUrl;

  @URL private String otpUrl;

  @NotNull private Boolean otpAllowedForUsers;

  @NotNull private Boolean otpAllowedForConsultants;

  @NotNull private Boolean otpAllowedForRestrictedAgencyAdmins;

  @NotNull private Boolean otpAllowedForSingleTenantAdmins;

  @NotNull private Boolean otpAllowedForTenantSuperAdmins;

  @NotNull private Boolean displayNameAllowedForConsultants;

  @NotNull private TechnicalUserConfig technicalUser;

  @NotBlank
  @Pattern(regexp = "^@\\S+$")
  private String emailDummySuffix;

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  public String getOpenIdConnectUrl(String path) {
    return getOpenIdConnectUrl(path, "");
  }

  public String getOpenIdConnectUrl(String path, String arg) {
    var builder = UriComponentsBuilder.fromUriString(openidConnectUrl);
    addPath(path, arg, builder);

    return builder.toUriString();
  }

  public String getOtpUrl(String path, String arg) {
    var builder = UriComponentsBuilder.fromUriString(otpUrl);
    addPath(path, arg, builder);

    return builder.toUriString();
  }

  private void addPath(String path, String arg, UriComponentsBuilder builder) {
    Arrays.stream(
            StringUtils.trimLeadingCharacter(path, PATH_SEPARATOR)
                .replaceAll("(\\{).*(})", arg)
                .split(Character.toString(PATH_SEPARATOR)))
        .forEach(builder::pathSegment);
  }

  @Override
  public boolean isOtpAllowed(@NotNull Set<String> roles) {
    return roles.contains(UserRole.USER.getValue()) && otpAllowedForUsers
        || roles.contains(UserRole.CONSULTANT.getValue()) && otpAllowedForConsultants
        || roles.contains(UserRole.TENANT_ADMIN.getValue()) && otpAllowedForTenantSuperAdmins
        || roles.contains(UserRole.SINGLE_TENANT_ADMIN.getValue())
            && otpAllowedForSingleTenantAdmins
        || roles.contains(UserRole.RESTRICTED_AGENCY_ADMIN.getValue())
            && otpAllowedForRestrictedAgencyAdmins;
  }
}

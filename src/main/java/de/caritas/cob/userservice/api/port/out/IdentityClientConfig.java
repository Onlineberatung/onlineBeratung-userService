package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.config.auth.TechnicalUserConfig;
import java.util.Set;
import javax.validation.constraints.NotNull;

public interface IdentityClientConfig {

  String getErrorMessageDuplicatedEmail();

  String getErrorMessageDuplicatedUsername();

  String getOpenidConnectUrl();

  String getOpenIdConnectUrl(String path);

  String getOpenIdConnectUrl(String path, String arg);

  String getOtpUrl(String path, String arg);

  Boolean getOtpAllowedForUsers();

  Boolean getOtpAllowedForConsultants();

  Boolean getOtpAllowedForSingleTenantAdmins();

  Boolean getOtpAllowedForTenantSuperAdmins();

  Boolean getDisplayNameAllowedForConsultants();

  TechnicalUserConfig getTechnicalUser();

  String getEmailDummySuffix();

  boolean isOtpAllowed(@NotNull Set<String> roles);
}

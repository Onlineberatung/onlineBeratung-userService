package de.caritas.cob.userservice.api.port.out;

import java.util.Set;
import javax.validation.constraints.NotNull;

public interface IdentityClientConfig {

  String getErrorMessageDuplicatedEmail();

  String getErrorMessageDuplicatedUsername();

  String getOpenidConnectUrl();

  String getOpenIdConnectUrl(String path);

  String getOpenIdConnectUrl(String path, String arg);

  String getOtpUrl();

  String getOtpUrl(String path);

  String getOtpUrl(String path, String arg);

  Boolean getOtpAllowedForUsers();

  Boolean getOtpAllowedForConsultants();

  boolean isOtpAllowed(@NotNull Set<String> roles);
}

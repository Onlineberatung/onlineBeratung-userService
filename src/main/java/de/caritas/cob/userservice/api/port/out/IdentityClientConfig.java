package de.caritas.cob.userservice.api.port.out;

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
}

package de.caritas.cob.userservice.api.port.out;

public interface IdentityClientConfig {

  String getErrorMessageDuplicatedEmail();

  String getErrorMessageDuplicatedUsername();

  String getLoginUrl();

  String getLogoutUrl();

  String getOtpInfoUrl();

  String getOtpSetupUrl();

  String getOtpTeardownUrl();

  Boolean getOtpAllowedForUsers();

  Boolean getOtpAllowedForConsultants();
}

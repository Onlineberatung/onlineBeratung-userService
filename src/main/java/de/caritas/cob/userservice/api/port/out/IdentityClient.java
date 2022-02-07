package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import java.util.Optional;

public interface IdentityClient {

  boolean changePassword(final String userId, final String password);

  KeycloakLoginResponseDTO loginUser(final String userName, final String password);

  boolean logoutUser(final String refreshToken);

  void changeEmailAddress(String emailAddress);

  void deleteEmailAddress();

  Optional<OtpInfoDTO> getOtpCredential(final String userName);

  void setUpOtpCredential(final String userName, final OtpSetupDTO otpSetupDTO);

  void deleteOtpCredential(final String userName);
}

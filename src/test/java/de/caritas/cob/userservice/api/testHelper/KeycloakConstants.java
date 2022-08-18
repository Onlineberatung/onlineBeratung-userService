package de.caritas.cob.userservice.api.testHelper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import java.util.ArrayList;
import java.util.List;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;

public class KeycloakConstants {

  public static final List<UserRepresentation> EMPTY_USER_REPRESENTATION_LIST = new ArrayList<>();
  public static final UserRepresentation USER_REPRESENTATION_WITH_ENCODED_USERNAME =
      new UserRepresentation() {
        {
          setUsername(USERNAME);
        }
      };
  public static final KeycloakCreateUserResponseDTO KEYCLOAK_CREATE_USER_RESPONSE_DTO_CONFLICT =
      new KeycloakCreateUserResponseDTO(HttpStatus.CONFLICT);
  public static final KeycloakCreateUserResponseDTO KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITH_USER_ID =
      new KeycloakCreateUserResponseDTO(USER_ID);
  public static final KeycloakCreateUserResponseDTO
      KEYCLOAK_CREATE_USER_RESPONSE_DTO_WITHOUT_USER_ID =
          new KeycloakCreateUserResponseDTO(HttpStatus.OK);
}

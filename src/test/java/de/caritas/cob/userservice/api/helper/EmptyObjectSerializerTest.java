package de.caritas.cob.userservice.api.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LdapLoginDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EmptyObjectSerializerTest {

  private final String USERNAME = "username";
  private final String PASSWORD = "password";
  private final String EXPECTED_JSON =
      "{\"username\":\""
          + USERNAME
          + "\",\"ldapPass\":\""
          + PASSWORD
          + "\",\"ldap\":true,\"ldapOptions\":\"{}\"}";

  @Test
  public void EmptyObjectSerializer_Should_SerializeEmptyObjectsToCurlyBraces()
      throws JsonProcessingException {

    LdapLoginDTO ldapLoginDTO = new LdapLoginDTO();
    ldapLoginDTO.setLdap(true);
    ldapLoginDTO.setUsername(USERNAME);
    ldapLoginDTO.setLdapPass(PASSWORD);

    ObjectMapper objectMapper = new ObjectMapper();
    String result = objectMapper.writerFor(LdapLoginDTO.class).writeValueAsString(ldapLoginDTO);
    assertEquals(EXPECTED_JSON, result);
  }
}

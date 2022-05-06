package de.caritas.cob.userservice.api.testConfig;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatClient;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatMapper;
import de.caritas.cob.userservice.api.adapters.rocketchat.config.RocketChatConfig;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.DataDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.MeDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class RocketChatTestConfig {

  public static final String AUTH_TOKEN =
      "auth-token configured in " + RocketChatTestConfig.class.getName();

  @Bean
  public RocketChatService rocketChatService(RestTemplate restTemplate,
      RocketChatCredentialsProvider rocketChatCredentialsProvider,
      RocketChatConfig rocketChatConfig, RocketChatClient rocketChatClient,
      RocketChatMapper rocketChatMapper) {
    return new RocketChatService(restTemplate, rocketChatCredentialsProvider, rocketChatClient,
        rocketChatConfig, rocketChatMapper) {
      @Override
      public ResponseEntity<LoginResponseDTO> loginUserFirstTime(String username, String password) {
        var loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setStatus("ok");
        var dataDTO = new DataDTO();
        dataDTO.setUserId("user-id configured in " + RocketChatTestConfig.class.getName());
        dataDTO.setAuthToken(AUTH_TOKEN);
        var meDTO = new MeDTO();
        dataDTO.setMe(meDTO);

        loginResponseDTO.setData(dataDTO);
        return ResponseEntity.ok(loginResponseDTO);
      }

      @Override
      public Optional<GroupResponseDTO> createPrivateGroup(String name,
          RocketChatCredentials rocketChatCredentials) {
        var groupResponseDTO = new GroupResponseDTO();
        groupResponseDTO.setSuccess(true);
        var group = new GroupDTO();
        if (!rocketChatCredentials.getRocketChatToken().equals(AUTH_TOKEN)) {
          group.setId("rcGroupId");
        }
        groupResponseDTO.setGroup(group);
        return Optional.of(groupResponseDTO);
      }

      @Override
      public void deleteGroupAsTechnicalUser(String groupId) {
      }

      @Override
      public void addUserToGroup(String rcUserId, String rcGroupId) {
      }

      @Override
      public void addTechnicalUserToGroup(String rcGroupId) {
      }

      @Override
      public void removeUserFromGroup(String rcUserId, String rcGroupId) {
      }

      @Override
      public void removeTechnicalUserFromGroup(String rcGroupId) {
      }

      @Override
      public void removeAllStandardUsersFromGroup(String rcGroupId) {
      }

      @Override
      public void removeSystemMessages(String rcGroupId, LocalDateTime oldest,
          LocalDateTime latest) {
      }

      @Override
      public void removeAllMessages(String rcGroupId) {
      }

      @Override
      public void deleteUser(String rcUserId) {
      }
    };
  }
}

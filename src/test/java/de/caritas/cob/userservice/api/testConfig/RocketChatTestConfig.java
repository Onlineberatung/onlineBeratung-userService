package de.caritas.cob.userservice.api.testConfig;

import com.mongodb.client.MongoClient;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatClient;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatMapper;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.config.RocketChatConfig;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.DataDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.MeDTO;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
@TestConfiguration
public class RocketChatTestConfig {

  public static final String AUTH_TOKEN =
      "auth-token configured in " + RocketChatTestConfig.class.getName();

  @MockBean MongoClient mongoClient;

  @Bean
  public RocketChatService rocketChatService(
      RestTemplate restTemplate,
      RocketChatCredentialsProvider rocketChatCredentialsProvider,
      RocketChatConfig rocketChatConfig,
      RocketChatClient rocketChatClient,
      MongoClient mongoClient,
      RocketChatMapper rocketChatMapper,
      RocketChatCredentials rocketChatCredentials) {
    return new RocketChatService(
        restTemplate,
        rocketChatCredentialsProvider,
        rocketChatClient,
        mongoClient,
        rocketChatConfig,
        rocketChatMapper,
        rocketChatCredentials) {
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
      public Optional<GroupResponseDTO> createPrivateGroup(
          String name, RocketChatCredentials rocketChatCredentials) {
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
      public void deleteGroupAsTechnicalUser(String groupId) {}

      @Override
      public void addUserToGroup(String rcUserId, String rcGroupId) {}

      @Override
      public void addTechnicalUserToGroup(String rcGroupId) {
        log.info("RocketChatTestConfig.addTechnicalUserToGroup({}) called", rcGroupId);
      }

      @Override
      public void removeUserFromGroup(String rcUserId, String rcGroupId) {
        log.info("RocketChatTestConfig.removeUserFromGroup({},{}) called", rcUserId, rcGroupId);
      }

      @Override
      public void leaveFromGroupAsTechnicalUser(String rcGroupId) {
        log.info("RocketChatTestConfig.leaveFromGroupAsTechnicalUser({}) called", rcGroupId);
      }

      @Override
      public void removeAllStandardUsersFromGroup(String rcGroupId) {}

      @Override
      public void removeSystemMessages(
          String rcGroupId, LocalDateTime oldest, LocalDateTime latest) {}

      @Override
      public void removeAllMessages(String rcGroupId) {}

      @Override
      public void deleteUser(String rcUserId) {}
    };
  }
}

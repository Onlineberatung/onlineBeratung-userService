package de.caritas.cob.userservice.testConfig;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.DataDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.MeDTO;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.testHelper.TestConstants;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class RocketChatTestConfig {

  @Bean
  public RocketChatService rocketChatService(RestTemplate restTemplate,
      RocketChatCredentialsProvider rocketChatCredentialsProvider) {
    return new RocketChatService(restTemplate, rocketChatCredentialsProvider) {
      @Override
      public ResponseEntity<LoginResponseDTO> loginUserFirstTime(String username, String password) {
        var loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setStatus("ok");
        var dataDTO = new DataDTO();
        dataDTO.setUserId("user-id configured in " + RocketChatTestConfig.class.getName());
        dataDTO.setAuthToken("auth-token configured in " + RocketChatTestConfig.class.getName());
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
        groupResponseDTO.setGroup(new GroupDTO());
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

      @Override
      public String getRocketChatUserIdByUsername(String username) {
        return TestConstants.RC_USER_ID;
      }
    };
  }
}

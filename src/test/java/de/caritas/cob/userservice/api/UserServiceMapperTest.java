package de.caritas.cob.userservice.api;

import static org.assertj.core.api.Assertions.assertThat;

import de.caritas.cob.userservice.api.adapters.web.dto.EmailNotificationsDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceMapperTest {

  @InjectMocks private UserServiceMapper userServiceMapper;

  @Mock
  @SuppressWarnings("unused")
  private UsernameTranscoder usernameTranscoder;

  @Test
  void saveWalkThroughEnabled() {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("walkThroughEnabled", true);
    requestData.put("id", "1");
    Consultant consultant = new Consultant();

    userServiceMapper.consultantOf(consultant, requestData);

    assertThat(consultant.getWalkThroughEnabled()).isTrue();
  }

  @Test
  void saveNotificationsEnabled() {
    Map<String, Object> requestData = new HashMap<>();
    NotificationsSettingsDTO allActiveSettings =
        new NotificationsSettingsDTO()
            .appointmentNotificationEnabled(true)
            .reassignmentNotificationEnabled(true)
            .newChatMessageNotificationEnabled(true)
            .appointmentNotificationEnabled(true);
    EmailNotificationsDTO emailNotificationsDTO =
        new EmailNotificationsDTO().emailNotificationsEnabled(true).settings(allActiveSettings);
    requestData.put("emailNotifications", emailNotificationsDTO);
    requestData.put("id", "1");
    Consultant consultant = new Consultant();

    userServiceMapper.consultantOf(consultant, requestData);

    assertThat(consultant.isNotificationsEnabled()).isTrue();
    assertThat(consultant.getNotificationsSettings())
        .isEqualTo(
            "{\"initialEnquiryNotificationEnabled\":false,\"newChatMessageNotificationEnabled\":true,\"reassignmentNotificationEnabled\":true,\"appointmentNotificationEnabled\":true}");
  }

  @Test
  void e2eKeyOfShouldMapIfKeyExists() {
    var map = Map.of("e2eKey", "tmp." + RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertThat(e2eKey).isPresent();
    assertThat(map).containsEntry("e2eKey", e2eKey.get());
  }

  @Test
  void e2eKeyOfShouldNotMapIfKeyFormatIsWrong() {
    var map = Map.of("e2eKey", RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertThat(e2eKey).isNotPresent();
  }

  @Test
  void e2eKeyOfShouldNotMapIfKeyDoesNotExist() {
    var map = Map.of("notE2eKey", RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertThat(e2eKey).isNotPresent();
  }
}

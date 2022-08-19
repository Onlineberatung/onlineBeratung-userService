package de.caritas.cob.userservice.api.adapters.web.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.userservice.api.config.auth.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(LiveProxyController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.profiles.active=testing")
public class LiveProxyControllerIT {

  private static final String LIVE_EVENT_PATH = "/liveproxy/send";

  @Autowired private MockMvc mockMvc;

  @MockBean private LiveEventNotificationService liveEventNotificationService;

  @MockBean private RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper;

  @MockBean private LinkDiscoverers linkDiscoverers;

  @Test
  public void sendLiveEvent_Should_returnBadRequest_When_rcGroupIdIsNotProvided() throws Exception {
    this.mockMvc.perform(post(LIVE_EVENT_PATH)).andExpect(status().isBadRequest());

    verifyNoInteractions(liveEventNotificationService);
  }

  @Test
  public void sendLiveEvent_Should_returnStatusOkAndUseMock_When_rcGroupIdIsProvided()
      throws Exception {
    this.mockMvc.perform(post(LIVE_EVENT_PATH).param("rcGroupId", "id")).andExpect(status().isOk());

    verify(liveEventNotificationService, times(1)).sendLiveDirectMessageEventToUsers(eq("id"));
  }
}

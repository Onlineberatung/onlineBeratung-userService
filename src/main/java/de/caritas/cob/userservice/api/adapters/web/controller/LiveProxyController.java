package de.caritas.cob.userservice.api.adapters.web.controller;

import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.LiveproxyApi;
import io.swagger.annotations.Api;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller to consume live events and send them to the live service. */
@RestController
@RequiredArgsConstructor
@Api(tags = "live-controller")
public class LiveProxyController implements LiveproxyApi {

  private final @NonNull LiveEventNotificationService liveEventNotificationService;

  /**
   * Sends a live event to all relevant users according to the rocket chat group id.
   *
   * @param rcGroupId Rocket Chat group id (required)
   * @return {@link ResponseEntity} with status ok if no error occurs
   */
  @Override
  public ResponseEntity<Void> sendLiveEvent(@RequestParam String rcGroupId) {
    this.liveEventNotificationService.sendLiveDirectMessageEventToUsers(rcGroupId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}

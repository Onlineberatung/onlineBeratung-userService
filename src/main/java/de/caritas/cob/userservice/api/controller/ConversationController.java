package de.caritas.cob.userservice.api.controller;

import de.caritas.cob.userservice.generated.api.conversation.controller.ConversationsApi;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for conversation API requests
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "conversation-controller")
public class ConversationController implements ConversationsApi {

}

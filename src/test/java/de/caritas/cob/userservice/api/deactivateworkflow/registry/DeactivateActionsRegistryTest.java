package de.caritas.cob.userservice.api.deactivateworkflow.registry;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.deactivateworkflow.session.DeactivateSessionAction;
import de.caritas.cob.userservice.api.deactivateworkflow.user.DeactivateKeycloakUserAction;
import de.caritas.cob.userservice.api.deactivateworkflow.user.DeactivateRocketChatUserAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
public class DeactivateActionsRegistryTest {

  @InjectMocks
  private DeactivateActionsRegistry deactivateActionsRegistry;

  @Mock
  private ApplicationContext applicationContext;

  @Test
  void getUserDeactivateActions_Should_useApplicationContextForSpecificClasses() {
    this.deactivateActionsRegistry.getUserDeactivateActions();

    verify(this.applicationContext, times(1)).getBeansOfType(DeactivateRocketChatUserAction.class);
    verify(this.applicationContext, times(1)).getBeansOfType(DeactivateKeycloakUserAction.class);
  }

  @Test
  void getSessionDeactivateActions_Should_useApplicationContextForSpecificClasses() {
    this.deactivateActionsRegistry.getSessionDeactivateActions();

    verify(this.applicationContext, times(1)).getBeansOfType(DeactivateSessionAction.class);
  }

}

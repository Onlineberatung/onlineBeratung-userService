package de.caritas.cob.UserService.api.manager.consultingType;

import de.caritas.cob.UserService.api.manager.consultingType.notifications.Notifications;
import de.caritas.cob.UserService.api.manager.consultingType.registration.Registration;
import de.caritas.cob.UserService.api.manager.consultingType.roles.Roles;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConsultingTypeSettings {

  private ConsultingType consultingType;
  private boolean sendWelcomeMessage;
  private String welcomeMessage;
  private SessionDataInitializing sessionDataInitializing;
  private boolean monitoring;
  private String monitoringFile;
  private boolean feedbackChat;
  private Notifications notifications;
  private boolean languageFormal;
  private Roles roles;
  private Registration registration;

}

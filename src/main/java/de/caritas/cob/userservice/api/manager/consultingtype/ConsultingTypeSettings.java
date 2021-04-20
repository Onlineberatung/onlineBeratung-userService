package de.caritas.cob.userservice.api.manager.consultingtype;

import de.caritas.cob.userservice.api.manager.consultingtype.registration.Registration;
import de.caritas.cob.userservice.api.model.NotificationDTO;
import de.caritas.cob.userservice.api.model.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConsultingTypeSettings {

  private int consultingTypeId;
  private String consultingUrlName;
  private boolean excludeNonMainConsultantsFromTeamSessions;
  private boolean isGroupChat;
  private boolean consultantBoundedToConsultingType;
  private boolean sendWelcomeMessage;
  private String welcomeMessage;
  private boolean sendFurtherStepsMessage;
  private boolean sendSaveSessionDataMessage;
  private SessionDataInitializing sessionDataInitializing;
  private boolean monitoring;
  private String monitoringFile;
  private boolean feedbackChat;
  private NotificationDTO notifications;
  private boolean languageFormal;
  private RoleDTO roles;
  private Registration registration;
}

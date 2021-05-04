package de.caritas.cob.userservice.testHelper;

import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.RegistrationMandatoryFieldsDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.RegistrationDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.RolesDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NewMessageDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.TeamSessionsDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NotificationsDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.MonitoringDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.SessionDataInitializingDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.WelcomeMessageDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.GroupChatDTO;
import de.caritas.cob.userservice.api.manager.consultingtype.SessionDataInitializing;
import de.caritas.cob.userservice.api.manager.consultingtype.registration.Registration;
import de.caritas.cob.userservice.api.model.NotificationDTO;
import de.caritas.cob.userservice.api.model.RoleDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
public class ExtendedConsultingTypeResponseDTOHelper {

  public static ExtendedConsultingTypeResponseDTO createExtendedConsultingTypeResponseDTO(int id, String slug, boolean excludeNonMainConsultantsFromTeamSessions,
      boolean isGroupChat, boolean consultantBoundedToConsultingType,boolean sendWelcomeMessage,
      String welcomeMessage,boolean sendFurtherStepsMessage,boolean sendSaveSessionDataMessage,
      SessionDataInitializing sessionDataInitializing,
      boolean monitoring, String monitoringFile, boolean feedbackChat, NotificationDTO notifications,boolean languageFormal, RoleDTO roles,
      Registration registration){
    var extendedConsultingTypeResponseDTO = new ExtendedConsultingTypeResponseDTO();

    var groupChatDTO = new GroupChatDTO();
    groupChatDTO.setIsGroupChat(isGroupChat);

    var welcomeMessageDTO = new WelcomeMessageDTO();
    welcomeMessageDTO.setSendWelcomeMessage(sendWelcomeMessage);
    welcomeMessageDTO.setWelcomeMessageText(welcomeMessage);

    SessionDataInitializingDTO sessionDataInitializingDTO = null;
    if(sessionDataInitializing != null) {
      sessionDataInitializingDTO = new SessionDataInitializingDTO();
      sessionDataInitializingDTO.setAge(sessionDataInitializing.isAge());
      sessionDataInitializingDTO.setState(sessionDataInitializing.isState());
      sessionDataInitializingDTO.setAddictiveDrugs(sessionDataInitializing.isAddictiveDrugs());
      sessionDataInitializingDTO.setGender(sessionDataInitializing.isGender());
      sessionDataInitializingDTO.setRelation(sessionDataInitializing.isRelation());
    }

    var monitoringDTO = new MonitoringDTO();
    monitoringDTO.setInitializeMonitoring(monitoring);
    monitoringDTO.setMonitoringTemplateFile(monitoringFile);

    NotificationsDTO notificationsDTO = null;
    if(notifications != null && notifications.getNewMessage() != null){
      notificationsDTO = new NotificationsDTO();
      var teamSessionsDTO = new TeamSessionsDTO();
      var newMessageDTO = new NewMessageDTO();
      newMessageDTO.setAllTeamConsultants(notifications.getNewMessage().getTeamSession().getToConsultant().getAllTeamConsultants());
      teamSessionsDTO.setNewMessage(newMessageDTO);
      notificationsDTO.setTeamSessions(teamSessionsDTO);
    }

    RolesDTO rolesDTO = null;
    if(roles != null){
      rolesDTO = new RolesDTO();
      rolesDTO.setConsultant(roles.getConsultant());
    }

    RegistrationDTO registrationDTO = null;
    if(registration!=null){
      registrationDTO = new RegistrationDTO();
      RegistrationMandatoryFieldsDTO registrationMandatoryFieldsDTO = null;
      if(registration.getMandatoryFields() != null) {
        registrationMandatoryFieldsDTO = new RegistrationMandatoryFieldsDTO();
        registrationMandatoryFieldsDTO.setAge(registration.getMandatoryFields().isAge());
        registrationMandatoryFieldsDTO.setState(registration.getMandatoryFields().isState());
      }
      registrationDTO.setMandatoryFields(registrationMandatoryFieldsDTO);
    }

    extendedConsultingTypeResponseDTO.setId(id);
    extendedConsultingTypeResponseDTO.setSlug(slug);
    extendedConsultingTypeResponseDTO.setExcludeNonMainConsultantsFromTeamSessions(excludeNonMainConsultantsFromTeamSessions);
    extendedConsultingTypeResponseDTO.setGroupChat(groupChatDTO);
    extendedConsultingTypeResponseDTO.setConsultantBoundedToConsultingType(consultantBoundedToConsultingType);
    extendedConsultingTypeResponseDTO.setWelcomeMessage(welcomeMessageDTO);
    extendedConsultingTypeResponseDTO.setSendFurtherStepsMessage(sendFurtherStepsMessage);
    extendedConsultingTypeResponseDTO.setSendSaveSessionDataMessage(sendSaveSessionDataMessage);
    extendedConsultingTypeResponseDTO.setSessionDataInitializing(sessionDataInitializingDTO);
    extendedConsultingTypeResponseDTO.setMonitoring(monitoringDTO);
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(feedbackChat);
    extendedConsultingTypeResponseDTO.setNotifications(notificationsDTO);
    extendedConsultingTypeResponseDTO.setLanguageFormal(languageFormal);
    extendedConsultingTypeResponseDTO.setRoles(rolesDTO);
    extendedConsultingTypeResponseDTO.setRegistration(registrationDTO);

    return extendedConsultingTypeResponseDTO;

  }

  public static ExtendedConsultingTypeResponseDTO createExtendedConsultingTypeResponseDTO(){
    return new ExtendedConsultingTypeResponseDTO();
  }

}

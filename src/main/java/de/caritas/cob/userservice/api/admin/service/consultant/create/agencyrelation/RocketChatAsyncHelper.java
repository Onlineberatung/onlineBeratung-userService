package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_FREE_TEXT;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatAddToGroupOperationService;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.mailservice.generated.web.model.ErrorMailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RocketChatAsyncHelper {

  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull MailService mailService;
  @Value("${app.base.url}")
  private String applicationBaseUrl;

  @Async
  @Transactional
  public void addConsultantToSessions(Consultant consultant, AgencyDTO agency,
      Consumer<String> logMethod, Long tenantId) {
    try {
      TenantContext.setCurrentTenant(tenantId);
      List<Session> relevantSessions = collectRelevantSessionsToAddConsultant(agency);
      RocketChatAddToGroupOperationService
          .getInstance(this.rocketChatFacade, identityClient, logMethod,
              consultingTypeManager)
          .onSessions(relevantSessions)
          .withConsultant(consultant)
          .addToGroupsOrRollbackOnFailure();
      updateConsultantStatus(consultant, ConsultantStatus.CREATED);
    } catch (Exception e) {
      updateConsultantStatus(consultant, ConsultantStatus.ERROR);
      sendErrorEmail(consultant, e);
      log.error("Error happened during rocket chat session assignments", e);
    }
    TenantContext.clear();
  }

  private void updateConsultantStatus(Consultant consultant, ConsultantStatus status) {
    consultant.setStatus(status);
    consultantRepository.save(consultant);
  }

  private void sendErrorEmail(Consultant consultant, Exception exception) {
    ErrorMailDTO errorMailDTO = new ErrorMailDTO()
        .template(TEMPLATE_FREE_TEXT)
        .templateData(asList(
            new TemplateDataDTO().key("subject").value("RocketChat sessions assignment error"),
            new TemplateDataDTO().key("url").value(this.applicationBaseUrl),
            new TemplateDataDTO().key("text").value(getEmailText(consultant, exception))));
    this.mailService.sendErrorEmailNotification(errorMailDTO);
  }

  private String getEmailText(Consultant consultant, Exception exception) {
    return "Error happened during rocket chat session assignments for consultant " + consultant
        .getUsername() + ". Error message: " + exception.getMessage();
  }

  private List<Session> collectRelevantSessionsToAddConsultant(AgencyDTO agency) {
    List<Session> sessionsToAddConsultant = sessionRepository
        .findByAgencyIdAndStatusAndConsultantIsNull(agency.getId(), SessionStatus.NEW);
    if (isTrue(agency.getTeamAgency())) {
      sessionsToAddConsultant.addAll(sessionRepository
          .findByAgencyIdAndStatusAndTeamSessionIsTrue(agency.getId(), SessionStatus.IN_PROGRESS));
    }
    return sessionsToAddConsultant;
  }

}

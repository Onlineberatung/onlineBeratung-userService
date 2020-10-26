package de.caritas.cob.userservice.api.facade;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AgencyHelper;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.registration.NewRegistrationDto;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.SessionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateSessionFacade {

  private final SessionService sessionService;
  private final ConsultingTypeManager consultingTypeManager;
  private final AgencyHelper agencyHelper;
  private final MonitoringService monitoringService;
  private final SessionDataService sessionDataService;

  @Autowired
  public CreateSessionFacade(SessionService sessionService,
      ConsultingTypeManager consultingTypeManager, AgencyHelper agencyHelper,
      MonitoringService monitoringService, SessionDataService sessionDataService) {
    this.sessionService = sessionService;
    this.consultingTypeManager = consultingTypeManager;
    this.agencyHelper = agencyHelper;
    this.monitoringService = monitoringService;
    this.sessionDataService = sessionDataService;
  }

  /**
   * Creates a new {@link Session} for the currently {@link AuthenticatedUser} if this user does not
   * already have a session for the provided {@link ConsultingType}.
   *
   * @param newRegistrationDto {@link NewRegistrationDto}
   * @return The ID of the created session
   */
  public Long createSession(NewRegistrationDto newRegistrationDto,
      User user) {

    ConsultingType consultingType =
        ConsultingType.values()[Integer.parseInt(newRegistrationDto.getConsultingType())];

    if (isRegisteredToConsultingType(user, consultingType)) {
      throw new ConflictException(
          String.format("User %s is already registered to consulting type %s", user.getUserId(),
              consultingType.getValue()));
    }

    // Get agency and check if agency is assigned to given consulting type
    AgencyDTO agencyDto =
        agencyHelper.getVerifiedAgency(newRegistrationDto.getAgencyId(), consultingType);

    if (agencyDto == null) {
      throw new BadRequestException(String.format("Agency %s is not assigned to given consulting type %s",
          newRegistrationDto.getAgencyId(), consultingType.getValue()));
    }

    Session session = null;
    try {
      session = saveNewSession(newRegistrationDto, consultingType,
          isTrue(agencyDto.getTeamAgency()), user);
      sessionDataService.saveSessionDataFromRegistration(session, new UserDTO());

    } catch (InternalServerErrorException serviceException) {
      if (session != null) {
        sessionService.deleteSession(session);
      }
      throw new InternalServerErrorException(
          String.format("Could not register new consulting type session with %s",
          newRegistrationDto.toString()));
    } catch (CreateMonitoringException createMonitoringException) {
      monitoringService.rollbackInitializeMonitoring(
          createMonitoringException.getExceptionInformation().getSession());

      throw new InternalServerErrorException(String.format(
          "Could not create monitoring while registering a new consulting type session with %s",
          newRegistrationDto.toString()));
    }

    return session.getId();
  }

  /**
   * Saves the new {@link Session} and creates the initial monitoring.
   *
   * @param newRegistrationDto {@link NewRegistrationDto}
   * @param consultingType     {@link ConsultingType}
   * @param isTeamAgency       {@link AgencyDTO#getTeamAgency()}
   * @return the new registered {@link Session}
   * @throws CreateMonitoringException when initialization of monitoring fails
   */
  private Session saveNewSession(NewRegistrationDto newRegistrationDto,
      ConsultingType consultingType, Boolean isTeamAgency, User user)
      throws CreateMonitoringException {

    ConsultingTypeSettings consultingTypeSettings;
    try {
      consultingTypeSettings = consultingTypeManager.getConsultantTypeSettings(consultingType);
    } catch (MissingConsultingTypeException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
    Session session = sessionService.saveSession(new Session(user, consultingType,
        newRegistrationDto.getPostcode(), newRegistrationDto.getAgencyId(), SessionStatus.INITIAL,
        isTrue(isTeamAgency), consultingTypeSettings.isMonitoring()));

    monitoringService.createMonitoringIfConfigured(session, consultingTypeSettings);

    return session;
  }

  /**
   * Checks if the given {@link User} is already registered within the given {@link
   * ConsultingType}.
   *
   * @param user {@link User}
   * @param consultingType {@link ConsultingType}
   * @return true if already registered, false if not
   */
  private boolean isRegisteredToConsultingType(User user, ConsultingType consultingType) {

    List<Session> sessions =
        sessionService.getSessionsForUserByConsultingType(user, consultingType);

    return !sessions.isEmpty();
  }
}

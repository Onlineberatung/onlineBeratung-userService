package de.caritas.cob.UserService.api.facade;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.UserService.api.helper.AuthenticatedUser;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.model.NewRegistrationDto;
import de.caritas.cob.UserService.api.model.UserSessionResponseDTO;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.session.SessionStatus;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.UserService;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

@Service
public class CreateSessionFacade {
  private final UserService userService;
  private final SessionService sessionService;
  private final ConsultingTypeManager consultingTypeManager;
  private final AgencyServiceHelper agencyServiceHelper;
  private final AuthenticatedUser authenticatedUser;

  @Autowired
  public CreateSessionFacade(UserService userService, SessionService sessionService,
      ConsultingTypeManager consultingTypeManager, AgencyServiceHelper agencyServiceHelper,
      AuthenticatedUser authenticatedUser) {
    this.userService = userService;
    this.sessionService = sessionService;
    this.consultingTypeManager = consultingTypeManager;
    this.agencyServiceHelper = agencyServiceHelper;
    this.authenticatedUser = authenticatedUser;
  }

  public HttpStatus createSession(NewRegistrationDto newRegistrationDto) {
    ConsultingType consultingType =
        ConsultingType.values()[Integer.valueOf(newRegistrationDto.getConsultingType())];

    // Check if already registered to consulting type
    List<UserSessionResponseDTO> sessions =
        sessionService.getSessionsForUserId(authenticatedUser.getUserId());

    if (sessions.stream()
        .filter(session -> session.getSession().getConsultingType() == consultingType.getValue())
        .findFirst().isPresent()) {
      return HttpStatus.CONFLICT;
    }

    // Check if agency has correct (provided) consulting type
    AgencyDTO agencyDTO = null;
    try {
      agencyDTO = agencyServiceHelper.getAgencyWithoutCaching(newRegistrationDto.getAgencyId());
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      throw new ServiceException(
          String.format("Could not get agency with id %s for consultingType %s",
              newRegistrationDto.getAgencyId(), consultingType),
          agencyServiceHelperException);
    }
    if (agencyDTO == null) {
      throw new ServiceException(
          String.format("Could not get agency with id %s for consultingType %s",
              newRegistrationDto.getAgencyId(), consultingType));
    }
    if (!agencyDTO.getConsultingType().equals(consultingType)) {
      throw new BadRequestException(String.format(
          "The provided agency with id %s is not assigned to the provided consulting type %s",
          newRegistrationDto.getAgencyId(), consultingType));
    }

    Optional<User> user = userService.getUserViaAuthenticatedUser(authenticatedUser);
    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(consultingType);
    Session session = sessionService.saveSession(new Session(user.get(), consultingType,
        newRegistrationDto.getPostcode(), newRegistrationDto.getAgencyId(), SessionStatus.INITIAL,
        agencyDTO.isTeamAgency(), consultingTypeSettings.isMonitoring()));

    if (session != null) {
      return HttpStatus.CREATED;

    }

    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}

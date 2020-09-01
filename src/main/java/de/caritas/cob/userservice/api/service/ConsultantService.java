package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.AbsenceDTO;
import de.caritas.cob.userservice.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;

@Service
public class ConsultantService {

  private ConsultantRepository consultantRepository;
  private UserHelper userHelper;

  @Autowired
  public ConsultantService(ConsultantRepository consultantRepository, UserHelper userHelper) {
    this.consultantRepository = consultantRepository;
    this.userHelper = userHelper;
  }

  /**
   * Save a {@link Consultant} to the database
   *
   * @param consultant
   * @return the {@link Consultant}
   */
  public Consultant saveConsultant(Consultant consultant) {
    try {
      return consultantRepository.save(consultant);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new InternalServerErrorException("Database error while saving consultant");
    }
  }

  /**
   * Load a {@link Consultant}
   *
   * @param consultantId
   * @return An {@link Optional} with the {@link Consultant}, if found
   */
  public Optional<Consultant> getConsultant(String consultantId) {
    try {
      return consultantRepository.findById(consultantId);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new InternalServerErrorException("Database error while loading consultant");
    }
  }

  /**
   * Returns a {@link Consultant} by the provided Rocket.Chat user id
   *
   * @param rcUserId
   * @return An {@link Optional} with the {@link Consultant}
   */
  public Optional<Consultant> getConsultantByRcUserId(String rcUserId) {
    try {
      return consultantRepository.findByRocketChatId(rcUserId);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new InternalServerErrorException(String
          .format("Database error while loading consultant by Rocket.Chat user id %s", rcUserId));
    }
  }

  /**
   * Update a {@link Consultant} with the absence data from a (@Link AbsenceDTO)
   *
   * @param consultant
   * @param absence
   * @return The updated {@link Consultant}
   */
  public Consultant updateConsultantAbsent(Consultant consultant, AbsenceDTO absence) {

    consultant.setAbsent(absence.isAbsent());

    if (absence.getMessage() != null && !absence.getMessage().isEmpty()) {
      consultant.setAbsenceMessage(Helper.removeHTMLFromText(absence.getMessage()));
    } else {
      consultant.setAbsenceMessage(null);
    }

    saveConsultant(consultant);

    return consultant;
  }

  /**
   * Returns a {@link Consultant} by the provided email address
   *
   * @param email
   * @return An {@link Optional} with the {@link Consultant}
   */
  public Optional<Consultant> getConsultantByEmail(String email) {
    try {
      return consultantRepository.findByEmail(email);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new InternalServerErrorException(String.format("Database error while loading consultant by email"));
    }
  }

  /**
   * Returns a {@link Consultant} by the provided username
   *
   * @param username
   * @return An {@link Optional} with the {@link Consultant}
   */
  public Optional<Consultant> getConsultantByUsername(String username) {
    try {
      return consultantRepository.findByUsername(username);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new InternalServerErrorException(
          String.format("Database error while loading consultant by username"));
    }
  }

  /**
   * Find a consultant by these steps: 1. username 2. encoded username 3. email
   * 
   * @param username
   * @param email
   * @return an optional with the consultant found or an empty optional
   */
  public Optional<Consultant> findConsultantByUsernameOrEmail(String username, String email) {

    // Search for decoded username
    Optional<Consultant> consultantOptional =
        getConsultantByUsername(userHelper.decodeUsername(username));
    if (consultantOptional.isPresent()) {
      return consultantOptional;
    }

    // Search for encoded username
    consultantOptional = getConsultantByUsername(userHelper.encodeUsername(username));
    if (consultantOptional.isPresent()) {
      return consultantOptional;
    }

    consultantOptional = getConsultantByEmail(email);
    if (consultantOptional.isPresent()) {
      return consultantOptional;
    }

    return Optional.empty();

  }

  /**
   * Find a consultant via the {@link AuthenticatedUser}
   * 
   * @param authenticatedUser
   * @return
   * @throws {@link InternalServerErrorException}
   */
  public Optional<Consultant> getConsultantViaAuthenticatedUser(
      AuthenticatedUser authenticatedUser) {

    Optional<Consultant> consultantOptional = getConsultant(authenticatedUser.getUserId());

    if (!consultantOptional.isPresent()) {
      throw new InternalServerErrorException(
          String.format("Calling consultant with id %s not found.", authenticatedUser.getUserId()));
    }

    return consultantOptional;

  }

  /**
   * 
   * Find consultants by agency id
   * 
   * @param chatAgencies
   * @return
   * @throws {@link InternalServerErrorException}
   */
  public List<Consultant> findConsultantsByAgencyIds(Set<ChatAgency> chatAgencies) {

    List<Long> agencyIds =
        chatAgencies.stream().map(ChatAgency::getAgencyId).collect(Collectors.toList());

    try {
      return consultantRepository.findByConsultantAgenciesAgencyIdIn(agencyIds);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new InternalServerErrorException(
          String.format("Database error while loading consultant by agency ids"));
    }
  }

}

package de.caritas.cob.userservice.api.service;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.AbsenceDTO;
import de.caritas.cob.userservice.api.repository.chatagency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;

@Service
@RequiredArgsConstructor
public class ConsultantService {

  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull UserHelper userHelper;

  /**
   * Save a {@link Consultant} to the database.
   *
   * @param consultant {@link Consultant}
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
   * Load a {@link Consultant}.
   *
   * @param consultantId consultant ID
   * @return An {@link Optional} with the {@link Consultant}, if found
   */
  public Optional<Consultant> getConsultant(String consultantId) {
    try {
      return consultantRepository.findById(consultantId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while loading consultant",
          LogService::logDatabaseError);
    }
  }

  /**
   * Returns a {@link Consultant} by the provided Rocket.Chat user ID.
   *
   * @param rcUserId Rocket.Chat user ID
   * @return An {@link Optional} with the {@link Consultant}
   */
  public Optional<Consultant> getConsultantByRcUserId(String rcUserId) {
    try {
      return consultantRepository.findByRocketChatId(rcUserId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(String
          .format("Database error while loading consultant by Rocket.Chat user id %s", rcUserId),
          LogService::logDatabaseError);
    }
  }

  /**
   * Update a {@link Consultant} with the absence data from a (@Link AbsenceDTO).
   *
   * @param consultant {@link Consultant}
   * @param absence    {@link AbsenceDTO}
   * @return The updated {@link Consultant}
   */
  public Consultant updateConsultantAbsent(Consultant consultant, AbsenceDTO absence) {

    consultant.setAbsent(isTrue(absence.getAbsent()));

    if (absence.getMessage() != null && !absence.getMessage().isEmpty()) {
      consultant.setAbsenceMessage(Helper.removeHTMLFromText(absence.getMessage()));
    } else {
      consultant.setAbsenceMessage(null);
    }

    saveConsultant(consultant);

    return consultant;
  }

  /**
   * Returns a {@link Consultant} by the provided email address.
   *
   * @param email email address
   * @return An {@link Optional} with the {@link Consultant}
   */
  public Optional<Consultant> getConsultantByEmail(String email) {
    try {
      return consultantRepository.findByEmail(email);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException("Database error while loading consultant by email",
          LogService::logDatabaseError);
    }
  }

  /**
   * Returns a {@link Consultant} by the provided username.
   *
   * @param username username
   * @return An {@link Optional} with the {@link Consultant}
   */
  public Optional<Consultant> getConsultantByUsername(String username) {
    try {
      return consultantRepository.findByUsername(username);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          String.format("Database error while loading consultant by username %s", username),
          LogService::logDatabaseError);
    }
  }

  /**
   * Find a consultant by these steps: 1. username 2. encoded username 3. email.
   *
   * @param username username
   * @param email    email address
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
    return consultantOptional;
  }

  /**
   * Find a consultant via the {@link AuthenticatedUser}.
   *
   * @param authenticatedUser {@link AuthenticatedUser}
   * @return {@link Optional} of {@link Consultant}
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
   * Find consultants by agency ID.
   *
   * @param chatAgencies {@link Set} of {@link ChatAgency}
   * @return {@link List} of {@link Consultant}
   * @throws {@link InternalServerErrorException}
   */
  public List<Consultant> findConsultantsByAgencyIds(Set<ChatAgency> chatAgencies) {

    List<Long> agencyIds =
        chatAgencies.stream().map(ChatAgency::getAgencyId).collect(Collectors.toList());

    try {
      return consultantRepository.findByConsultantAgenciesAgencyIdIn(agencyIds);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          "Database error while loading consultant by agency ids", LogService::logDatabaseError);
    }
  }

}

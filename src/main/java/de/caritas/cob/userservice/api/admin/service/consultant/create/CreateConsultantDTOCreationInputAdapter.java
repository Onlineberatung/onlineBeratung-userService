package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Adapter class to provide a {@link ConsultantCreationInput} based on a
 * {@link CreateConsultantDTO}.
 */
@RequiredArgsConstructor
public class CreateConsultantDTOCreationInputAdapter implements ConsultantCreationInput {

  private final @NonNull CreateConsultantDTO createConsultantDTO;
  private final @NonNull UserHelper userHelper;

  /**
   * Provides the old id.
   *
   * @return always null here
   */
  @Override
  public Long getIdOld() {
    return null;
  }

  /**
   * Provides the user name.
   *
   * @return the user name
   */
  @Override
  public String getUserName() {
    return this.createConsultantDTO.getUsername();
  }

  /**
   * Provides the encoded user name.
   *
   * @return the encoded user name
   */
  @Override
  public String getEncodedUsername() {
    return this.userHelper.encodeUsername(createConsultantDTO.getUsername());
  }

  /**
   * Provides the first name.
   *
   * @return the first name
   */
  @Override
  public String getFirstName() {
    return this.createConsultantDTO.getFirstname();
  }

  /**
   * Provides the last name.
   *
   * @return the last name
   */
  @Override
  public String getLastName() {
    return this.createConsultantDTO.getLastname();
  }

  /**
   * Provides the email address.
   *
   * @return the email address
   */
  @Override
  public String getEmail() {
    return this.createConsultantDTO.getEmail();
  }

  /**
   * Provides the absent flag.
   *
   * @return the absent flag
   */
  @Override
  public boolean isAbsent() {
    return isTrue(this.createConsultantDTO.getAbsent());
  }

  /**
   * Provides the absence message.
   *
   * @return the absence message
   */
  @Override
  public String getAbsenceMessage() {
    return this.createConsultantDTO.getAbsenceMessage();
  }

  /**
   * Provides the team consultant flag.
   *
   * @return the team consultant flag
   */
  @Override
  public boolean isTeamConsultant() {
    return false;
  }

  /**
   * Provdes the language formal flag.
   *
   * @return the language formal flag
   */
  @Override
  public boolean isLanguageFormal() {
    return isTrue(this.createConsultantDTO.getFormalLanguage());
  }

  /**
   * Provides the created date.
   *
   * @return the created date
   */
  @Override
  public LocalDateTime getCreateDate() {
    return LocalDateTime.now();
  }

  /**
   * Provides the updated date.
   *
   * @return the updated date
   */
  @Override
  public LocalDateTime getUpateDate() {
    return LocalDateTime.now();
  }
}

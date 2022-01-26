package de.caritas.cob.userservice.api.admin.service.consultant.create;

import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Adapter class to provide a {@link ConsultantCreationInput} based on a {@link ImportRecord}.
 */
@RequiredArgsConstructor
public class ImportRecordCreationInputAdapter implements ConsultantCreationInput {

  private final @NonNull ImportRecord importRecord;

  /**
   * Provides the old id.
   *
   * @return the old id
   */
  @Override
  public Long getIdOld() {
    return this.importRecord.getIdOld();
  }

  /**
   * Provides the user name.
   *
   * @return the user name
   */
  @Override
  public String getUserName() {
    return this.importRecord.getUsername();
  }

  /**
   * Provides the encoded user name.
   *
   * @return the encoded user name
   */
  @Override
  public String getEncodedUsername() {
    return this.importRecord.getUsernameEncoded();
  }

  /**
   * Provides the first name.
   *
   * @return the first name
   */
  @Override
  public String getFirstName() {
    return this.importRecord.getFirstName();
  }

  /**
   * Provides the last name.
   *
   * @return the last name
   */
  @Override
  public String getLastName() {
    return this.importRecord.getLastName();
  }

  /**
   * Provides the email address.
   *
   * @return the email address
   */
  @Override
  public String getEmail() {
    return this.importRecord.getEmail();
  }

  /**
   * Provides the absent flag.
   *
   * @return the absent flag
   */
  @Override
  public boolean isAbsent() {
    return this.importRecord.isAbsent();
  }

  /**
   * Provides the absence message.
   *
   * @return the absence message
   */
  @Override
  public String getAbsenceMessage() {
    return this.importRecord.getAbsenceMessage();
  }

  /**
   * Provides the team consultant flag.
   *
   * @return the team consultant flag
   */
  @Override
  public boolean isTeamConsultant() {
    return this.importRecord.isTeamConsultant();
  }

  /**
   * Provdes the language formal flag.
   *
   * @return the language formal flag
   */
  @Override
  public boolean isLanguageFormal() {
    return this.importRecord.isFormalLanguage();
  }

  /**
   * Provides the tenantId.
   *
   * @return the tenant id
   */
  @Override
  public Long getTenantId() {
    return this.importRecord.getTenantId();
  }

}

package de.caritas.cob.userservice.api.admin.mapper;

import de.caritas.cob.userservice.api.admin.model.ConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Mapper class to build a {@link ConsultantDTO} based on required {@link Consultant} entity.
 */
@RequiredArgsConstructor
public class ConsultantAdminMapper {

  private final @NonNull Consultant consultant;

  /**
   * Maps the data of given {@link Consultant} to a {@link ConsultantDTO}.
   *
   * @return the generated {@link ConsultantDTO}
   */
  public ConsultantDTO mapData() {
    return new ConsultantDTO()
        .id(this.consultant.getId())
        .username(this.consultant.getUsername())
        .firstname(this.consultant.getFirstName())
        .lastname(this.consultant.getLastName())
        .email(this.consultant.getEmail())
        .formalLanguage(this.consultant.isLanguageFormal())
        .teamConsultant(this.consultant.isTeamConsultant())
        .absent(this.consultant.isAbsent())
        .absenceMessage(this.consultant.getAbsenceMessage())
        .createDate(String.valueOf(this.consultant.getCreateDate()))
        .updateDate(String.valueOf(this.consultant.getUpdateDate()))
        .deleteDate(String.valueOf(this.consultant.getDeleteDate()));
  }

}

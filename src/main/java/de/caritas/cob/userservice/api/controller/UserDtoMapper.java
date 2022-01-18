package de.caritas.cob.userservice.api.controller;

import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import org.springframework.stereotype.Service;

@Service
public class UserDtoMapper {

  public UpdateAdminConsultantDTO updateAdminConsultantOf(UpdateConsultantDTO updateConsultantDTO,
      Consultant consultant) {

    return new UpdateAdminConsultantDTO()
        .email(updateConsultantDTO.getEmail())
        .firstname(updateConsultantDTO.getFirstname())
        .lastname(updateConsultantDTO.getLastname())
        .formalLanguage(consultant.isLanguageFormal())
        .languages(updateConsultantDTO.getLanguages())
        .absent(consultant.isAbsent())
        .absenceMessage(consultant.getAbsenceMessage());
  }
}

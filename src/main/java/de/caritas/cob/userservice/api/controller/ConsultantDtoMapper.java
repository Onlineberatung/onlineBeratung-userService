package de.caritas.cob.userservice.api.controller;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO.LanguagesEnum;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ConsultantDtoMapper {

  public UpdateAdminConsultantDTO updateAdminConsultantOf(UpdateConsultantDTO updateConsultantDTO,
      Consultant consultant) {

    var languages = updateConsultantDTO.getLanguages();
    var languageStrings = isNull(languages) ? null : languages.stream()
        .map(LanguagesEnum::getValue)
        .collect(Collectors.toList());

    return new UpdateAdminConsultantDTO()
        .email(updateConsultantDTO.getEmail())
        .firstname(updateConsultantDTO.getFirstname())
        .lastname(updateConsultantDTO.getLastname())
        .formalLanguage(consultant.isLanguageFormal())
        .languages(languageStrings)
        .absent(consultant.isAbsent())
        .absenceMessage(consultant.getAbsenceMessage());
  }
}

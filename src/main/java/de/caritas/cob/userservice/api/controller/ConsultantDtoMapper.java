package de.caritas.cob.userservice.api.controller;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.AgencyResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO.LanguagesEnum;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.util.List;
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

  public ConsultantResponseDTO consultantResponseDtoOf(Consultant consultant,
      List<AgencyDTO> agencies, boolean mapNames) {
    var agencyDtoList = agencies.stream()
        .map(this::agencyResponseDtoOf)
        .collect(Collectors.toList());

    var consultantResponseDto = new ConsultantResponseDTO()
        .consultantId(consultant.getId())
        .agencies(agencyDtoList);

    if (mapNames) {
      consultantResponseDto
          .firstName(consultant.getFirstName())
          .lastName(consultant.getLastName());
    }

    return consultantResponseDto;
  }

  public AgencyResponseDTO agencyResponseDtoOf(AgencyDTO agencyDTO) {
    return new AgencyResponseDTO()
        .id(agencyDTO.getId())
        .city(agencyDTO.getCity())
        .consultingType(agencyDTO.getConsultingType())
        .postcode(agencyDTO.getPostcode())
        .name(agencyDTO.getName())
        .description(agencyDTO.getDescription())
        .teamAgency(agencyDTO.getTeamAgency())
        .offline(agencyDTO.getOffline());
  }
}

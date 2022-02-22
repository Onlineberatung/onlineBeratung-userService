package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ConsultantDtoMapper {

  public UpdateAdminConsultantDTO updateAdminConsultantOf(UpdateConsultantDTO updateConsultantDTO,
      Consultant consultant) {

    return new UpdateAdminConsultantDTO()
        .email(updateConsultantDTO.getEmail())
        .firstname(updateConsultantDTO.getFirstname())
        .lastname(updateConsultantDTO.getLastname())
        .formalLanguage(consultant.isLanguageFormal())
        .languages(languageStringsOf(updateConsultantDTO.getLanguages()))
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

  public String languageOf(LanguageCode languageCode) {
    return isNull(languageCode) ? null : languageCode.getValue();
  }

  public List<String> languageStringsOf(List<LanguageCode> languages) {
    return isNull(languages) ? null : languages.stream()
        .map(this::languageOf)
        .collect(Collectors.toList());
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

  public LanguageResponseDTO languageResponseDtoOf(Set<String> languageCodes) {
    var languages = languageCodes.stream()
        .sorted()
        .map(LanguageCode::fromValue)
        .collect(Collectors.toList());

    var dto = new LanguageResponseDTO();
    dto.setLanguages(languages);

    return dto;
  }
}

package de.caritas.cob.userservice.api.adapters.web.mapping;

import static java.util.Objects.isNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink;
import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageResponseDTO;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpEntity;

public interface DtoMapperUtils {

  default String languageOf(LanguageCode languageCode) {
    return isNull(languageCode) ? null : languageCode.getValue();
  }

  default List<String> languageStringsOf(List<LanguageCode> languages) {
    return isNull(languages)
        ? null
        : languages.stream().map(this::languageOf).collect(Collectors.toList());
  }

  default AgencyResponseDTO agencyResponseDtoOf(AgencyDTO agencyDTO) {
    return new AgencyResponseDTO()
        .id(agencyDTO.getId())
        .city(agencyDTO.getCity())
        .consultingType(agencyDTO.getConsultingType())
        .postcode(agencyDTO.getPostcode())
        .name(agencyDTO.getName())
        .description(agencyDTO.getDescription())
        .teamAgency(agencyDTO.getTeamAgency())
        .offline(agencyDTO.getOffline())
        .topicIds(agencyDTO.getTopicIds());
  }

  default LanguageResponseDTO languageResponseDtoOf(Set<String> languageCodes) {
    var languages =
        languageCodes.stream().sorted().map(LanguageCode::fromValue).collect(Collectors.toList());

    var dto = new LanguageResponseDTO();
    dto.setLanguages(languages);

    return dto;
  }

  default HalLink halLinkOf(HttpEntity<?> httpEntity, MethodEnum method) {
    var link = linkTo(httpEntity).withSelfRel();

    return new HalLink().href(link.getHref()).method(method).templated(link.isTemplated());
  }

  default String mappedFieldOf(String field) {
    switch (field) {
      case "FIRSTNAME":
        return "firstName";
      case "LASTNAME":
        return "lastName";
      case "EMAIL":
        return "email";
      case "TENANT_ID":
        return "tenantId";
      default:
    }

    throw new IllegalArgumentException("Mapping of field '" + field + "' not supported.");
  }

  default String chatIdOf(Map<String, Object> sessionMap) {
    if (sessionMap.containsKey("chatId")) {
      return (String) sessionMap.get("chatId");
    }

    return null;
  }
}

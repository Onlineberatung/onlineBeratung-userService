package de.caritas.cob.userservice.api.adapters.web.mapping;

import de.caritas.cob.userservice.api.adapters.web.dto.AnonymousEnquiry;
import de.caritas.cob.userservice.api.adapters.web.dto.AnonymousEnquiry.StatusEnum;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationDtoMapper {

  public AnonymousEnquiry anonymousEnquiryOf(
      Map<String, Object> sessionMap, Set<String> availableConsultants) {
    var statusString = (String) sessionMap.get("status");

    var anonymousEnquiry = new AnonymousEnquiry();
    anonymousEnquiry.setNumAvailableConsultants(availableConsultants.size());
    anonymousEnquiry.setStatus(StatusEnum.fromValue(statusString));

    return anonymousEnquiry;
  }

  public String adviceSeekerIdOf(Map<String, Object> sessionMap) {
    return (String) sessionMap.get("adviceSeekerId");
  }

  public Integer consultingTypeIdOf(Map<String, Object> sessionMap) {
    return (Integer) sessionMap.get("consultingTypeId");
  }
}

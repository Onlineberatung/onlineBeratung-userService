package de.caritas.cob.userservice.api.service.httpheader;

import java.util.Collections;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class OriginHeaderSupplier {

  public String getOriginHeaderValue(String requestServerName) {
    if (requestServerName != null) {
      return requestServerName;
    } else {
      return getOriginHeaderValueFromRequestContext();
    }
  }

  public String getOriginHeaderValue() {
    return getOriginHeaderValueFromRequestContext();
  }

  private String getOriginHeaderValueFromRequestContext() {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();

    return Collections.list(request.getHeaderNames())
        .stream()
        .collect(Collectors.toMap(h -> h, request::getHeader)).get("host");
  }

}

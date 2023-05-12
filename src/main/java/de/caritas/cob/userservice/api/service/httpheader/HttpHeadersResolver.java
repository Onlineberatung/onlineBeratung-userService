package de.caritas.cob.userservice.api.service.httpheader;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
public class HttpHeadersResolver {

  public Optional<Long> findHeaderValue(String headerName) {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    try {
      return Optional.of(Long.parseLong(request.getHeader(headerName)));
    } catch (NumberFormatException exception) {
      log.debug("Header not found or not a number {}", headerName);
      return Optional.empty();
    }
  }
}

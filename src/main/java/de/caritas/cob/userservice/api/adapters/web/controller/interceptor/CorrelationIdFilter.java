package de.caritas.cob.userservice.api.adapters.web.controller.interceptor;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

  private static final String HEADER_NAME = "X-Correlation-ID";
  private static final String MDC_NAME = "CID";

  @Override
  @SuppressWarnings("NullableProblems")
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    var correlationId = request.getHeader(HEADER_NAME);

    if (isEmpty(correlationId)) {
      log.debug("No correlation-id header '{}' has been found in request.", HEADER_NAME);

      correlationId = UUID.randomUUID().toString();
      log.debug("Set correlation id '{}' in response header '{}'.", correlationId, HEADER_NAME);
      response.addHeader(HEADER_NAME, correlationId);
    } else {
      log.debug("Correlation-id header '{}' with value '{}' found.", HEADER_NAME, correlationId);
    }

    MDC.put(MDC_NAME, correlationId);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}

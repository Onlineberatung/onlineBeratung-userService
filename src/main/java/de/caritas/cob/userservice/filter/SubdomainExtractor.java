package de.caritas.cob.userservice.filter;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;

import com.google.common.net.InternetDomainName;
import de.caritas.cob.userservice.api.helper.HttpUrlUtils;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Setter
@Slf4j
@AllArgsConstructor
public class SubdomainExtractor {

  private @NonNull NonInternetDomainSubdomainExtractor nonInternetDomainSubdomainExtractor;

  public Optional<String> getCurrentSubdomain() {
    return tryResolveSubdomain(getHttpServletRequest());
  }

  private HttpServletRequest getHttpServletRequest() {
    return ((ServletRequestAttributes) currentRequestAttributes()).getRequest();
  }

  private Optional<String> tryResolveSubdomain(HttpServletRequest request) {
    try {
      return resolveSubdomain(request.getServerName(), request);
    } catch (URISyntaxException e) {
      log.error("Could not extract subdomain: ", e);
      return empty();
    }
  }

  Optional<String> resolveSubdomain(String site, HttpServletRequest request)
      throws URISyntaxException {
    String domain = HttpUrlUtils.removeHttpPrefix(site);
    if (isUnderPublicDomain(domain)) {
      return getInternetDomainPrefix(domain);
    } else {
      return nonInternetDomainSubdomainExtractor
          .resolveSubdomain(domain, getOriginHeaderValue(request));
    }
  }

  private String getOriginHeaderValue(HttpServletRequest request) {
    return Collections.list(request.getHeaderNames())
        .stream()
        .collect(Collectors.toMap(h -> h, request::getHeader)).get("origin");
  }

  private boolean isUnderPublicDomain(String site) {
    return InternetDomainName.isValid(site) && InternetDomainName.from(site).hasPublicSuffix();
  }

  private Optional<String> getInternetDomainPrefix(String site) {
    var domainName = InternetDomainName.from(site);
    return domainName.hasParent() ? getInternetSubdomain(domainName) : empty();
  }

  private Optional<String> getInternetSubdomain(InternetDomainName domainName) {
    if (domainName.parts().isEmpty()) {
      return Optional.empty();
    }
    return of(domainName.parts().get(0));
  }
}


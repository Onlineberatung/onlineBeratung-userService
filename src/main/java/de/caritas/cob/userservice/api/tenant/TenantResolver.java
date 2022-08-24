package de.caritas.cob.userservice.api.tenant;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

public interface TenantResolver {

  Optional<Long> resolve(HttpServletRequest request);

  boolean canResolve(HttpServletRequest request);
}

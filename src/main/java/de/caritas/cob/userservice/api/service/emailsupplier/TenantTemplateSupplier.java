package de.caritas.cob.userservice.api.service.emailsupplier;

import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantTemplateSupplier {

  private final @NonNull TenantService tenantService;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  public List<TemplateDataDTO> getTemplateAttributes() {
    var subdomain = TenantContext.getCurrentTenantData().getSubdomain();
    RestrictedTenantDTO tenantData = tenantService
        .getRestrictedTenantDataBySubdomain(subdomain);

    List<TemplateDataDTO> templateAttributes = new ArrayList<>();
    templateAttributes.add(getTenantName(tenantData));
    templateAttributes.add(getTenantClaim(tenantData));
    String tenantBaseUrl = getTenantBaseUrl(subdomain);
    templateAttributes.add(new TemplateDataDTO().key("url").value(tenantBaseUrl));
    templateAttributes.add(getTenantImprintUrl(tenantBaseUrl));
    templateAttributes.add(getTanantPrivacyUrl(tenantBaseUrl));

    return templateAttributes;
  }

  private TemplateDataDTO getTenantImprintUrl(String tenantUrl) {
    return new TemplateDataDTO().key("tenant_urlimpressum").value(tenantUrl + "/impressum");
  }

  private TemplateDataDTO getTanantPrivacyUrl(String tenantUrl) {
    return new TemplateDataDTO().key("tenant_urldatenschutz").value(tenantUrl + "/datenschutz");
  }

  private String getTenantBaseUrl(
      String subdomain) {
    String hostName = "";
    try {
      hostName = new URI(applicationBaseUrl).getHost();
    } catch (URISyntaxException exception) {
      log.error("Application base url not valid");
    }

    return "https://" + subdomain + "." + hostName;
  }

  private TemplateDataDTO getTenantClaim(RestrictedTenantDTO tenantData) {
    return new TemplateDataDTO().key("tenant_claim").value(tenantData.getContent().getClaim());
  }

  private TemplateDataDTO getTenantName(RestrictedTenantDTO tenantData) {
    return new TemplateDataDTO().key("tenant_name").value(tenantData.getName());
  }

}

package de.caritas.cob.userservice.api.service.emailsupplier;

import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import de.caritas.cob.userservice.tenantservice.generated.web.TenantControllerApi;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantDataSupplier {

  private final TenantControllerApi tenantControllerApi;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  public List<TemplateDataDTO> getTemplateAttributes() {
    var subdomain = TenantContext.getCurrentTenantData().getSubdomain();
    RestrictedTenantDTO tenantData = tenantControllerApi
        .getRestrictedTenantDataBySubdomain(subdomain);

    List<TemplateDataDTO> templateAttributes = new ArrayList<>();
    templateAttributes.add(new TemplateDataDTO().key("tenant_name").value(tenantData.getName()));
    templateAttributes
        .add(new TemplateDataDTO().key("tenant_claim").value(tenantData.getContent().getClaim()));
    String hostName = "";
    try {
      hostName = new URI(applicationBaseUrl).getHost();
    } catch (URISyntaxException exception) {
      log.error("Application base url not valid");
    }

    var tenantUrl = "https://" + subdomain + "." + hostName;

    templateAttributes.add(new TemplateDataDTO().key("url").value(tenantUrl));
    templateAttributes
        .add(new TemplateDataDTO().key("tenant_urlimpressum").value(tenantUrl + "/impressum"));
    templateAttributes
        .add(new TemplateDataDTO().key("tenant_urldatenschutz").value(tenantUrl + "/datenschutz"));

    return templateAttributes;
  }

}

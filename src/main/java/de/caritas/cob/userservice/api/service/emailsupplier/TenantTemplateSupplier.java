package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.tenant.TenantContext.getCurrentTenantData;

import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.service.consultingtype.ApplicationSettingsService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.applicationsettingsservice.generated.web.model.ApplicationSettingsDTO;
import de.caritas.cob.userservice.applicationsettingsservice.generated.web.model.SettingDTO;
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

  private static final String HTTPS = "https://";
  private final @NonNull TenantService tenantService;

  private final @NonNull ApplicationSettingsService applicationSettingsService;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  @Value("${feature.multitenancy.with.single.domain.enabled}")
  private boolean multitenancyWithSingleDomain;

  public List<TemplateDataDTO> getTemplateAttributes() {

    List<TemplateDataDTO> templateAttributes = new ArrayList<>();
    RestrictedTenantDTO tenantData = getRestrictedTenantDTO();
    if (multitenancyWithSingleDomain) {
      RestrictedTenantDTO rootDomainTenantData =
          getRestrictedTenantDTOForSingleDomainMultitenancy();
      templateAttributes.add(getTenantName(rootDomainTenantData));
      templateAttributes.add(getTenantClaim(rootDomainTenantData));
    } else {
      templateAttributes.add(getTenantName(tenantData));
      templateAttributes.add(getTenantClaim(tenantData));
    }
    addUrlBasedAttributes(tenantData, templateAttributes);
    return templateAttributes;
  }

  private void addUrlBasedAttributes(
      RestrictedTenantDTO tenantData, List<TemplateDataDTO> templateAttributes) {
    String tenantBaseUrl = getTenantBaseUrl(tenantData.getSubdomain());
    templateAttributes.add(new TemplateDataDTO().key("url").value(tenantBaseUrl));
    templateAttributes.add(getTenantImprintUrl(tenantBaseUrl));
    templateAttributes.add(getTanantPrivacyUrl(tenantBaseUrl));
  }

  private RestrictedTenantDTO getRestrictedTenantDTOForSingleDomainMultitenancy() {
    ApplicationSettingsDTO applicationSettings =
        applicationSettingsService.getApplicationSettings();
    SettingDTO mainTenantSubdomainForSingleDomainMultitenancy =
        applicationSettings.getMainTenantSubdomainForSingleDomainMultitenancy();

    if (mainTenantSubdomainForSingleDomainMultitenancy == null
        || mainTenantSubdomainForSingleDomainMultitenancy.getValue() == null) {
      log.error("main tenant subdomain not found in app settings");
      throw new IllegalStateException("main tenant subdomain not found in app settings");
    }

    var mainTenantSubdomain = mainTenantSubdomainForSingleDomainMultitenancy.getValue();
    return tenantService.getRestrictedTenantData(mainTenantSubdomain);
  }

  private RestrictedTenantDTO getRestrictedTenantDTO() {

    if (shouldResolveTenantDataBySubdomin()) {
      return tenantService.getRestrictedTenantData(getCurrentTenantData().getSubdomain());
    } else {
      log.warn("Subdomain was null, fallback to tenant data resolution by tenant id");
      var tenantId = getCurrentTenantData().getTenantId();
      log.info("Resolving tenant data by tenantId {}", tenantId);
      return tenantService.getRestrictedTenantData(tenantId);
    }
  }

  private boolean shouldResolveTenantDataBySubdomin() {
    return !multitenancyWithSingleDomain
        && TenantContext.getCurrentTenantData().getSubdomain() != null;
  }

  private TemplateDataDTO getTenantImprintUrl(String tenantUrl) {
    return new TemplateDataDTO().key("tenant_urlimpressum").value(tenantUrl + "/impressum");
  }

  private TemplateDataDTO getTanantPrivacyUrl(String tenantUrl) {
    return new TemplateDataDTO().key("tenant_urldatenschutz").value(tenantUrl + "/datenschutz");
  }

  private String getTenantBaseUrl(String subdomain) {
    if (multitenancyWithSingleDomain) {
      return applicationBaseUrl;
    } else {
      return getTenantBaseUrlForStandardMultitenancyMode(subdomain);
    }
  }

  private String getTenantBaseUrlForStandardMultitenancyMode(String subdomain) {
    String hostName = "";
    try {
      hostName = new URI(applicationBaseUrl).getHost();
    } catch (URISyntaxException exception) {
      log.error("Application base url not valid");
    }
    return getHostnameWithSubdomainPrefix(subdomain, hostName);
  }

  private String getHostnameWithSubdomainPrefix(String subdomain, String hostName) {
    return HTTPS + subdomain + "." + hostName;
  }

  private TemplateDataDTO getTenantClaim(RestrictedTenantDTO tenantData) {
    return new TemplateDataDTO().key("tenant_claim").value(tenantData.getContent().getClaim());
  }

  private TemplateDataDTO getTenantName(RestrictedTenantDTO tenantData) {
    return new TemplateDataDTO().key("tenant_name").value(tenantData.getName());
  }
}

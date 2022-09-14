package de.caritas.cob.userservice.api.service.emailsupplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantData;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TenantTemplateSupplierTest {

  private static final String VALID_SUBDOMAIN = "subdomain";

  @InjectMocks TenantTemplateSupplier tenantTemplateSupplier;

  @Mock TenantService tenantService;

  private final EasyRandom easyRandom = new EasyRandom();

  @AfterEach
  void tearDown() {
    ReflectionTestUtils.setField(tenantTemplateSupplier, "multitenancyWithSingleDomain", false);
  }

  @Test
  void getTemplateAttributes_ShouldProvideTenantSpecificDataForEmailTemplates() {
    // given
    var tenantData = new TenantData();
    tenantData.setTenantId(1L);
    tenantData.setSubdomain(VALID_SUBDOMAIN);
    TenantContext.setCurrentTenantData(tenantData);
    ReflectionTestUtils.setField(
        tenantTemplateSupplier, "applicationBaseUrl", "https://onlineberatung.net");
    RestrictedTenantDTO mockedTenantData = easyRandom.nextObject(RestrictedTenantDTO.class);

    // when
    when(tenantService.getRestrictedTenantData(tenantData.getSubdomain()))
        .thenReturn(mockedTenantData);
    mockedTenantData.setSubdomain(VALID_SUBDOMAIN);
    List<TemplateDataDTO> templateAttributes = tenantTemplateSupplier.getTemplateAttributes();

    // then
    assertTemplateAttributesAreCorrect(mockedTenantData, templateAttributes);

    verify(tenantService).getRestrictedTenantData(tenantData.getSubdomain());
    verify(tenantService, Mockito.never()).getRestrictedTenantData(tenantData.getTenantId());

    TenantContext.clear();
  }

  @Test
  void
      getTemplateAttributes_Should_ProvideTenantSpecificDataForEmailTemplates_When_SubdomainNotSet() {
    // given
    var tenantData = new TenantData();
    tenantData.setTenantId(1L);
    tenantData.setSubdomain(null);
    TenantContext.setCurrentTenantData(tenantData);
    ReflectionTestUtils.setField(
        tenantTemplateSupplier, "applicationBaseUrl", "https://onlineberatung.net");
    RestrictedTenantDTO mockedTenantData = easyRandom.nextObject(RestrictedTenantDTO.class);
    mockedTenantData.setSubdomain(VALID_SUBDOMAIN);

    // when
    when(tenantService.getRestrictedTenantData(tenantData.getTenantId()))
        .thenReturn(mockedTenantData);
    List<TemplateDataDTO> templateAttributes = tenantTemplateSupplier.getTemplateAttributes();

    // then
    assertTemplateAttributesAreCorrect(mockedTenantData, templateAttributes);

    verify(tenantService, Mockito.never()).getRestrictedTenantData(tenantData.getSubdomain());
    verify(tenantService).getRestrictedTenantData(tenantData.getTenantId());

    TenantContext.clear();
  }

  @Test
  void
      getTemplateAttributes_ProvideTenantSpecificDataForEmailTemplates_When_FeatureMultitenancyWithSingleDomainIsEnabled() {
    // given
    ReflectionTestUtils.setField(tenantTemplateSupplier, "multitenancyWithSingleDomain", true);
    var tenantData = new TenantData();
    tenantData.setTenantId(1L);
    tenantData.setSubdomain("somedomain");
    TenantContext.setCurrentTenantData(tenantData);
    ReflectionTestUtils.setField(
        tenantTemplateSupplier, "applicationBaseUrl", "https://onlineberatung.net");
    RestrictedTenantDTO mockedTenantData = easyRandom.nextObject(RestrictedTenantDTO.class);
    mockedTenantData.setSubdomain(VALID_SUBDOMAIN);

    // when
    when(tenantService.getRestrictedTenantData(tenantData.getTenantId()))
        .thenReturn(mockedTenantData);
    List<TemplateDataDTO> templateAttributes = tenantTemplateSupplier.getTemplateAttributes();

    // then
    assertTemplateAttributesAreCorrect(mockedTenantData, templateAttributes);

    verify(tenantService, Mockito.never()).getRestrictedTenantData(tenantData.getSubdomain());
    verify(tenantService).getRestrictedTenantData(tenantData.getTenantId());

    TenantContext.clear();
  }

  private void assertTemplateAttributesAreCorrect(
      RestrictedTenantDTO mockedTenantData, List<TemplateDataDTO> templateAttributes) {
    assertThat(templateAttributes.get(0).getKey(), is("tenant_name"));
    assertThat(templateAttributes.get(0).getValue(), is(mockedTenantData.getName()));

    assertThat(templateAttributes.get(1).getKey(), is("tenant_claim"));
    assertThat(templateAttributes.get(1).getValue(), is(mockedTenantData.getContent().getClaim()));

    assertThat(templateAttributes.get(2).getKey(), is("url"));
    assertThat(templateAttributes.get(2).getValue(), is("https://subdomain.onlineberatung.net"));

    assertThat(templateAttributes.get(3).getKey(), is("tenant_urlimpressum"));
    assertThat(
        templateAttributes.get(3).getValue(), is("https://subdomain.onlineberatung.net/impressum"));

    assertThat(templateAttributes.get(4).getKey(), is("tenant_urldatenschutz"));
    assertThat(
        templateAttributes.get(4).getValue(),
        is("https://subdomain.onlineberatung.net/datenschutz"));
  }
}

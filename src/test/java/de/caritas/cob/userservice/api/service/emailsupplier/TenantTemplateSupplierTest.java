package de.caritas.cob.userservice.api.service.emailsupplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantData;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class TenantTemplateSupplierTest {

  @InjectMocks
  TenantTemplateSupplier tenantTemplateSupplier;

  @Mock
  TenantService tenantService;

  private final EasyRandom easyRandom = new EasyRandom();

  @Test
  public void test_Provide_Tenant_Specific_Data_For_Email_Templates() {
    //given
    var tenantData = new TenantData();
    tenantData.setTenantId(1L);
    tenantData.setSubdomain("subdomain");
    TenantContext.setCurrentTenantData(tenantData);
    ReflectionTestUtils
        .setField(tenantTemplateSupplier, "applicationBaseUrl", "https://onlineberatung.net");
    RestrictedTenantDTO mockedTenantData = easyRandom.nextObject(RestrictedTenantDTO.class);

    //when
    when(tenantService.getRestrictedTenantDataBySubdomain(tenantData.getSubdomain()))
        .thenReturn(mockedTenantData);
    List<TemplateDataDTO> templateAttributes = tenantTemplateSupplier.getTemplateAttributes();

    //then
    assertThat(templateAttributes.get(0).getKey(), is("tenant_name"));
    assertThat(templateAttributes.get(0).getValue(), is(mockedTenantData.getName()));

    assertThat(templateAttributes.get(1).getKey(), is("tenant_claim"));
    assertThat(templateAttributes.get(1).getValue(), is(mockedTenantData.getContent().getClaim()));

    assertThat(templateAttributes.get(2).getKey(), is("url"));
    assertThat(templateAttributes.get(2).getValue(), is("https://subdomain.onlineberatung.net"));

    assertThat(templateAttributes.get(3).getKey(), is("tenant_urlimpressum"));
    assertThat(templateAttributes.get(3).getValue(),
        is("https://subdomain.onlineberatung.net/impressum"));

    assertThat(templateAttributes.get(4).getKey(), is("tenant_urldatenschutz"));
    assertThat(templateAttributes.get(4).getValue(),
        is("https://subdomain.onlineberatung.net/datenschutz"));

    TenantContext.clear();
  }

}

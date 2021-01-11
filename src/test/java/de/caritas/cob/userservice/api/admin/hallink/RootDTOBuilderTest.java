package de.caritas.cob.userservice.api.admin.hallink;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import de.caritas.cob.userservice.api.model.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.model.RootDTO;
import de.caritas.cob.userservice.api.model.RootLinks;
import org.junit.Test;

public class RootDTOBuilderTest {

  @Test
  public void buildRootDTO_Should_returnRootDTOWithHalLinks() {
    RootDTO rootDTO = new RootDTOBuilder().buildRootDTO();

    assertThat(rootDTO, notNullValue());
    RootLinks rootLinks = rootDTO.getLinks();
    assertThat(rootLinks, notNullValue());
    assertThat(rootLinks.getSessions(), notNullValue());
    assertThat(rootLinks.getSessions().getHref(), is("/useradmin/sessions?page=1&perPage=20"));
    assertThat(rootLinks.getSessions().getMethod(), is(MethodEnum.GET));
    assertThat(rootLinks.getConsultants(), notNullValue());
    assertThat(rootLinks.getConsultants().getHref(),
        is("/useradmin/consultants?page=1&perPage=20"));
    assertThat(rootLinks.getConsultants().getMethod(), is(MethodEnum.GET));
    assertThat(rootLinks.getSelf().getHref(), is("/useradmin"));
    assertThat(rootLinks.getSelf().getMethod(), is(MethodEnum.GET));
    assertThat(rootLinks.getConsultantAgencies(), notNullValue());
    assertThat(rootLinks.getConsultantAgencies().getHref(),
        is("/useradmin/consultant/{consultantId}/agencies"));
    assertThat(rootLinks.getConsultantAgencies().getMethod(), is(MethodEnum.GET));
    assertThat(rootLinks.getCreateConsultant(), notNullValue());
    assertThat(rootLinks.getCreateConsultant().getHref(),
        is("/useradmin/consultant"));
    assertThat(rootLinks.getCreateConsultant().getMethod(), is(MethodEnum.POST));

  }

}

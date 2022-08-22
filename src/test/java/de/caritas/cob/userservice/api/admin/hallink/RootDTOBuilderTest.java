package de.caritas.cob.userservice.api.admin.hallink;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import de.caritas.cob.userservice.api.adapters.web.dto.HalLink.MethodEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.RootDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.RootLinks;
import org.junit.jupiter.api.Test;

class RootDTOBuilderTest {

  @Test
  void buildRootDTO_Should_returnRootDTOWithSessionHalLinks() {
    RootDTO rootDTO = new RootDTOBuilder().buildRootDTO();

    assertThat(rootDTO, notNullValue());
    RootLinks rootLinks = rootDTO.getLinks();
    assertThat(rootLinks, notNullValue());
    assertThat(rootLinks.getSessions(), notNullValue());
    assertThat(rootLinks.getSessions().getHref(), is("/useradmin/sessions?page=1&perPage=20"));
    assertThat(rootLinks.getSessions().getMethod(), is(MethodEnum.GET));
  }

  @Test
  void buildRootDTO_Should_returnRootDTOWithConsultantHalLinks() {
    RootDTO rootDTO = new RootDTOBuilder().buildRootDTO();

    assertThat(rootDTO, notNullValue());
    RootLinks rootLinks = rootDTO.getLinks();
    assertThat(rootLinks, notNullValue());
    assertThat(rootLinks.getConsultants(), notNullValue());
    assertThat(
        rootLinks.getConsultants().getHref(), is("/useradmin/consultants?page=1&perPage=20"));
    assertThat(rootLinks.getConsultants().getMethod(), is(MethodEnum.GET));
    assertThat(rootLinks.getCreateConsultant(), notNullValue());
    assertThat(rootLinks.getCreateConsultant().getHref(), is("/useradmin/consultants"));
    assertThat(rootLinks.getCreateConsultant().getMethod(), is(MethodEnum.POST));
  }

  @Test
  void buildRootDTO_Should_returnRootDTOWithSelfHalLink() {
    RootDTO rootDTO = new RootDTOBuilder().buildRootDTO();

    assertThat(rootDTO, notNullValue());
    RootLinks rootLinks = rootDTO.getLinks();
    assertThat(rootLinks, notNullValue());
    assertThat(rootLinks.getSelf().getHref(), is("/useradmin"));
    assertThat(rootLinks.getSelf().getMethod(), is(MethodEnum.GET));
  }

  @Test
  void buildRootDTO_Should_returnRootDTOWithConsultantAgenciesHalLinks() {
    RootDTO rootDTO = new RootDTOBuilder().buildRootDTO();

    assertThat(rootDTO, notNullValue());
    RootLinks rootLinks = rootDTO.getLinks();
    assertThat(rootLinks, notNullValue());
    assertThat(rootLinks.getConsultantAgencies(), notNullValue());
    assertThat(
        rootLinks.getConsultantAgencies().getHref(),
        is("/useradmin/consultants/{consultantId}/agencies"));
    assertThat(rootLinks.getConsultantAgencies().getMethod(), is(MethodEnum.GET));
  }

  @Test
  void buildRootDTO_Should_returnRootDTOWithDeleteAskerHalLink() {
    RootDTO rootDTO = new RootDTOBuilder().buildRootDTO();

    assertThat(rootDTO, notNullValue());
    RootLinks rootLinks = rootDTO.getLinks();
    assertThat(rootLinks, notNullValue());
    assertThat(rootLinks.getDeleteAsker(), notNullValue());
    assertThat(rootLinks.getDeleteAsker().getHref(), is("/useradmin/askers/{askerId}"));
    assertThat(rootLinks.getDeleteAsker().getMethod(), is(MethodEnum.DELETE));
  }
}

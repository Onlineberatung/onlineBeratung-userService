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
    assertThat(rootLinks.getSelf().getHref(), is("/useradmin"));
    assertThat(rootLinks.getSelf().getMethod(), is(MethodEnum.GET));
  }

}

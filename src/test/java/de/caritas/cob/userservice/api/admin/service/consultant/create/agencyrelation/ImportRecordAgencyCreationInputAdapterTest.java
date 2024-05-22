package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hibernate.search.util.impl.CollectionHelper.asSet;

import org.junit.jupiter.api.Test;

public class ImportRecordAgencyCreationInputAdapterTest {

  @Test
  public void getter_Should_returnExpectedValues_When_membersAreSet() {
    ConsultantAgencyCreationInput input =
        new ImportRecordAgencyCreationInputAdapter("consultantId", 1L, asSet("role set"));

    assertThat(input.getConsultantId(), is("consultantId"));
    assertThat(input.getAgencyId(), is(1L));
    assertThat(input.getRoleSetNames().iterator().next(), is("role set"));
    assertThat(input.getCreateDate(), notNullValue());
    assertThat(input.getUpdateDate(), notNullValue());
  }
}

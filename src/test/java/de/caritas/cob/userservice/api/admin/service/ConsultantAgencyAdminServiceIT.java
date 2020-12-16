package de.caritas.cob.userservice.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantAgencyAdminServiceIT {

  @Autowired
  private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Test
  public void findConsultantAgencies_Should_returnAllConsultantAgenciesForGivenConsultantId_with_correctConsultantId() {

    ConsultantAgencyAdminResultDTO consultantAgencies = consultantAgencyAdminService
        .findConsultantAgencies("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe");

    assertThat(consultantAgencies, notNullValue());
    assertThat(consultantAgencies.getEmbedded(), hasSize(24));
  }

  @Test
  public void findConsultantAgencies_Should_returnFullMappedSessionAdminDTO() {

    ConsultantAgencyAdminResultDTO consultantAgencies = consultantAgencyAdminService
        .findConsultantAgencies("5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe");

    ConsultantAgencyAdminDTO consultantAgencyAdminDTO = consultantAgencies.getEmbedded().iterator()
        .next();
    assertThat(consultantAgencyAdminDTO.getConsultantId(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getAgencyId(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getCreateDate(), notNullValue());
    assertThat(consultantAgencyAdminDTO.getUpdateDate(), notNullValue());
  }

  @Test
  public void findConsultantAgencies_Should_returnEmptyResult_with_incorrectConsultantId() {

    ConsultantAgencyAdminResultDTO consultantAgencies = consultantAgencyAdminService
        .findConsultantAgencies("5674839f-1234-47e2-8f9c-bb49fc2ddbbe");

    assertThat(consultantAgencies, notNullValue());
    assertThat(consultantAgencies.getEmbedded(), hasSize(0));
  }

}

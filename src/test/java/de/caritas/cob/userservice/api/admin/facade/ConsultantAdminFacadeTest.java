package de.caritas.cob.userservice.api.admin.facade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.admin.service.ConsultantAgencyAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminFilterService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminService;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.model.ConsultantFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantAdminFacadeTest {

  @InjectMocks
  private ConsultantAdminFacade consultantAdminFacade;

  @Mock
  private ConsultantAdminService consultantAdminService;

  @Mock
  private ConsultantAdminFilterService consultantAdminFilterService;

  @Mock
  private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Test
  public void findConsultant_Should_useConsultantAdminService() {
    this.consultantAdminFacade.findConsultant("");

    verify(this.consultantAdminService, times(1)).findConsultantById(any());
  }

  @Test
  public void findFilteredConsultants_Should_useConsultantAdminFilterService() {
    this.consultantAdminFacade.findFilteredConsultants(1, 1, new ConsultantFilter());

    verify(this.consultantAdminFilterService, times(1))
        .findFilteredConsultants(eq(1), eq(1), any());
  }

  @Test
  public void findConsultantAgencies_Should_useConsultantAdminFilterService() {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    this.consultantAgencyAdminService.findConsultantAgencies(consultantId);

    verify(this.consultantAgencyAdminService, times(1))
        .findConsultantAgencies(eq(consultantId));
  }

  @Test
  public void createNewConsultant_Should_useConsultantAdminServiceCorrectly() {
    this.consultantAdminFacade.createNewConsultant(null);

    verify(this.consultantAdminService, times(1)).createNewConsultant(null);
  }

  @Test
  public void findConsultantAgencies_Should_useConsultantAgencyAdminServiceCorrectly() {
    this.consultantAdminFacade.findConsultantAgencies(null);

    verify(this.consultantAgencyAdminService, times(1)).findConsultantAgencies(null);
  }

  @Test
  public void updateConsultant_Should_useConsultantAdminServiceCorrectly() {
    this.consultantAdminFacade.updateConsultant(null, null);

    verify(this.consultantAdminService, times(1)).updateConsultant(any(), any());
  }

}

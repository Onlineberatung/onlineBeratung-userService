package de.caritas.cob.userservice.api.admin.facade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminFilterService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminService;
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

}

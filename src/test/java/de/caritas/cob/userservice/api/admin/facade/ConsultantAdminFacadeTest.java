package de.caritas.cob.userservice.api.admin.facade;

import static de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO.AgencyTypeEnum.DEFAULT_AGENCY;
import static de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO.AgencyTypeEnum.TEAM_AGENCY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.FieldEnum;
import de.caritas.cob.userservice.api.admin.service.agency.ConsultantAgencyAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminFilterService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation.ConsultantAgencyRelationCreatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultantAdminFacadeTest {

  @InjectMocks private ConsultantAdminFacade consultantAdminFacade;

  @Mock private ConsultantAdminService consultantAdminService;

  @Mock private ConsultantAdminFilterService consultantAdminFilterService;

  @Mock private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Mock private ConsultantAgencyRelationCreatorService relationCreatorService;

  @Test
  void findConsultant_Should_useConsultantAdminService() {
    this.consultantAdminFacade.findConsultant("");

    verify(this.consultantAdminService).findConsultantById(any());
  }

  @Test
  void findFilteredConsultants_Should_useConsultantAdminFilterService() {
    this.consultantAdminFacade.findFilteredConsultants(
        1, 1, new ConsultantFilter(), new Sort().field(FieldEnum.EMAIL));

    verify(this.consultantAdminFilterService).findFilteredConsultants(eq(1), eq(1), any(), any());
  }

  @Test
  void findConsultantAgencies_Should_useConsultantAdminFilterService() {
    var consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    this.consultantAgencyAdminService.findConsultantAgencies(consultantId);

    verify(this.consultantAgencyAdminService).findConsultantAgencies(consultantId);
  }

  @Test
  void createNewConsultant_Should_useConsultantAdminServiceCorrectly() {
    this.consultantAdminFacade.createNewConsultant(null);

    verify(this.consultantAdminService).createNewConsultant(null);
  }

  @Test
  void findConsultantAgencies_Should_useConsultantAgencyAdminServiceCorrectly() {
    this.consultantAdminFacade.findConsultantAgencies(null);

    verify(this.consultantAgencyAdminService).findConsultantAgencies(null);
  }

  @Test
  void createNewConsultantAgency_Should_useConsultantAgencyAdminServiceCorrectly() {
    this.consultantAdminFacade.createNewConsultantAgency(null, null);

    verify(this.relationCreatorService).createNewConsultantAgency(null, null);
  }

  @Test
  void updateConsultant_Should_useConsultantAdminServiceCorrectly() {
    this.consultantAdminFacade.updateConsultant(null, null);

    verify(this.consultantAdminService).updateConsultant(any(), any());
  }

  @Test
  void
      changeAgencyType_Should_callMarkAllAssignedConsultantsAsTeamConsultant_When_typeIsTeamAgency() {
    this.consultantAdminFacade.changeAgencyType(1L, new AgencyTypeDTO().agencyType(TEAM_AGENCY));

    verify(this.consultantAgencyAdminService).markAllAssignedConsultantsAsTeamConsultant(1L);
  }

  @Test
  void
      changeAgencyType_Should_callRemoveConsultantsFromTeamSessionsByAgencyId_When_typeIsDefaultAgency() {
    this.consultantAdminFacade.changeAgencyType(1L, new AgencyTypeDTO().agencyType(DEFAULT_AGENCY));

    verify(this.consultantAgencyAdminService).removeConsultantsFromTeamSessionsByAgencyId(1L);
  }

  @Test
  void markConsultantAgencyForDeletion_Should_callMarkConsultantAgencyForDeletion() {
    this.consultantAdminFacade.markConsultantAgencyForDeletion("1", 1L);

    verify(this.consultantAgencyAdminService).markConsultantAgencyForDeletion("1", 1L);
  }

  @Test
  void markConsultantForDeletion_Should_callMarkConsultantForDeletion() {
    this.consultantAdminFacade.markConsultantForDeletion("1");

    verify(this.consultantAdminService).markConsultantForDeletion("1");
  }

  @Test
  void findConsultantsForAgency_Should_callConsultantAgencyAdminService() {
    this.consultantAdminFacade.findConsultantsForAgency("1");

    verify(this.consultantAgencyAdminService).findConsultantsForAgency(1L);
  }
}

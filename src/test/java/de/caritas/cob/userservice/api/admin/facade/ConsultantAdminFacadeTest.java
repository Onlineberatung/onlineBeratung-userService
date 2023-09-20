package de.caritas.cob.userservice.api.admin.facade;

import static de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO.AgencyTypeEnum.DEFAULT_AGENCY;
import static de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO.AgencyTypeEnum.TEAM_AGENCY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.FieldEnum;
import de.caritas.cob.userservice.api.admin.service.agency.ConsultantAgencyAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminFilterService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation.ConsultantAgencyRelationCreatorService;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConsultantAdminFacadeTest {

  public static final Long AGENCY_ID_1 = 1L;
  public static final Long AGENCY_ID_2 = 2L;
  @InjectMocks private ConsultantAdminFacade consultantAdminFacade;

  @Mock private ConsultantAdminService consultantAdminService;

  @Mock private ConsultantAdminFilterService consultantAdminFilterService;

  @Mock private ConsultantAgencyAdminService consultantAgencyAdminService;

  @Mock private ConsultantAgencyRelationCreatorService relationCreatorService;

  @Mock private AuthenticatedUser authenticatedUser;

  @Mock private AdminUserFacade adminUserFacade;

  @Mock private AgencyService agencyService;

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
    this.consultantAdminFacade.markConsultantForDeletion("1", true);

    verify(this.consultantAdminService).markConsultantForDeletion("1", true);
  }

  @Test
  void findConsultantsForAgency_Should_callConsultantAgencyAdminService() {
    this.consultantAdminFacade.findConsultantsForAgency("1");

    verify(this.consultantAgencyAdminService).findConsultantsForAgency(1L);
  }

  @Test
  void checkPermissionsToUpdateAgencies_Should_PassIfUserDoesntHaveRestrictedPermissions() {
    consultantAdminFacade.checkPermissionsToAssignedAgencies(
        Lists.newArrayList(new CreateConsultantAgencyDTO().agencyId(1L)));

    verify(authenticatedUser).hasRestrictedAgencyPriviliges();
  }

  @Test
  void
      checkPermissionsToUpdateAgencies_Should_PassIfUserHasRestrictedPermissionsAndHasPermissionsForTheGivenAgency() {
    when(adminUserFacade.findAdminUserAgencyIds(authenticatedUser.getUserId()))
        .thenReturn(Lists.newArrayList(1L, 2L, 3L));
    when(authenticatedUser.hasRestrictedAgencyPriviliges()).thenReturn(true);
    consultantAdminFacade.checkPermissionsToAssignedAgencies(
        Lists.newArrayList(new CreateConsultantAgencyDTO().agencyId(1L)));
    verify(authenticatedUser).hasRestrictedAgencyPriviliges();
  }

  @Test
  void
      checkPermissionsToUpdateAgencies_Should_ThrowForbiddenExceptionIfUserHasRestrictedPermissionsAndDoesntHavePermissionsForTheGivenAgency() {
    when(adminUserFacade.findAdminUserAgencyIds(authenticatedUser.getUserId()))
        .thenReturn(Lists.newArrayList(1L, 2L, 3L));
    when(authenticatedUser.hasRestrictedAgencyPriviliges()).thenReturn(true);

    ArrayList<CreateConsultantAgencyDTO> agencyList =
        Lists.newArrayList(new CreateConsultantAgencyDTO().agencyId(4L));
    assertThrows(
        ForbiddenException.class,
        () -> consultantAdminFacade.checkPermissionsToAssignedAgencies(agencyList));
  }

  @Test
  void
      checkAssignedAgenciesMatchConsultantTenant_Should_Throw_BadRequestException_When_TenantDoesNotMatch() {
    // given
    ReflectionTestUtils.setField(consultantAdminFacade, "multiTenancyEnabled", true);
    ConsultantAdminResponseDTO consultant =
        new ConsultantAdminResponseDTO().embedded(new ConsultantDTO());
    consultant.getEmbedded().setTenantId(1);

    when(consultantAdminService.findConsultantById("consultantId")).thenReturn(consultant);

    List<CreateConsultantAgencyDTO> agencyList = new ArrayList<>();
    CreateConsultantAgencyDTO agency1 = new CreateConsultantAgencyDTO();
    agency1.setAgencyId(AGENCY_ID_1);
    agencyList.add(agency1);

    CreateConsultantAgencyDTO agency2 = new CreateConsultantAgencyDTO();
    agency2.setAgencyId(AGENCY_ID_2);
    agencyList.add(agency2);

    when(agencyService.getAgency(AGENCY_ID_1)).thenReturn(createAgencyWithTenant(1L));
    when(agencyService.getAgency(AGENCY_ID_2)).thenReturn(createAgencyWithTenant(2L));

    // when, then
    assertThrows(
        BadRequestException.class,
        () -> {
          consultantAdminFacade.checkAssignedAgenciesMatchConsultantTenant(
              "consultantId", agencyList);
        });

    ReflectionTestUtils.setField(consultantAdminFacade, "multiTenancyEnabled", false);
  }

  @Test
  void checkAssignedAgenciesMatchConsultantTenant_Should_PassCheck_When_TenantMatches() {
    // given
    ReflectionTestUtils.setField(consultantAdminFacade, "multiTenancyEnabled", true);
    ConsultantAdminResponseDTO consultant =
        new ConsultantAdminResponseDTO().embedded(new ConsultantDTO());
    consultant.getEmbedded().setTenantId(1);

    when(consultantAdminService.findConsultantById("consultantId")).thenReturn(consultant);

    List<CreateConsultantAgencyDTO> agencyList = new ArrayList<>();
    CreateConsultantAgencyDTO agency1 = new CreateConsultantAgencyDTO();
    agency1.setAgencyId(AGENCY_ID_1);
    agencyList.add(agency1);

    CreateConsultantAgencyDTO agency2 = new CreateConsultantAgencyDTO();
    agency2.setAgencyId(AGENCY_ID_2);
    agencyList.add(agency2);

    when(agencyService.getAgency(AGENCY_ID_1)).thenReturn(createAgencyWithTenant(1L));
    when(agencyService.getAgency(AGENCY_ID_2)).thenReturn(createAgencyWithTenant(1L));

    // when
    consultantAdminFacade.checkAssignedAgenciesMatchConsultantTenant("consultantId", agencyList);

    // then
    Mockito.verify(agencyService, times(2)).getAgency(any());

    // tear down
    ReflectionTestUtils.setField(consultantAdminFacade, "multiTenancyEnabled", false);
  }

  @Test
  void checkAssignedAgenciesMatchConsultantTenant_Should_PassCheck_When_MultitenancyIsDisabled() {
    // given
    ReflectionTestUtils.setField(consultantAdminFacade, "multiTenancyEnabled", false);

    List<CreateConsultantAgencyDTO> agencyList = new ArrayList<>();
    CreateConsultantAgencyDTO agency1 = new CreateConsultantAgencyDTO();
    agency1.setAgencyId(AGENCY_ID_1);
    agencyList.add(agency1);

    CreateConsultantAgencyDTO agency2 = new CreateConsultantAgencyDTO();
    agency2.setAgencyId(AGENCY_ID_2);
    agencyList.add(agency2);

    // when
    consultantAdminFacade.checkAssignedAgenciesMatchConsultantTenant("consultantId", agencyList);

    // then
    Mockito.verifyNoInteractions(agencyService);
  }

  private AgencyDTO createAgencyWithTenant(Long tenantId) {
    AgencyDTO agency = new AgencyDTO();
    agency.setTenantId(tenantId);
    return agency;
  }
}

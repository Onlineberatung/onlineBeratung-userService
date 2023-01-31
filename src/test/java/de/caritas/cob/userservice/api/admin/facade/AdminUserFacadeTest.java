package de.caritas.cob.userservice.api.admin.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateTenantAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.AdminAgencyRelationService;
import de.caritas.cob.userservice.api.admin.service.admin.AgencyAdminUserService;
import de.caritas.cob.userservice.api.admin.service.admin.TenantAdminUserService;
import de.caritas.cob.userservice.api.admin.service.admin.search.AdminFilterService;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUserFacadeTest {

  @InjectMocks private AdminUserFacade adminUserFacade;
  @Mock private AgencyAdminUserService agencyAdminUserService;
  @Mock private AdminAgencyRelationService adminAgencyRelationService;
  @Mock private AdminFilterService adminFilterService;

  @Mock private TenantAdminUserService tenantAdminUserService;

  @Mock private AuthenticatedUser authenticatedUser;

  @Test
  void findAgencyAdmin_Should_useAdminAgencyService() {
    final String adminId = "123";

    this.adminUserFacade.findAgencyAdmin(adminId);

    verify(this.agencyAdminUserService).findAgencyAdmin(adminId);
  }

  @Test
  void createNewAdminAgency_Should_useAdminAgencyService() {
    CreateAdminDTO createAgencyAdminDTO = mock(CreateAdminDTO.class);

    this.adminUserFacade.createNewAgencyAdmin(createAgencyAdminDTO);

    verify(this.agencyAdminUserService).createNewAgencyAdmin(createAgencyAdminDTO);
  }

  @Test
  void createNewTenantAdmin_Should_useAdminAgencyService() {
    CreateAdminDTO createAdminDTO = mock(CreateAdminDTO.class);

    this.adminUserFacade.createNewTenantAdmin(createAdminDTO);

    verify(this.tenantAdminUserService).createNewTenantAdmin(createAdminDTO);
  }

  @Test
  void updateAgencyAdmin_Should_useAdminAgencyService() {
    String adminId = "123";
    UpdateAgencyAdminDTO updateAgencyAdminDTO = mock(UpdateAgencyAdminDTO.class);

    this.adminUserFacade.updateAgencyAdmin(adminId, updateAgencyAdminDTO);

    verify(this.agencyAdminUserService).updateAgencyAdmin(adminId, updateAgencyAdminDTO);
  }

  @Test
  void updateTenantAdmin_Should_useTenantAdminUserService() {
    String adminId = "123";
    UpdateTenantAdminDTO updateTenantAdminDTO = mock(UpdateTenantAdminDTO.class);

    this.adminUserFacade.updateTenantAdmin(adminId, updateTenantAdminDTO);

    verify(this.tenantAdminUserService).updateTenantAdmin(adminId, updateTenantAdminDTO);
  }

  @Test
  void deleteAgencyAdmin_Should_useAdminAgencyService() {
    String adminId = "123";

    this.adminUserFacade.deleteAgencyAdmin(adminId);

    verify(this.agencyAdminUserService).deleteAgencyAdmin(adminId);
  }

  @Test
  void deleteTenantAdmin_Should_useAdminAgencyService() {
    String adminId = "123";

    this.adminUserFacade.deleteTenantAdmin(adminId);

    verify(this.tenantAdminUserService).deleteTenantAdmin(adminId);
  }

  @Test
  void findAdminUserAgencyIds_Should_useAdminAgencyService() {
    String adminId = "123";

    this.adminUserFacade.findAdminUserAgencyIds(adminId);

    verify(this.agencyAdminUserService).findAgenciesOfAdmin(adminId);
  }

  @Test
  void createNewAdminAgencyRelation_Should_useAdminAgencyRelationService() {
    String adminId = "123";
    CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO =
        mock(CreateAdminAgencyRelationDTO.class);

    this.adminUserFacade.createNewAdminAgencyRelation(adminId, createAdminAgencyRelationDTO);

    verify(this.adminAgencyRelationService)
        .createAdminAgencyRelation(adminId, createAdminAgencyRelationDTO);
  }

  @Test
  void deleteAdminAgencyRelation_Should_useAdminAgencyRelationService() {
    String adminId = "123";
    long agencyId = 2L;

    this.adminUserFacade.deleteAdminAgencyRelation(adminId, agencyId);

    verify(this.adminAgencyRelationService).deleteAdminAgencyRelation(adminId, agencyId);
  }

  @Test
  void setAdminAgenciesRelation_Should_useAdminAgencyRelationService() {
    String adminId = "123";
    List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOS = new ArrayList<>();

    this.adminUserFacade.setAdminAgenciesRelation(adminId, newAdminAgencyRelationDTOS);

    verify(this.adminAgencyRelationService)
        .synchronizeAdminAgenciesRelation(adminId, newAdminAgencyRelationDTOS);
  }

  @Test
  void patchAdminData_Should_patchAgencyAdminForAgencies() {
    when(authenticatedUser.getUserId()).thenReturn("adminId");
    when(authenticatedUser.isRestrictedAgencyAdmin()).thenReturn(true);
    when(authenticatedUser.isSingleTenantAdmin()).thenReturn(false);

    this.adminUserFacade.patchAdminUserData(
        new PatchAdminDTO()
            .firstname("updated firstname")
            .lastname("updated lastname")
            .email("updated email"));

    ArgumentCaptor<PatchAdminDTO> captor = ArgumentCaptor.forClass(PatchAdminDTO.class);
    verify(this.agencyAdminUserService).patchAgencyAdmin(eq("adminId"), captor.capture());

    PatchAdminDTO value = captor.getValue();
    assertThat(value.getFirstname()).isEqualTo("updated firstname");
    assertThat(value.getLastname()).isEqualTo("updated lastname");
    assertThat(value.getEmail()).isEqualTo("updated email");
  }

  @Test
  void findFilteredAdminsAgency_Should_useAdminAgencyRelationService_and_useAdminFilterService() {
    Integer page = 1;
    Integer perPage = 10;
    AdminFilter adminFilter = mock(AdminFilter.class);
    Sort sort = mock(Sort.class);
    AdminSearchResultDTO agencyAdminSearchResultDTO = mock(AdminSearchResultDTO.class);
    AdminResponseDTO adminResponseDTO = mock(AdminResponseDTO.class);
    when(adminResponseDTO.getEmbedded()).thenReturn(mock(AdminDTO.class));
    when(agencyAdminSearchResultDTO.getEmbedded()).thenReturn(List.of(adminResponseDTO));

    when(adminFilterService.findFilteredAdmins(page, perPage, adminFilter, sort))
        .thenReturn(agencyAdminSearchResultDTO);

    this.adminUserFacade.findFilteredAdminsAgency(page, perPage, adminFilter, sort);

    verify(this.adminFilterService).findFilteredAdmins(page, perPage, adminFilter, sort);
    verify(this.adminAgencyRelationService).appendAgenciesForAdmins(anySet());
  }
}

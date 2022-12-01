package de.caritas.cob.userservice.api.admin.facade;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.admin.service.admin.AdminAgencyRelationService;
import de.caritas.cob.userservice.api.admin.service.admin.AdminAgencyService;
import de.caritas.cob.userservice.api.admin.service.admin.search.AdminFilterService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminAgencyFacadeTest {

  @InjectMocks private AdminAgencyFacade adminAgencyFacade;
  @Mock private AdminAgencyService adminAgencyService;
  @Mock private AdminAgencyRelationService adminAgencyRelationService;
  @Mock private AdminFilterService adminFilterService;

  @Test
  void findAgencyAdmin_Should_useAdminAgencyService() {
    final String adminId = "123";

    this.adminAgencyFacade.findAgencyAdmin(adminId);

    verify(this.adminAgencyService).findAgencyAdmin(adminId);
  }

  @Test
  void createNewAdminAgency_Should_useAdminAgencyService() {
    CreateAgencyAdminDTO createAgencyAdminDTO = mock(CreateAgencyAdminDTO.class);

    this.adminAgencyFacade.createNewAdminAgency(createAgencyAdminDTO);

    verify(this.adminAgencyService).createNewAdminAgency(createAgencyAdminDTO);
  }

  @Test
  void updateAgencyAdmin_Should_useAdminAgencyService() {
    String adminId = "123";
    UpdateAgencyAdminDTO updateAgencyAdminDTO = mock(UpdateAgencyAdminDTO.class);

    this.adminAgencyFacade.updateAgencyAdmin(adminId, updateAgencyAdminDTO);

    verify(this.adminAgencyService).updateAgencyAdmin(adminId, updateAgencyAdminDTO);
  }

  @Test
  void deleteAgencyAdmin_Should_useAdminAgencyService() {
    String adminId = "123";

    this.adminAgencyFacade.deleteAgencyAdmin(adminId);

    verify(this.adminAgencyService).deleteAgencyAdmin(adminId);
  }

  @Test
  void findAdminUserAgencyIds_Should_useAdminAgencyService() {
    String adminId = "123";

    this.adminAgencyFacade.findAdminUserAgencyIds(adminId);

    verify(this.adminAgencyService).findAgenciesOfAdmin(adminId);
  }

  @Test
  void createNewAdminAgencyRelation_Should_useAdminAgencyRelationService() {
    String adminId = "123";
    CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO =
        mock(CreateAdminAgencyRelationDTO.class);

    this.adminAgencyFacade.createNewAdminAgencyRelation(adminId, createAdminAgencyRelationDTO);

    verify(this.adminAgencyRelationService)
        .createAdminAgencyRelation(adminId, createAdminAgencyRelationDTO);
  }

  @Test
  void deleteAdminAgencyRelation_Should_useAdminAgencyRelationService() {
    String adminId = "123";
    long agencyId = 2L;

    this.adminAgencyFacade.deleteAdminAgencyRelation(adminId, agencyId);

    verify(this.adminAgencyRelationService).deleteAdminAgencyRelation(adminId, agencyId);
  }

  @Test
  void setAdminAgenciesRelation_Should_useAdminAgencyRelationService() {
    String adminId = "123";
    List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOS = new ArrayList<>();

    this.adminAgencyFacade.setAdminAgenciesRelation(adminId, newAdminAgencyRelationDTOS);

    verify(this.adminAgencyRelationService)
        .synchronizeAdminAgenciesRelation(adminId, newAdminAgencyRelationDTOS);
  }

  @Test
  void findFilteredAdminsAgency_Should_useAdminAgencyRelationService_and_useAdminFilterService() {
    Integer page = 1;
    Integer perPage = 10;
    AdminFilter adminFilter = mock(AdminFilter.class);
    Sort sort = mock(Sort.class);
    AgencyAdminSearchResultDTO agencyAdminSearchResultDTO = mock(AgencyAdminSearchResultDTO.class);
    AdminResponseDTO adminResponseDTO = mock(AdminResponseDTO.class);
    when(adminResponseDTO.getEmbedded()).thenReturn(mock(AdminDTO.class));
    when(agencyAdminSearchResultDTO.getEmbedded()).thenReturn(List.of(adminResponseDTO));

    when(adminFilterService.findFilteredAdmins(page, perPage, adminFilter, sort))
        .thenReturn(agencyAdminSearchResultDTO);

    this.adminAgencyFacade.findFilteredAdminsAgency(page, perPage, adminFilter, sort);

    verify(this.adminFilterService).findFilteredAdmins(page, perPage, adminFilter, sort);
    verify(this.adminAgencyRelationService).appendAgenciesForAdmins(anySet());
  }
}

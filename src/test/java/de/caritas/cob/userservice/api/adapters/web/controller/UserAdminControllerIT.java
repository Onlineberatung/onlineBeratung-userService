package de.caritas.cob.userservice.api.adapters.web.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.AdminDtoMapper;
import de.caritas.cob.userservice.api.admin.facade.AdminUserFacade;
import de.caritas.cob.userservice.api.admin.facade.AskerUserAdminFacade;
import de.caritas.cob.userservice.api.admin.facade.ConsultantAdminFacade;
import de.caritas.cob.userservice.api.admin.report.service.ViolationReportGenerator;
import de.caritas.cob.userservice.api.admin.service.session.SessionAdminService;
import de.caritas.cob.userservice.api.config.auth.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.ArrayList;
import java.util.UUID;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class UserAdminControllerIT {

  protected static final String ROOT_PATH = "/useradmin";
  protected static final String SESSION_PATH = ROOT_PATH + "/sessions";
  protected static final String REPORT_PATH = ROOT_PATH + "/report";
  protected static final String FILTERED_CONSULTANTS_PATH = ROOT_PATH + "/consultants";
  protected static final String CONSULTANT_PATH = ROOT_PATH + "/consultants/";
  protected static final String DELETE_CONSULTANT_PATH = CONSULTANT_PATH + "1234";
  protected static final String DELETE_ASKER_PATH = ROOT_PATH + "/askers/1234";
  protected static final String CONSULTANT_AGENCIES_PATH = ROOT_PATH + "/consultants/%s/agencies";
  protected static final String CONSULTANT_AGENCY_PATH = ROOT_PATH + "/consultants/%s/agencies";
  protected static final String AGENCY_CONSULTANT_PATH = ROOT_PATH + "/agencies/%s/consultants";
  protected static final String DELETE_CONSULTANT_AGENCY_PATH =
      ROOT_PATH + "/consultants/%s" + "/agencies/%s";
  protected static final String AGENCY_ADMIN_PATH = ROOT_PATH + "/agencyadmins/";

  protected static final String ADMIN_DATA_PATH = ROOT_PATH + "/data/";

  protected static final String TENANT_ADMIN_PATH_WITHOUT_SLASH = ROOT_PATH + "/tenantadmins";
  protected static final String TENANT_ADMIN_PATH = TENANT_ADMIN_PATH_WITHOUT_SLASH + "/";
  protected static final String DELETE_AGENCY_ADMIN_PATH = AGENCY_ADMIN_PATH + "%s";
  protected static final String AGENCIES_OF_ADMIN_PATH = ROOT_PATH + "/agencyadmins/%s/agencies";
  protected static final String DELETE_ADMIN_AGENCY_PATH = AGENCIES_OF_ADMIN_PATH + "/%s";

  protected static final String AGENCY_CHANGE_TYPE_PATH = ROOT_PATH + "/agency/1/changetype";
  protected static final String PAGE_PARAM = "page";
  protected static final String PER_PAGE_PARAM = "perPage";

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final EasyRandom easyRandom = new EasyRandom();

  @Autowired private MockMvc mvc;

  @MockBean private SessionAdminService sessionAdminService;

  @MockBean private ConsultantAdminFacade consultantAdminFacade;

  @MockBean private ViolationReportGenerator violationReportGenerator;

  @MockBean
  @SuppressWarnings("unused")
  private LinkDiscoverers linkDiscoverers;

  @MockBean
  @SuppressWarnings("unused")
  private RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper;

  @MockBean private AskerUserAdminFacade askerUserAdminFacade;

  @MockBean private AppointmentService appointmentService;

  @MockBean private AdminUserFacade adminUserFacade;

  @MockBean private AdminDtoMapper adminDtoMapper;

  @MockBean private AuthenticatedUser authenticatedUser;

  @MockBean private KeycloakConfigResolver keycloakConfigResolver;

  @Test
  void getSessions_Should_returnBadRequest_When_requiredPaginationParamsAreMissing()
      throws Exception {
    this.mvc.perform(get(SESSION_PATH)).andExpect(status().isBadRequest());
  }

  @Test
  void getRoot_Should_returnExpectedRootDTO() throws Exception {
    this.mvc
        .perform(get(ROOT_PATH))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._links").exists())
        .andExpect(jsonPath("$._links.self").exists())
        .andExpect(jsonPath("$._links.self.href", endsWith("/useradmin")))
        .andExpect(jsonPath("$._links.sessions").exists())
        .andExpect(
            jsonPath("$._links.sessions.href", endsWith("/useradmin/sessions?page=1&perPage=20")))
        .andExpect(jsonPath("$._links.consultants").exists())
        .andExpect(
            jsonPath(
                "$._links.consultants.href", endsWith("/useradmin/consultants?page=1&perPage=20")));
  }

  @Test
  void getSessions_Should_returnOk_When_requiredPaginationParamsAreGiven() throws Exception {
    this.mvc
        .perform(get(SESSION_PATH).param(PAGE_PARAM, "0").param(PER_PAGE_PARAM, "1"))
        .andExpect(status().isOk());

    verify(this.sessionAdminService, times(1)).findSessions(eq(0), eq(1), any());
  }

  @Test
  void getConsultantAgency_Should_returnOk_When_requiredConsultantIdParamIsGiven()
      throws Exception {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    String consultantAgencyPath = String.format(CONSULTANT_AGENCIES_PATH, consultantId);

    this.mvc.perform(get(consultantAgencyPath)).andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).findConsultantAgencies(consultantId);
  }

  @Test
  void generateReport_Should_returnOk() throws Exception {
    this.mvc.perform(get(REPORT_PATH)).andExpect(status().isOk());

    verify(this.violationReportGenerator, times(1)).generateReport();
  }

  @Test
  void getConsultants_Should_returnBadRequest_When_requiredPaginationParamsAreMissing()
      throws Exception {
    this.mvc.perform(get(FILTERED_CONSULTANTS_PATH)).andExpect(status().isBadRequest());
  }

  @Test
  void getConsultants_Should_returnOk_When_requiredPaginationParamsAreGiven() throws Exception {
    this.mvc
        .perform(get(FILTERED_CONSULTANTS_PATH).param(PAGE_PARAM, "0").param(PER_PAGE_PARAM, "1"))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .findFilteredConsultants(eq(0), eq(1), any(), any());
  }

  @Test
  void getConsultant_Should_returnOk_When_requiredConsultantIdParamIsGiven() throws Exception {
    this.mvc.perform(get(CONSULTANT_PATH + "consultantId")).andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).findConsultant("consultantId");
  }

  @Test
  void getConsultant_Should_returnNoContent_When_requiredConsultantDoesNotExist() throws Exception {
    when(this.consultantAdminFacade.findConsultant(any())).thenThrow(new NoContentException(""));

    this.mvc.perform(get(CONSULTANT_PATH + "consultantId")).andExpect(status().isNoContent());
  }

  @Test
  void createConsultant_Should_returnOk_When_requiredCreateConsultantIsGiven() throws Exception {
    CreateConsultantDTO createConsultantDTO =
        new EasyRandom().nextObject(CreateConsultantDTO.class);

    this.mvc
        .perform(
            post(CONSULTANT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createConsultantDTO)))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).createNewConsultant(any());
  }

  @Test
  void createConsultant_Should_returnBadRequest_When_requiredCreateConsultantIsMissing()
      throws Exception {
    this.mvc
        .perform(post(CONSULTANT_PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateConsultant_Should_returnOk_When_requiredCreateConsultantIsGiven() throws Exception {
    UpdateAdminConsultantDTO updateConsultantDTO =
        new EasyRandom().nextObject(UpdateAdminConsultantDTO.class);

    this.mvc
        .perform(
            put(CONSULTANT_PATH + "consultantId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateConsultantDTO)))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).updateConsultant(anyString(), any());
  }

  @Test
  void updateConsultant_Should_returnBadRequest_When_requiredParamsAreMissing() throws Exception {
    this.mvc
        .perform(put(CONSULTANT_PATH + "consultantId").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createConsultantAgency_Should_returnCreated_When_requiredParamsAreGiven() throws Exception {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    String consultantAgencyPath = String.format(CONSULTANT_AGENCY_PATH, consultantId);

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRoleSetKey("role set");

    this.mvc
        .perform(
            post(consultantAgencyPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createConsultantAgencyDTO)))
        .andExpect(status().isCreated());

    verify(this.consultantAdminFacade, times(1))
        .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
  }

  @Test
  void setConsultantAgencies_ShouldReturnOk_When_RequiredParamsAreGiven() throws Exception {
    var consultantId = UUID.randomUUID().toString();
    var agencies = givenAgenciesToSet();

    mvc.perform(
            put("/useradmin/consultants/{consultantId}/agencies", consultantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agencies)))
        .andExpect(status().isOk());

    verify(consultantAdminFacade).markConsultantAgenciesForDeletion(any(), anyList());
    verify(consultantAdminFacade).filterAgencyListForCreation(any(), anyList());
    verify(consultantAdminFacade).prepareConsultantAgencyRelation(any(), anyList());
    verify(consultantAdminFacade).completeConsultantAgencyAssigment(any(), anyList());
    verify(this.appointmentService).syncAgencies(any(), anyList());
  }

  @Test
  void
      setConsultantAgencies_Should_ReturnForbiddenIfUserDoesNotHavePermissionsToTheRequestedAgency()
          throws Exception {
    var consultantId = UUID.randomUUID().toString();

    doThrow(new ForbiddenException(""))
        .when(consultantAdminFacade)
        .checkPermissionsToAssignedAgencies(Mockito.anyList());
    var agencies = givenAgenciesToSet();

    mvc.perform(
            put("/useradmin/consultants/{consultantId}/agencies", consultantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agencies)))
        .andExpect(status().isForbidden());
  }

  @Test
  void changeAgencyType_Should_returnOk_When_parametersAreValid() throws Exception {
    this.mvc
        .perform(post(AGENCY_CHANGE_TYPE_PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).changeAgencyType(any(), any());
  }

  @Test
  void deleteConsultantAgency_Should_returnOk_When_requiredParamsAreGiven() throws Exception {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";
    Long agencyId = 1L;

    String consultantAgencyDeletePath =
        String.format(DELETE_CONSULTANT_AGENCY_PATH, consultantId, agencyId);

    this.mvc
        .perform(delete(consultantAgencyDeletePath).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .markConsultantAgencyForDeletion(consultantId, agencyId);
  }

  @Test
  void deleteConsultant_Should_returnOk_When_requiredParamIsGiven() throws Exception {
    this.mvc
        .perform(delete(DELETE_CONSULTANT_PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).markConsultantForDeletion("1234", null);
  }

  @Test
  void deleteConsultant_Should_returnOk_When_requiredParamIsGivenAndForceDeleteToTrue()
      throws Exception {
    this.mvc
        .perform(
            delete(DELETE_CONSULTANT_PATH + "?forceDeleteSessions=true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).markConsultantForDeletion("1234", true);
  }

  @Test
  void deleteAsker_Should_returnOk_When_requiredParamIsGiven() throws Exception {
    this.mvc
        .perform(delete(DELETE_ASKER_PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.askerUserAdminFacade, times(1)).markAskerForDeletion("1234");
  }

  @Test
  void getAgencyConsultants_Should_returnOk_When_requiredAgencyIdParamIsGiven() throws Exception {
    var agencyId = "1";
    var agencyConsultantsPath = String.format(AGENCY_CONSULTANT_PATH, agencyId);

    this.mvc.perform(get(agencyConsultantsPath)).andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).findConsultantsForAgency(agencyId);
  }

  @Test
  void getAgencyAdmins_Should_returnBadRequest_When_requiredPaginationParamsAreMissing()
      throws Exception {
    this.mvc.perform(get(AGENCY_ADMIN_PATH)).andExpect(status().isBadRequest());
  }

  @Test
  void getAgencyAdmins_Should_returnOk_When_requiredPaginationParamsAreGiven() throws Exception {
    // given
    // when
    this.mvc
        .perform(get(AGENCY_ADMIN_PATH).param(PAGE_PARAM, "0").param(PER_PAGE_PARAM, "1"))
        .andExpect(status().isOk());

    // then
    verify(this.adminUserFacade, times(1)).findFilteredAdminsAgency(eq(0), eq(1), any(), any());
  }

  @Test
  void getAgencyAdmin_Should_returnOk_When_requiredAdminIdParamIsGiven() throws Exception {
    // given
    String adminId = "adminId";

    // when
    this.mvc.perform(get(AGENCY_ADMIN_PATH + adminId)).andExpect(status().isOk());

    // then
    verify(this.adminUserFacade, times(1)).findAgencyAdmin(adminId);
  }

  @Test
  void getAgencyAdmin_Should_returnNoContent_When_requiredAdminDoesNotExist() throws Exception {

    // given
    when(this.consultantAdminFacade.findConsultant(any())).thenThrow(new NoContentException(""));

    // when
    this.mvc
        .perform(get(CONSULTANT_PATH + "consultantId"))

        // then
        .andExpect(status().isNoContent());
  }

  @Test
  void createNewAdminAgency_Should_returnOk_When_requiredCreateAgencyAdminIsGiven()
      throws Exception {
    // given
    CreateAdminDTO createAgencyAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);

    // when
    this.mvc
        .perform(
            post(AGENCY_ADMIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAgencyAdminDTO)))
        .andExpect(status().isOk());

    // then
    verify(this.adminUserFacade, times(1)).createNewAgencyAdmin(any());
  }

  @Test
  void createNewTenantAdmin_Should_returnOk_When_requiredCreateTenantAdminIsGiven()
      throws Exception {
    // given
    CreateAdminDTO createAdminDTO = new EasyRandom().nextObject(CreateAdminDTO.class);

    // when
    this.mvc
        .perform(
            post(TENANT_ADMIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminDTO)))
        .andExpect(status().isOk());

    // then
    verify(this.adminUserFacade, times(1)).createNewTenantAdmin(any());
  }

  @Test
  void createAgencyAdmin_Should_returnBadRequest_When_requiredCreateAgencyAdminIsMissing()
      throws Exception {
    // given
    // when
    this.mvc
        .perform(post(AGENCY_ADMIN_PATH).contentType(MediaType.APPLICATION_JSON))
        // then
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateAgencyAdmin_Should_returnOk_When_requiredCreateAgencyAdminIsGiven() throws Exception {
    // given
    UpdateAgencyAdminDTO updateAgencyAdminDTO =
        new EasyRandom().nextObject(UpdateAgencyAdminDTO.class);
    updateAgencyAdminDTO.setEmail("test@test.com");

    // when
    this.mvc
        .perform(
            put(AGENCY_ADMIN_PATH + "adminId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAgencyAdminDTO)))
        .andExpect(status().isOk());

    // then
    verify(this.adminUserFacade, times(1)).updateAgencyAdmin(anyString(), any());
  }

  @Test
  void updateTenantAdmin_Should_returnOk_When_requiredCreateTenantAdminIsGiven() throws Exception {
    // given
    UpdateAgencyAdminDTO updateAgencyAdminDTO =
        new EasyRandom().nextObject(UpdateAgencyAdminDTO.class);
    updateAgencyAdminDTO.setEmail("test@test.com");

    // when
    this.mvc
        .perform(
            put(TENANT_ADMIN_PATH + "adminId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAgencyAdminDTO)))
        .andExpect(status().isOk());

    // then
    verify(this.adminUserFacade, times(1)).updateTenantAdmin(anyString(), any());
  }

  @Test
  void updateAgencyAdmin_Should_returnBadRequest_When_requiredParamsAreMissing() throws Exception {
    // given
    // when
    this.mvc
        .perform(put(AGENCY_ADMIN_PATH + "adminId").contentType(MediaType.APPLICATION_JSON))
        // then
        .andExpect(status().isBadRequest());
  }

  @Test
  void createAdminAgency_Should_returnCreated_When_requiredParamsAreGiven() throws Exception {
    String adminId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    // given
    String adminAgencyPath = String.format(AGENCIES_OF_ADMIN_PATH, adminId);

    CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO = new CreateAdminAgencyRelationDTO();
    createAdminAgencyRelationDTO.setAgencyId(15L);

    // when
    this.mvc
        .perform(
            post(adminAgencyPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminAgencyRelationDTO)))
        .andExpect(status().isCreated());

    // then
    verify(this.adminUserFacade, times(1))
        .createNewAdminAgencyRelation(adminId, createAdminAgencyRelationDTO);
  }

  @Test
  void setAdminAgencies_Should_return_ok_When_RequiredParams_Are_Given() throws Exception {
    // given
    var adminId = UUID.randomUUID().toString();
    var agencies = givenAgenciesToSet();

    // when
    mvc.perform(
            put(AGENCIES_OF_ADMIN_PATH, adminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agencies)))
        .andExpect(status().isOk());

    // then
    verify(adminUserFacade).setAdminAgenciesRelation(any(), anyList());
  }

  @Test
  void deleteAdminAgency_Should_return_Ok_When_requiredParamsAreGiven() throws Exception {
    // given
    String adminId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";
    Long agencyId = 1L;

    // when
    this.mvc
        .perform(
            delete(String.format(DELETE_ADMIN_AGENCY_PATH, adminId, agencyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // then
    verify(this.adminUserFacade, times(1)).deleteAdminAgencyRelation(adminId, agencyId);
  }

  @Test
  void deleteAgencyAdmin_Should_returnOk_When_requiredParamIsGiven() throws Exception {
    // given
    String adminId = "1234";

    // when
    this.mvc
        .perform(
            delete(String.format(DELETE_AGENCY_ADMIN_PATH, adminId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // then
    verify(this.adminUserFacade, times(1)).deleteAgencyAdmin(adminId);
  }

  private ArrayList<CreateConsultantAgencyDTO> givenAgenciesToSet() {
    var agencies = new ArrayList<CreateConsultantAgencyDTO>();
    var count = easyRandom.nextInt(9) + 1;

    for (int i = 0; i < count; i++) {
      var agency = new CreateConsultantAgencyDTO();
      agency.setAgencyId(easyRandom.nextLong());
      agency.setRoleSetKey("role set");
      agencies.add(agency);
    }
    return agencies;
  }
}

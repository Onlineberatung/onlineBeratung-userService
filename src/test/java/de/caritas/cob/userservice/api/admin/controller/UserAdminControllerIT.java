package de.caritas.cob.userservice.api.admin.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import de.caritas.cob.userservice.api.admin.facade.ConsultantAdminFacade;
import de.caritas.cob.userservice.api.admin.facade.UserAdminFacade;
import de.caritas.cob.userservice.api.admin.report.service.ViolationReportGenerator;
import de.caritas.cob.userservice.api.admin.service.ConsultingTypeAdminService;
import de.caritas.cob.userservice.api.admin.service.session.SessionAdminService;
import de.caritas.cob.userservice.api.authorization.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class UserAdminControllerIT {

  protected static final String ROOT_PATH = "/useradmin";
  protected static final String SESSION_PATH = ROOT_PATH + "/sessions";
  protected static final String REPORT_PATH = ROOT_PATH + "/report";
  protected static final String FILTERED_CONSULTANTS_PATH = ROOT_PATH + "/consultants";
  protected static final String GET_CONSULTANT_PATH = ROOT_PATH + "/consultants/";
  protected static final String DELETE_CONSULTANT_PATH = GET_CONSULTANT_PATH + "1234";
  protected static final String DELETE_ASKER_PATH = ROOT_PATH + "/askers/1234";
  protected static final String CONSULTING_TYPE_PATH = ROOT_PATH + "/consultingtypes";
  protected static final String CONSULTANT_AGENCIES_PATH = ROOT_PATH + "/consultants/%s/agencies";
  protected static final String CONSULTANT_AGENCY_PATH = ROOT_PATH + "/consultants/%s/agencies";
  protected static final String DELETE_CONSULTANT_AGENCY_PATH = ROOT_PATH + "/consultants/%s"
      + "/agencies/%s";
  protected static final String AGENCY_CHANGE_TYPE_PATH = ROOT_PATH + "/agency/1/changetype";
  protected static final String PAGE_PARAM = "page";
  protected static final String PER_PAGE_PARAM = "perPage";

  @Autowired
  private MockMvc mvc;

  @MockBean
  private SessionAdminService sessionAdminService;

  @MockBean
  private ConsultantAdminFacade consultantAdminFacade;

  @MockBean
  private ViolationReportGenerator violationReportGenerator;

  @MockBean
  private LinkDiscoverers linkDiscoverers;

  @MockBean
  private RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper;

  @MockBean
  private ConsultingTypeAdminService consultingTypeAdminService;

  @MockBean
  private UserAdminFacade userAdminFacade;

  @Test
  public void getSessions_Should_returnBadRequest_When_requiredPaginationParamsAreMissing()
      throws Exception {
    this.mvc.perform(get(SESSION_PATH))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getRoot_Should_returnExpectedRootDTO()
      throws Exception {
    this.mvc.perform(get(ROOT_PATH))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._links").exists())
        .andExpect(jsonPath("$._links.self").exists())
        .andExpect(jsonPath("$._links.self.href", endsWith("/useradmin")))
        .andExpect(jsonPath("$._links.sessions").exists())
        .andExpect(
            jsonPath("$._links.sessions.href", endsWith("/useradmin/sessions?page=1&perPage=20")))
        .andExpect(jsonPath("$._links.consultants").exists())
        .andExpect(jsonPath("$._links.consultants.href",
            endsWith("/useradmin/consultants?page=1&perPage=20")));
  }

  @Test
  public void getSessions_Should_returnOk_When_requiredPaginationParamsAreGiven()
      throws Exception {
    this.mvc.perform(get(SESSION_PATH)
        .param(PAGE_PARAM, "0")
        .param(PER_PAGE_PARAM, "1"))
        .andExpect(status().isOk());

    verify(this.sessionAdminService, times(1))
        .findSessions(eq(0), eq(1), any());
  }

  @Test
  public void getConsultantAgency_Should_returnOk_When_requiredConsultantIdParamIsGiven()
      throws Exception {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    String consultantAgencyPath = String.format(CONSULTANT_AGENCIES_PATH, consultantId);

    this.mvc.perform(get(consultantAgencyPath))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .findConsultantAgencies(eq(consultantId));
  }

  @Test
  public void getConsultingTypes_Should_returnBadRequest_When_requiredPaginationParamsAreMissing()
      throws Exception {
    this.mvc.perform(get(CONSULTING_TYPE_PATH)).andExpect(status().isBadRequest());
  }

  @Test
  public void getConsultingTypes_Should_returnOk_When_requiredPaginationParamsAreGiven()
      throws Exception {
    this.mvc.perform(get(CONSULTING_TYPE_PATH)
        .param(PAGE_PARAM, "0")
        .param(PER_PAGE_PARAM, "1"))
        .andExpect(status().isOk());

    verify(this.consultingTypeAdminService, times(1))
        .findConsultingTypes(eq(0), eq(1));
  }

  @Test
  public void generateReport_Should_returnOk() throws Exception {
    this.mvc.perform(get(REPORT_PATH))
        .andExpect(status().isOk());

    verify(this.violationReportGenerator, times(1)).generateReport();
  }

  @Test
  public void getConsultants_Should_returnBadRequest_When_requiredPaginationParamsAreMissing()
      throws Exception {
    this.mvc.perform(get(FILTERED_CONSULTANTS_PATH))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getConsultants_Should_returnOk_When_requiredPaginationParamsAreGiven()
      throws Exception {
    this.mvc.perform(get(FILTERED_CONSULTANTS_PATH)
        .param(PAGE_PARAM, "0")
        .param(PER_PAGE_PARAM, "1"))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .findFilteredConsultants(eq(0), eq(1), any());
  }

  @Test
  public void getConsultant_Should_returnOk_When_requiredConsultantIdParamIsGiven()
      throws Exception {
    this.mvc.perform(get(GET_CONSULTANT_PATH + "consultantId"))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .findConsultant(eq("consultantId"));
  }

  @Test
  public void getConsultant_Should_returnNoContent_When_requiredConsultantDoesNotExist()
      throws Exception {
    when(this.consultantAdminFacade.findConsultant(any())).thenThrow(new NoContentException(""));

    this.mvc.perform(get(GET_CONSULTANT_PATH + "consultantId"))
        .andExpect(status().isNoContent());
  }

  @Test
  public void createConsultant_Should_returnOk_When_requiredCreateConsultantIsGiven()
      throws Exception {
    CreateConsultantDTO createConsultantDTO =
        new EasyRandom().nextObject(CreateConsultantDTO.class);

    this.mvc.perform(post(GET_CONSULTANT_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(createConsultantDTO)))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .createNewConsultant(any());
  }

  @Test
  public void createConsultant_Should_returnBadRequest_When_requiredCreateConsultantIsMissing()
      throws Exception {
    this.mvc.perform(post(GET_CONSULTANT_PATH)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateConsultant_Should_returnOk_When_requiredCreateConsultantIsGiven()
      throws Exception {
    UpdateAdminConsultantDTO updateConsultantDTO =
        new EasyRandom().nextObject(UpdateAdminConsultantDTO.class);

    this.mvc.perform(put(GET_CONSULTANT_PATH + "consultantId")
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(updateConsultantDTO)))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .updateConsultant(anyString(), any());
  }

  @Test
  public void updateConsultant_Should_returnBadRequest_When_requiredParamsAreMissing()
      throws Exception {
    this.mvc.perform(put(GET_CONSULTANT_PATH + "consultantId")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createConsultantAgency_Should_returnCreated_When_requiredParamsAreGiven()
      throws Exception {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    String consultantAgencyPath = String.format(CONSULTANT_AGENCY_PATH, consultantId);

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRole("role");

    this.mvc.perform(post(consultantAgencyPath)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(createConsultantAgencyDTO)))
        .andExpect(status().isCreated());

    verify(this.consultantAdminFacade, times(1))
        .createNewConsultantAgency(eq(consultantId), eq(createConsultantAgencyDTO));
  }

  @Test
  public void changeAgencyType_Should_returnOk_When_parametersAreValid() throws Exception {
    this.mvc.perform(post(AGENCY_CHANGE_TYPE_PATH)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).changeAgencyType(any(), any());
  }

  @Test
  public void deleteConsultantAgency_Should_returnOk_When_requiredParamsAreGiven()
      throws Exception {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";
    Long agencyId = 1L;

    String consultantAgencyDeletePath =
        String.format(DELETE_CONSULTANT_AGENCY_PATH, consultantId, agencyId);

    this.mvc.perform(delete(consultantAgencyDeletePath)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .markConsultantAgencyForDeletion(eq(consultantId), eq(agencyId));
  }

  @Test
  public void deleteConsultant_Should_returnOk_When_requiredParamIsGiven()
      throws Exception {
    this.mvc.perform(delete(DELETE_CONSULTANT_PATH)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1))
        .markConsultantForDeletion(eq("1234"));
  }

  @Test
  public void deleteAsker_Should_returnOk_When_requiredParamIsGiven()
      throws Exception {
    this.mvc.perform(delete(DELETE_ASKER_PATH)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.userAdminFacade, times(1))
        .markAskerForDeletion(eq("1234"));
  }

}

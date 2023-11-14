package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.AGENCY_CHANGE_TYPE_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.AGENCY_CONSULTANT_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.CONSULTANT_AGENCIES_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.CONSULTANT_AGENCY_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.CONSULTANT_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.DELETE_ASKER_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.DELETE_CONSULTANT_AGENCY_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.DELETE_CONSULTANT_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.FILTERED_CONSULTANTS_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.PAGE_PARAM;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.PER_PAGE_PARAM;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.REPORT_PATH;
import static de.caritas.cob.userservice.api.adapters.web.controller.UserAdminControllerIT.SESSION_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.admin.facade.AskerUserAdminFacade;
import de.caritas.cob.userservice.api.admin.facade.ConsultantAdminFacade;
import de.caritas.cob.userservice.api.admin.report.service.ViolationReportGenerator;
import de.caritas.cob.userservice.api.admin.service.session.SessionAdminService;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class UserAdminControllerAuthorizationIT {

  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final EasyRandom easyRandom = new EasyRandom();

  @Autowired private MockMvc mvc;

  @MockBean private SessionAdminService sessionAdminService;

  @MockBean private ViolationReportGenerator violationReportGenerator;

  @MockBean private ConsultantAdminFacade consultantAdminFacade;

  @MockBean private AskerUserAdminFacade askerUserAdminFacade;

  @Test
  public void
      getSessions_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {

    mvc.perform(get(SESSION_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(sessionAdminService);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void getSessions_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {

    mvc.perform(get(SESSION_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionAdminService);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void getSessions_Should_ReturnOkAndCallSessionAdminService_When_userAdminAuthority()
      throws Exception {

    mvc.perform(
            get(SESSION_PATH)
                .param(PAGE_PARAM, "0")
                .param(PER_PAGE_PARAM, "1")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(sessionAdminService, times(1)).findSessions(any(), anyInt(), any());
  }

  @Test
  public void
      generateViolationReport_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {

    mvc.perform(get(REPORT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(violationReportGenerator);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void
      generateViolationReport_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
          throws Exception {

    mvc.perform(get(REPORT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(violationReportGenerator);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      generateViolationReport_Should_ReturnOkAndCallViolationReportGenerator_When_userAdminAuthority()
          throws Exception {

    mvc.perform(get(REPORT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(violationReportGenerator, times(1)).generateReport();
  }

  @Test
  public void
      getConsultants_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {

    mvc.perform(get(FILTERED_CONSULTANTS_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void getConsultants_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {

    mvc.perform(get(FILTERED_CONSULTANTS_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      getConsultants_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
          throws Exception {

    mvc.perform(
            get(FILTERED_CONSULTANTS_PATH)
                .param(PAGE_PARAM, "0")
                .param(PER_PAGE_PARAM, "1")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).findFilteredConsultants(any(), anyInt(), any(), any());
  }

  @Test
  public void
      getConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {

    mvc.perform(get(CONSULTANT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void getConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {

    mvc.perform(get(CONSULTANT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      getConsultant_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
          throws Exception {

    mvc.perform(
            get(CONSULTANT_PATH + "consultantId")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).findConsultant(any());
  }

  @Test
  public void
      getConsultantAgencies_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    String consultantAgencyPath =
        String.format(CONSULTANT_AGENCIES_PATH, "1da238c6-cd46-4162-80f1-bff74eafeAAA");

    mvc.perform(get(consultantAgencyPath).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void
      getConsultantAgencies_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
          throws Exception {
    String consultantAgencyPath =
        String.format(CONSULTANT_AGENCIES_PATH, "1da238c6-cd46-4162-80f1-bff74eafeAAA");

    mvc.perform(get(consultantAgencyPath).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      getConsultantAgencies_Should_ReturnOkAndCallConsultantAdminFacade_When_userAdminAuthority()
          throws Exception {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    String consultantAgencyPath = String.format(CONSULTANT_AGENCIES_PATH, consultantId);

    mvc.perform(get(consultantAgencyPath).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).findConsultantAgencies(consultantId);
  }

  @Test
  public void
      createConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    mvc.perform(post(CONSULTANT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void createConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(post(CONSULTANT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN, AuthorityValue.CONSULTANT_CREATE})
  public void
      createConsultant_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
          throws Exception {
    CreateConsultantDTO createConsultantDTO = easyRandom.nextObject(CreateConsultantDTO.class);

    mvc.perform(
            post(CONSULTANT_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createConsultantDTO)))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).createNewConsultant(any());
  }

  @Test
  public void
      updateConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    mvc.perform(put(CONSULTANT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void updateConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(put(CONSULTANT_PATH).cookie(CSRF_COOKIE).header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      updateConsultant_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
          throws Exception {
    UpdateAdminConsultantDTO updateConsultantDTO =
        easyRandom.nextObject(UpdateAdminConsultantDTO.class);

    mvc.perform(
            put(CONSULTANT_PATH + "consultantId")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateConsultantDTO)))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).updateConsultant(anyString(), any());
  }

  @Test
  public void
      createConsultantAgency_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    mvc.perform(
            post(String.format(CONSULTANT_AGENCY_PATH, "consultantId"))
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void
      createConsultantAgency_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
          throws Exception {
    mvc.perform(
            post(String.format(CONSULTANT_AGENCY_PATH, "consultantId"))
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      createConsultantAgency_Should_ReturnCreatedAndCallConsultantAdminFilterService_When_userAdminAuthority()
          throws Exception {
    CreateConsultantAgencyDTO createConsultantAgencyDTO =
        easyRandom.nextObject(CreateConsultantAgencyDTO.class);

    mvc.perform(
            post(String.format(CONSULTANT_AGENCY_PATH, "consultantId"))
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createConsultantAgencyDTO)))
        .andExpect(status().isCreated());

    verify(consultantAdminFacade, times(1)).createNewConsultantAgency(anyString(), any());
  }

  @Test
  public void
      setConsultantAgenciesShouldReturnUnauthorizedAndCallNoMethodsWhenNoKeycloakAuthPresent()
          throws Exception {
    mvc.perform(
            put("/useradmin/consultants/{consultantId}/agencies", UUID.randomUUID().toString())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ANONYMOUS_DEFAULT,
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.START_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.USER_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.ASSIGN_CONSULTANT_TO_PEER_SESSION
      })
  public void setConsultantAgenciesShouldReturnForbiddenAndCallNoMethodsIfNotUserAdmin()
      throws Exception {
    mvc.perform(
            put("/useradmin/consultants/{consultantId}/agencies", UUID.randomUUID().toString())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN, AuthorityValue.CONSULTANT_UPDATE})
  public void setConsultantAgenciesShouldReturnOkAndCallConsultantAdminFacade() throws Exception {
    var agencies = List.of(easyRandom.nextObject(CreateConsultantAgencyDTO.class));

    mvc.perform(
            put("/useradmin/consultants/{consultantId}/agencies", UUID.randomUUID().toString())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agencies)))
        .andExpect(status().isOk());

    verify(consultantAdminFacade).markConsultantAgenciesForDeletion(anyString(), any());
    verify(consultantAdminFacade).filterAgencyListForCreation(anyString(), any());
    verify(consultantAdminFacade).prepareConsultantAgencyRelation(anyString(), any());
    verify(consultantAdminFacade).completeConsultantAgencyAssigment(anyString(), any());
  }

  @Test
  public void
      changeAgencyType_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    mvc.perform(
            post(AGENCY_CHANGE_TYPE_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void changeAgencyType_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(
            post(AGENCY_CHANGE_TYPE_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void changeAgencyType_Should_ReturnCreatedAndCallConsultantAdmin_When_userAdminAuthority()
      throws Exception {
    mvc.perform(
            post(AGENCY_CHANGE_TYPE_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).changeAgencyType(any(), any());
  }

  @Test
  public void
      deleteConsultantAgency_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    mvc.perform(
            delete(String.format(DELETE_CONSULTANT_AGENCY_PATH, "1", 1L))
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void
      deleteConsultantAgency_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
          throws Exception {
    mvc.perform(
            delete(String.format(DELETE_CONSULTANT_AGENCY_PATH, "1", 1L))
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      deleteConsultantAgency_Should_ReturnCreatedAndCallConsultantAdmin_When_userAdminAuthority()
          throws Exception {
    mvc.perform(
            delete(String.format(DELETE_CONSULTANT_AGENCY_PATH, "1", 1L))
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).markConsultantAgencyForDeletion(any(), any());
  }

  @Test
  public void
      deleteConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    mvc.perform(
            delete(DELETE_CONSULTANT_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void deleteConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(
            delete(DELETE_CONSULTANT_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void
      deleteConsultant_Should_MarkConsultantForDeletionAndCallConsultantAdmin_When_userAdminAuthority()
          throws Exception {
    mvc.perform(
            delete(DELETE_CONSULTANT_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).markConsultantForDeletion(any(), any());
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.RESTRICTED_AGENCY_ADMIN})
  public void deleteConsultant_Should_ReturnForbidden_When_userDoesNotHaveUserAdminAuthority()
      throws Exception {
    mvc.perform(
            delete(DELETE_CONSULTANT_PATH)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoInteractions(consultantAdminFacade);
  }

  @Test
  public void
      deleteAsker_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    mvc.perform(delete(DELETE_ASKER_PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(askerUserAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void deleteAsker_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(delete(DELETE_ASKER_PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(askerUserAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void deleteAsker_Should_ReturnOkAndCallUserAdminFacade_When_userAdminAuthority()
      throws Exception {
    mvc.perform(delete(DELETE_ASKER_PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.askerUserAdminFacade, times(1)).markAskerForDeletion(any());
  }

  @Test
  public void
      getAgencyConsultants_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
          throws Exception {
    mvc.perform(
            get(String.format(AGENCY_CONSULTANT_PATH, "1")).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {
        AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION,
        AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
        AuthorityValue.USE_FEEDBACK,
        AuthorityValue.CONSULTANT_DEFAULT,
        AuthorityValue.VIEW_AGENCY_CONSULTANTS,
        AuthorityValue.VIEW_ALL_PEER_SESSIONS,
        AuthorityValue.START_CHAT,
        AuthorityValue.CREATE_NEW_CHAT,
        AuthorityValue.STOP_CHAT,
        AuthorityValue.UPDATE_CHAT
      })
  public void
      getAgencyConsultants_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
          throws Exception {
    mvc.perform(
            get(String.format(AGENCY_CONSULTANT_PATH, "1")).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.USER_ADMIN})
  public void getAgencyConsultants_Should_ReturnOkAndCallUserAdminFacade_When_userAdminAuthority()
      throws Exception {
    mvc.perform(
            get(String.format(AGENCY_CONSULTANT_PATH, "1")).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).findConsultantsForAgency(any());
  }
}

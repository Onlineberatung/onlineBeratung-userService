package de.caritas.cob.userservice.api.admin.controller;

import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.AGENCY_CHANGE_TYPE_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.CONSULTANT_AGENCIES_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.CONSULTANT_AGENCY_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.CONSULTING_TYPE_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.DELETE_ASKER_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.DELETE_CONSULTANT_AGENCY_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.DELETE_CONSULTANT_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.FILTERED_CONSULTANTS_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.GET_CONSULTANT_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.PAGE_PARAM;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.PER_PAGE_PARAM;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.REPORT_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.SESSION_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.admin.facade.ConsultantAdminFacade;
import de.caritas.cob.userservice.api.admin.facade.UserAdminFacade;
import de.caritas.cob.userservice.api.admin.report.service.ViolationReportGenerator;
import de.caritas.cob.userservice.api.admin.service.session.SessionAdminService;
import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
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

  @Autowired
  private MockMvc mvc;

  @MockBean
  private SessionAdminService sessionAdminService;

  @MockBean
  private ViolationReportGenerator violationReportGenerator;

  @MockBean
  private ConsultantAdminFacade consultantAdminFacade;

  @MockBean
  private UserAdminFacade userAdminFacade;

  @MockBean
  private UsernameTranscoder usernameTranscoder;

  @Test
  public void getSessions_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {

    mvc.perform(get(SESSION_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(sessionAdminService);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getSessions_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {

    mvc.perform(get(SESSION_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionAdminService);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void getSessions_Should_ReturnOkAndCallSessionAdminService_When_userAdminAuthority()
      throws Exception {

    mvc.perform(get(SESSION_PATH)
        .param(PAGE_PARAM, "0")
        .param(PER_PAGE_PARAM, "1")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(sessionAdminService, times(1)).findSessions(any(), anyInt(), any());
  }

  @Test
  public void generateViolationReport_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {

    mvc.perform(get(REPORT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(violationReportGenerator);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void generateViolationReport_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {

    mvc.perform(get(REPORT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(violationReportGenerator);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void generateViolationReport_Should_ReturnOkAndCallViolationReportGenerator_When_userAdminAuthority()
      throws Exception {

    mvc.perform(get(REPORT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(violationReportGenerator, times(1)).generateReport();
  }

  @Test
  public void getConsultants_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {

    mvc.perform(get(FILTERED_CONSULTANTS_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getConsultants_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {

    mvc.perform(get(FILTERED_CONSULTANTS_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void getConsultants_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
      throws Exception {

    mvc.perform(get(FILTERED_CONSULTANTS_PATH)
        .param(PAGE_PARAM, "0")
        .param(PER_PAGE_PARAM, "1")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).findFilteredConsultants(any(), anyInt(), any());
  }

  @Test
  public void getConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {

    mvc.perform(get(GET_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {

    mvc.perform(get(GET_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void getConsultant_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
      throws Exception {

    mvc.perform(get(GET_CONSULTANT_PATH + "consultantId")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).findConsultant(any());
  }

  @Test
  public void getConsultantAgencies_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {
    String consultantAgencyPath = String
        .format(CONSULTANT_AGENCIES_PATH, "1da238c6-cd46-4162-80f1-bff74eafeAAA");

    mvc.perform(get(consultantAgencyPath)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getConsultantAgencies_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    String consultantAgencyPath = String
        .format(CONSULTANT_AGENCIES_PATH, "1da238c6-cd46-4162-80f1-bff74eafeAAA");

    mvc.perform(get(consultantAgencyPath)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void getConsultantAgencies_Should_ReturnOkAndCallConsultantAdminFacade_When_userAdminAuthority()
      throws Exception {
    String consultantId = "1da238c6-cd46-4162-80f1-bff74eafeAAA";

    String consultantAgencyPath = String.format(CONSULTANT_AGENCIES_PATH, consultantId);

    mvc.perform(get(consultantAgencyPath)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).findConsultantAgencies(consultantId);
  }

  @Test
  public void createConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {
    mvc.perform(post(GET_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void createConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(post(GET_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void createConsultant_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
      throws Exception {
    CreateConsultantDTO createConsultantDTO =
        new EasyRandom().nextObject(CreateConsultantDTO.class);

    mvc.perform(post(GET_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(createConsultantDTO)))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).createNewConsultant(any());
  }

  @Test
  public void updateConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {
    mvc.perform(put(GET_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void updateConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(put(GET_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void updateConsultant_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
      throws Exception {
    UpdateAdminConsultantDTO updateConsultantDTO =
        new EasyRandom().nextObject(UpdateAdminConsultantDTO.class);

    mvc.perform(put(GET_CONSULTANT_PATH + "consultantId")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(updateConsultantDTO)))
        .andExpect(status().isOk());

    verify(consultantAdminFacade, times(1)).updateConsultant(anyString(), any());
  }

  @Test
  public void createConsultantAgency_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {
    mvc.perform(post(String.format(CONSULTANT_AGENCY_PATH, "consultantId"))
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void createConsultantAgency_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(post(String.format(CONSULTANT_AGENCY_PATH, "consultantId"))
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void createConsultantAgency_Should_ReturnCreatedAndCallConsultantAdminFilterService_When_userAdminAuthority()
      throws Exception {
    CreateConsultantAgencyDTO createConsultantAgencyDTO =
        new EasyRandom().nextObject(CreateConsultantAgencyDTO.class);

    mvc.perform(post(String.format(CONSULTANT_AGENCY_PATH, "consultantId"))
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(createConsultantAgencyDTO)))
        .andExpect(status().isCreated());

    verify(consultantAdminFacade, times(1)).createNewConsultantAgency(anyString(), any());
  }

  @Test
  public void changeAgencyType_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {
    mvc.perform(post(AGENCY_CHANGE_TYPE_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void changeAgencyType_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(post(AGENCY_CHANGE_TYPE_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void changeAgencyType_Should_ReturnCreatedAndCallConsultantAdmin_When_userAdminAuthority()
      throws Exception {
    mvc.perform(post(AGENCY_CHANGE_TYPE_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).changeAgencyType(any(), any());
  }

  @Test
  public void deleteConsultantAgency_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {
    mvc.perform(delete(String.format(DELETE_CONSULTANT_AGENCY_PATH, "1", 1L))
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void deleteConsultantAgency_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(delete(String.format(DELETE_CONSULTANT_AGENCY_PATH, "1", 1L))
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void deleteConsultantAgency_Should_ReturnCreatedAndCallConsultantAdmin_When_userAdminAuthority()
      throws Exception {
    mvc.perform(delete(String.format(DELETE_CONSULTANT_AGENCY_PATH, "1", 1L))
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).markConsultantAgencyForDeletion(any(), any());
  }

  @Test
  public void deleteConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {
    mvc.perform(delete(DELETE_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void deleteConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(delete(DELETE_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void deleteConsultant_Should_ReturnCreatedAndCallConsultantAdmin_When_userAdminAuthority()
      throws Exception {
    mvc.perform(delete(DELETE_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.consultantAdminFacade, times(1)).markConsultantForDeletion(any());
  }

  @Test
  public void deleteAsker_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {
    mvc.perform(delete(DELETE_ASKER_PATH)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(userAdminFacade);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void deleteAsker_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {
    mvc.perform(delete(DELETE_ASKER_PATH)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(userAdminFacade);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void deleteAsker_Should_ReturnOkAndCallUserAdminFacade_When_userAdminAuthority()
      throws Exception {
    mvc.perform(delete(DELETE_ASKER_PATH)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(this.userAdminFacade, times(1)).markAskerForDeletion(any());
  }

}

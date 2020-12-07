package de.caritas.cob.userservice.api.admin.controller;

import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.CONSULTING_TYPE_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.FILTERED_CONSULTANTS_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.GET_CONSULTANT_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.PAGE_PARAM;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.PER_PAGE_PARAM;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.REPORT_PATH;
import static de.caritas.cob.userservice.api.admin.controller.UserAdminControllerIT.SESSION_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.userservice.api.admin.report.service.ViolationReportGenerator;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminFilterService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminService;
import de.caritas.cob.userservice.api.admin.service.session.SessionAdminService;
import de.caritas.cob.userservice.api.admin.service.ConsultingTypeAdminService;
import de.caritas.cob.userservice.api.admin.service.SessionAdminService;
import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import javax.servlet.http.Cookie;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
  private ConsultantAdminFilterService consultantAdminFilterService;

  @MockBean
  private ConsultantAdminService consultantAdminService;

  @MockBean
  private ConsultingTypeAdminService consultingTypeAdminService;

  @MockBean
  private ViolationReportGenerator violationReportGenerator;

  @Test
  public void getSessions_Should_ReturnForbiddenAndCallNoMethods_When_noCsrfTokenIsSet()
      throws Exception {

    mvc.perform(get(SESSION_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(sessionAdminService);
  }

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
  public void getConsultingTypes_Should_ReturnForbiddenAndCallNoMethods_When_noCsrfTokenIsSet()
      throws Exception {

    mvc.perform(get(CONSULTING_TYPE_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(this.consultingTypeAdminService);
  }

  @Test
  public void getConsultingTypes_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {

    mvc.perform(get(CONSULTING_TYPE_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultingTypeAdminService);
  }

  @Test
  @WithMockUser(
      authorities = {Authority.ASSIGN_CONSULTANT_TO_SESSION, Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
          Authority.USE_FEEDBACK, Authority.TECHNICAL_DEFAULT, Authority.CONSULTANT_DEFAULT,
          Authority.VIEW_AGENCY_CONSULTANTS, Authority.VIEW_ALL_PEER_SESSIONS, Authority.START_CHAT,
          Authority.CREATE_NEW_CHAT, Authority.STOP_CHAT, Authority.UPDATE_CHAT})
  public void getConsultingTypes_Should_ReturnForbiddenAndCallNoMethods_When_noUserAdminAuthority()
      throws Exception {

    mvc.perform(get(CONSULTING_TYPE_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultingTypeAdminService);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void getConsultingTypes_Should_ReturnOkAndCallSessionAdminService_When_userAdminAuthority()
      throws Exception {

    mvc.perform(get(CONSULTING_TYPE_PATH)
        .param(PAGE_PARAM, "0")
        .param(PER_PAGE_PARAM, "1")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(consultingTypeAdminService, times(1)).findConsultingTypes(any(), any());
  }

  @Test
  public void generateViolationReport_Should_ReturnForbiddenAndCallNoMethods_When_noCsrfTokenIsSet()
      throws Exception {

    mvc.perform(get(REPORT_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(violationReportGenerator);
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
  public void getConsultants_Should_ReturnForbiddenAndCallNoMethods_When_noCsrfTokenIsSet()
      throws Exception {

    mvc.perform(get(FILTERED_CONSULTANTS_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminFilterService);
  }

  @Test
  public void getConsultants_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {

    mvc.perform(get(FILTERED_CONSULTANTS_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminFilterService);
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

    verifyNoMoreInteractions(consultantAdminFilterService);
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

    verify(consultantAdminFilterService, times(1)).findFilteredConsultants(any(), anyInt(), any());
  }

  @Test
  public void getConsultant_Should_ReturnForbiddenAndCallNoMethods_When_noCsrfTokenIsSet()
      throws Exception {

    mvc.perform(get(GET_CONSULTANT_PATH))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(consultantAdminService);
  }

  @Test
  public void getConsultant_Should_ReturnUnauthorizedAndCallNoMethods_When_noKeycloakAuthorizationIsPresent()
      throws Exception {

    mvc.perform(get(GET_CONSULTANT_PATH)
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(consultantAdminService);
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

    verifyNoMoreInteractions(consultantAdminService);
  }

  @Test
  @WithMockUser(authorities = {Authority.USER_ADMIN})
  public void getConsultant_Should_ReturnOkAndCallConsultantAdminFilterService_When_userAdminAuthority()
      throws Exception {

    mvc.perform(get(GET_CONSULTANT_PATH + "consultantId")
        .cookie(CSRF_COOKIE)
        .header(CSRF_HEADER, CSRF_VALUE))
        .andExpect(status().isOk());

    verify(consultantAdminService, times(1)).findConsultantById(any());
  }

}

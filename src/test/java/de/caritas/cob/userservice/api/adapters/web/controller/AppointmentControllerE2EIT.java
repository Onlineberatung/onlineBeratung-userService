package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.api.adapters.web.dto.AppointmentStatus;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.AppointmentRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.util.Set;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class AppointmentControllerE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ConsultantRepository consultantRepository;

  @Autowired
  private AppointmentRepository appointmentRepository;

  @MockBean
  private AuthenticatedUser authenticatedUser;

  private Appointment appointment;

  private Consultant consultant;

  @AfterEach
  public void reset() {
    appointment = null;
    appointmentRepository.deleteAll();
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void getAppointmentShouldReturnOk() throws Exception {
    givenAValidAppointment();

    mockMvc.perform(
            get("/appointments/{id}", appointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void putAppointmentShouldReturnOk() throws Exception {
    givenAValidAppointment();

    mockMvc.perform(
            put("/appointments/{id}", appointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment))
        )
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void deleteAppointmentShouldReturnNoContent() throws Exception {
    givenAValidAppointment();

    mockMvc.perform(
            delete("/appointments/{id}", appointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void getAppointmentsShouldReturnOk() throws Exception {
    mockMvc.perform(
            get("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void postAppointmentShouldReturnCreated() throws Exception {
    givenAValidAppointment(false, AppointmentStatus.CREATED);
    givenAValidConsultant(true);

    mockMvc.perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("id", is(notNullValue())))
        .andExpect(jsonPath("description", is(appointment.getDescription())))
        .andExpect(jsonPath("datetime", is(appointment.getDatetime().toString())))
        .andExpect(jsonPath("status", is(appointment.getStatus().getValue())));

    assertEquals(1, appointmentRepository.count());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void postAppointmentShouldReturnBadRequestIfIdIsSet() throws Exception {
    givenAValidAppointment(true, AppointmentStatus.CREATED);
    givenAValidConsultant(true);

    mockMvc.perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment))
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void postAppointmentShouldReturnBadRequestIfStatusIsNull() throws Exception {
    givenAnAppointmentMissingStatus();
    givenAValidConsultant(true);

    mockMvc.perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment))
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  public void postAppointmentShouldReturnBadRequestIfDatetimeIsNull() throws Exception {
    givenAnAppointmentMissingDatetime();
    givenAValidConsultant(true);

    mockMvc.perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment))
        )
        .andExpect(status().isBadRequest());
  }

  private void givenAValidAppointment() {
    givenAValidAppointment(true, null);
  }

  private void givenAValidAppointment(boolean includingId, AppointmentStatus setStatus) {
    appointment = easyRandom.nextObject(Appointment.class);

    if (!includingId) {
      appointment.setId(null);
    }

    if (nonNull(setStatus)) {
      appointment.setStatus(setStatus);
    }

    var desc = appointment.getDescription();
    if (desc.length() > 300) {
      appointment.setDescription(desc.substring(0, 300));
    }
  }

  private void givenAnAppointmentMissingStatus() {
    givenAValidAppointment(false, null);
    appointment.setStatus(null);
  }

  private void givenAnAppointmentMissingDatetime() {
    givenAValidAppointment(false, null);
    appointment.setDatetime(null);
  }

  private void givenAValidConsultant() {
    givenAValidConsultant(false);
  }

  private void givenAValidConsultant(boolean isAuthUser) {
    consultant = consultantRepository.findAll().iterator().next();
    if (isAuthUser) {
      when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
      when(authenticatedUser.isUser()).thenReturn(false);
      when(authenticatedUser.isConsultant()).thenReturn(true);
      when(authenticatedUser.getUsername()).thenReturn(consultant.getUsername());
      when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.CONSULTANT.getValue()));
      when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anAuthority"));
    }
  }
}

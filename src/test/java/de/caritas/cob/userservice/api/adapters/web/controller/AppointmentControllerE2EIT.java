package de.caritas.cob.userservice.api.adapters.web.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=testing")
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

  private Appointment appointment;

  @AfterEach
  public void reset() {
    appointment = null;
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
    givenAValidAppointment(false);

    mockMvc.perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("id", is(notNullValue())));
  }

  private void givenAValidAppointment() {
    givenAValidAppointment(true);
  }

  private void givenAValidAppointment(boolean includingId) {
    appointment = easyRandom.nextObject(Appointment.class);

    if (!includingId) {
      appointment.setId(null);
    }

    var desc = appointment.getDescription();
    if (desc.length() > 300) {
      appointment.setDescription(desc.substring(0, 300));
    }
  }

}

package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.testHelper.AsyncVerification.verifyAsync;
import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.controller.interceptor.ApiResponseEntityExceptionHandler;
import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.api.adapters.web.dto.AppointmentStatus;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.AppointmentRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

@SpringBootTest
@ActiveProfiles("testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class AppointmentControllerE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);
  private static final Integer BOOKING_ID = 1;

  private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private AppointmentRepository appointmentRepository;

  @MockBean private RabbitTemplate amqpTemplate;

  @MockBean private AuthenticatedUser authenticatedUser;

  @MockBean private Clock clock;

  @MockBean private KeycloakConfigResolver keycloakConfigResolver;

  private Appointment appointment;

  private de.caritas.cob.userservice.api.model.Appointment savedAppointment;

  private Consultant consultant;

  @Autowired private AppointmentController appointmentController;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(appointmentController)
            .setHandlerExceptionResolvers(withExceptionControllerAdvice())
            .build();
    objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
  }

  private ExceptionHandlerExceptionResolver withExceptionControllerAdvice() {
    final ExceptionHandlerExceptionResolver exceptionResolver =
        new ExceptionHandlerExceptionResolver() {
          @Override
          protected ServletInvocableHandlerMethod getExceptionHandlerMethod(
              final HandlerMethod handlerMethod, final Exception exception) {
            Method method =
                new ExceptionHandlerMethodResolver(ApiResponseEntityExceptionHandler.class)
                    .resolveMethod(exception);
            if (method != null) {
              return new ServletInvocableHandlerMethod(
                  new ApiResponseEntityExceptionHandler(), method);
            }
            return super.getExceptionHandlerMethod(handlerMethod, exception);
          }
        };
    exceptionResolver.afterPropertiesSet();
    return exceptionResolver;
  }

  @AfterEach
  public void reset() {
    appointment = null;
    savedAppointment = null;
    appointmentRepository.deleteAll();
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getAppointmentShouldReturnOk() throws Exception {
    givenAValidConsultant(true);
    givenASavedAppointment();

    mockMvc
        .perform(
            get("/appointments/{id}", savedAppointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", is(notNullValue())))
        .andExpect(jsonPath("description", is(savedAppointment.getDescription())))
        .andExpect(jsonPath("datetime", is(notNullValue())))
        .andExpect(jsonPath("status", is(savedAppointment.getStatus().toString().toLowerCase())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getAppointmentByBookingIdShouldReturnOk() throws Exception {
    givenAValidConsultant(true);
    givenASavedAppointment();

    mockMvc
        .perform(
            get("/appointments/booking/{id}", savedAppointment.getBookingId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", is(notNullValue())))
        .andExpect(jsonPath("description", is(savedAppointment.getDescription())))
        .andExpect(jsonPath("datetime", is(notNullValue())))
        .andExpect(jsonPath("status", is(savedAppointment.getStatus().toString().toLowerCase())));
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.USER_DEFAULT)
  void getAppointmentShouldReturnOkAndOnlyStatusForAdviceSeeker() throws Exception {
    givenAnAdviceSeeker();
    givenAValidConsultant(false);
    givenASavedAppointment();

    mockMvc
        .perform(
            get("/appointments/{id}", savedAppointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", is(savedAppointment.getId().toString())))
        .andExpect(jsonPath("status", is(savedAppointment.getStatus().toString().toLowerCase())))
        .andExpect(jsonPath("description").isEmpty())
        .andExpect(jsonPath("datetime").isEmpty());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.ANONYMOUS_DEFAULT)
  void getAppointmentShouldReturnOkAndOnlyStatusForAnonymous() throws Exception {
    givenAnAnonymousUser();
    givenAValidConsultant(false);
    givenASavedAppointment();

    mockMvc
        .perform(
            get("/appointments/{id}", savedAppointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", is(savedAppointment.getId().toString())))
        .andExpect(jsonPath("status", is(savedAppointment.getStatus().toString().toLowerCase())))
        .andExpect(jsonPath("description").isEmpty())
        .andExpect(jsonPath("datetime").isEmpty());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getAppointmentShouldReturnClientErrorOnWrongIdFormat() throws Exception {
    givenAValidConsultant(true);

    mockMvc
        .perform(
            get("/appointments/{id}", RandomStringUtils.randomAlphabetic(36))
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getAppointmentShouldReturnNotFoundIfIdUnknown() throws Exception {
    givenAValidConsultant(true);

    mockMvc
        .perform(
            get("/appointments/{id}", UUID.randomUUID())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateAppointmentShouldReturnUpdateAppointmentAndSendStartStopStatistics() throws Exception {
    givenAValidConsultant(true);
    givenASavedAppointment();
    givenAValidAppointmentDto(savedAppointment.getId(), null);

    assertEquals(1, appointmentRepository.count());
    mockMvc
        .perform(
            put("/appointments/{id}", appointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", is(appointment.getId().toString())))
        .andExpect(jsonPath("description", is(appointment.getDescription())))
        .andExpect(jsonPath("datetime", is(notNullValue())))
        .andExpect(jsonPath("status", is(appointment.getStatus().toString().toLowerCase())));

    assertEquals(1, appointmentRepository.count());
    var updatedAppointment = appointmentRepository.findById(appointment.getId()).orElseThrow();
    assertEquals(appointment.getId(), updatedAppointment.getId());
    assertEquals(
        appointment.getStatus().getValue(),
        updatedAppointment.getStatus().toString().toLowerCase());
    assertEquals(appointment.getDatetime(), updatedAppointment.getDatetime());
    assertEquals(appointment.getDescription(), updatedAppointment.getDescription());

    if (appointment.getStatus() == AppointmentStatus.STARTED) {
      verifyRabbitMqMessageHasBeenSent("START_VIDEO_CALL");
    } else if (appointment.getStatus() == AppointmentStatus.PAUSED) {
      verifyRabbitMqMessageHasBeenSent("STOP_VIDEO_CALL");
    }
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateAppointmentShouldReturnBadRequestIfIdsDiffer() throws Exception {
    givenAValidConsultant(true);
    givenASavedAppointment();
    givenAValidAppointmentDto(savedAppointment.getId(), null);

    mockMvc
        .perform(
            put("/appointments/{id}", UUID.randomUUID())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void updateAppointmentShouldReturnNotFoundIfIdIsUnknown() throws Exception {
    givenAValidConsultant(true);
    var id = UUID.randomUUID();
    givenAValidAppointmentDto(id, null);
    givenASavedAppointment();

    mockMvc
        .perform(
            put("/appointments/{id}", id)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void deleteAppointmentShouldReturnNotFoundIfAppointmentDoesNotExist() throws Exception {
    givenAValidAppointmentDto();

    mockMvc
        .perform(
            delete("/appointments/{id}", appointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void deleteAppointmentShouldDeleteAppointmentAndReturnNoContent() throws Exception {
    givenAValidConsultant(true);
    givenASavedAppointment();

    mockMvc
        .perform(
            delete("/appointments/{id}", savedAppointment.getId())
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertEquals(0, appointmentRepository.count());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getAppointmentsShouldReturnOk() throws Exception {
    when(clock.instant()).thenReturn(Instant.now());
    mockMvc
        .perform(
            get("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void postAppointmentShouldReturnCreated() throws Exception {
    givenAValidAppointmentDto(null, AppointmentStatus.CREATED);
    givenAValidConsultant(true);
    appointment.setConsultantEmail(null);
    mockMvc
        .perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("id", is(notNullValue())))
        .andExpect(jsonPath("description", is(appointment.getDescription())))
        .andExpect(jsonPath("datetime", is(notNullValue())))
        .andExpect(jsonPath("status", is(appointment.getStatus().getValue())));

    assertEquals(1, appointmentRepository.count());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.TECHNICAL_DEFAULT)
  void postAppointmentFromTechnicalRoleContextShouldReturnCreated() throws Exception {
    givenAValidAppointmentDto(null, AppointmentStatus.CREATED);
    consultant = consultantRepository.findAll().iterator().next();
    when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.TECHNICAL.getValue()));
    appointment.setConsultantEmail("emigration@consultant.de");
    mockMvc
        .perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("id", is(notNullValue())))
        .andExpect(jsonPath("description", is(appointment.getDescription())))
        .andExpect(jsonPath("datetime", is(notNullValue())))
        .andExpect(jsonPath("status", is(appointment.getStatus().getValue())));

    assertEquals(1, appointmentRepository.count());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void postAppointmentShouldReturnBadRequestIfIdIsSet() throws Exception {
    givenAValidAppointmentDto(UUID.randomUUID(), AppointmentStatus.CREATED);
    givenAValidConsultant(true);

    mockMvc
        .perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void postAppointmentShouldReturnBadRequestIfStatusIsNull() throws Exception {
    givenAnAppointmentMissingStatus();
    givenAValidConsultant(true);

    mockMvc
        .perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void postAppointmentShouldReturnBadRequestIfDatetimeIsNull() throws Exception {
    givenAnAppointmentMissingDatetime();
    givenAValidConsultant(true);

    mockMvc
        .perform(
            post("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthorityValue.CONSULTANT_DEFAULT)
  void getAppointmentsShouldReturnAppointmentsOfTodayAndFutureOrderedByDatetimeAscending()
      throws Exception {
    var clockToday = LocalDateTime.of(2022, 2, 15, 17, 12).toInstant(ZoneOffset.UTC);
    when(clock.instant()).thenReturn(clockToday);
    givenAValidConsultant(true);
    var today = LocalDateTime.of(2022, 2, 15, 13, 37).toInstant(ZoneOffset.UTC);
    var tomorrow = LocalDateTime.of(2022, 2, 16, 14, 44).toInstant(ZoneOffset.UTC);
    var yesterday = LocalDateTime.of(2022, 2, 14, 7, 53).toInstant(ZoneOffset.UTC);
    givenASavedAppointment(yesterday);
    givenASavedAppointment(tomorrow);
    givenASavedAppointment(today);

    mockMvc
        .perform(
            get("/appointments")
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("[0].datetime", is(notNullValue())))
        .andExpect(jsonPath("[1].datetime", is(notNullValue())));
  }

  private void givenAValidAppointmentDto() {
    givenAValidAppointmentDto(UUID.randomUUID(), null);
  }

  private void givenAValidAppointmentDto(UUID id, AppointmentStatus setStatus) {
    appointment = easyRandom.nextObject(Appointment.class);
    appointment.setId(id);

    if (nonNull(setStatus)) {
      appointment.setStatus(setStatus);
    }

    var desc = appointment.getDescription();
    if (desc.length() > 300) {
      appointment.setDescription(desc.substring(0, 300));
    }
  }

  public void givenASavedAppointment() {
    givenASavedAppointment(null);
  }

  public void givenASavedAppointment(Instant datetime) {
    savedAppointment =
        easyRandom.nextObject(de.caritas.cob.userservice.api.model.Appointment.class);
    savedAppointment.setConsultant(consultant);
    savedAppointment.setId(null);
    savedAppointment.setBookingId(BOOKING_ID);
    var desc = savedAppointment.getDescription();
    if (desc.length() > 300) {
      savedAppointment.setDescription(desc.substring(0, 300));
    }
    if (nonNull(datetime)) {
      savedAppointment.setDatetime(datetime);
    }
    appointmentRepository.save(savedAppointment);
  }

  private void givenAnAppointmentMissingStatus() {
    givenAValidAppointmentDto(null, null);
    appointment.setStatus(null);
  }

  private void givenAnAppointmentMissingDatetime() {
    givenAValidAppointmentDto(null, null);
    appointment.setDatetime(null);
  }

  private void givenAValidConsultant(boolean isAuthUser) {
    consultant = consultantRepository.findAll().iterator().next();
    if (isAuthUser) {
      when(authenticatedUser.getUserId()).thenReturn(consultant.getId());
      when(authenticatedUser.isAdviceSeeker()).thenReturn(false);
      when(authenticatedUser.isConsultant()).thenReturn(true);
      when(authenticatedUser.getUsername()).thenReturn(consultant.getUsername());
      when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.CONSULTANT.getValue()));
      when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anAuthority"));
    }
  }

  private void givenAnAnonymousUser() {
    when(authenticatedUser.getUserId()).thenReturn(UUID.randomUUID().toString());
    when(authenticatedUser.isAdviceSeeker()).thenReturn(false);
    when(authenticatedUser.isConsultant()).thenReturn(false);
    when(authenticatedUser.getUsername()).thenReturn(RandomStringUtils.randomAlphabetic(8));
    when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.ANONYMOUS.getValue()));
    when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anotherAuthority"));
  }

  private void givenAnAdviceSeeker() {
    when(authenticatedUser.getUserId()).thenReturn(UUID.randomUUID().toString());
    when(authenticatedUser.isAdviceSeeker()).thenReturn(true);
    when(authenticatedUser.isConsultant()).thenReturn(false);
    when(authenticatedUser.getUsername()).thenReturn(RandomStringUtils.randomAlphabetic(8));
    when(authenticatedUser.getRoles()).thenReturn(Set.of(UserRole.USER.getValue()));
    when(authenticatedUser.getGrantedAuthorities()).thenReturn(Set.of("anotherAuthority"));
  }

  private void verifyRabbitMqMessageHasBeenSent(String routingKey) {
    verifyAsync(
        a ->
            amqpTemplate.convertAndSend(
                eq("statistics.topic"), eq(routingKey), ArgumentMatchers.<Message>any()));
  }
}

package de.caritas.cob.userservice.api.admin.service.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class SessionAdminServiceIT {

  @Autowired private SessionAdminService sessionAdminService;

  @Test
  public void findSessions_Should_returnAllSessions_When_noFilterIsGiven() {
    SessionAdminResultDTO sessions =
        this.sessionAdminService.findSessions(1, 100, new SessionFilter());

    assertThat(sessions.getEmbedded(), hasSize(100));
  }

  @Test
  public void findSessions_Should_returnOneSession_When_perPageIsNegativeValue() {
    SessionAdminResultDTO sessions =
        this.sessionAdminService.findSessions(1, -100, new SessionFilter());

    assertThat(sessions.getEmbedded(), hasSize(1));
  }

  @Test
  public void findSessions_Should_returnFullMappedSessionAdminDTO() {
    SessionAdminResultDTO sessions =
        this.sessionAdminService.findSessions(1, 1, new SessionFilter());

    SessionAdminDTO sessionAdminDTO = sessions.getEmbedded().iterator().next();
    assertThat(sessionAdminDTO.getAgencyId(), notNullValue());
    assertThat(sessionAdminDTO.getConsultantId(), notNullValue());
    assertThat(sessionAdminDTO.getConsultingType(), notNullValue());
    assertThat(sessionAdminDTO.getCreateDate(), notNullValue());
    assertThat(sessionAdminDTO.getEmail(), notNullValue());
    assertThat(sessionAdminDTO.getIsTeamSession(), notNullValue());
    assertThat(sessionAdminDTO.getMessageDate(), notNullValue());
    assertThat(sessionAdminDTO.getPostcode(), notNullValue());
    assertThat(sessionAdminDTO.getUpdateDate(), notNullValue());
    assertThat(sessionAdminDTO.getUserId(), notNullValue());
    assertThat(sessionAdminDTO.getUsername(), notNullValue());
  }

  @Test
  public void findSessions_Should_haveCorrectPagedResults_When_noFilterIsGiven() {
    SessionAdminResultDTO firstPage =
        this.sessionAdminService.findSessions(1, 100, new SessionFilter());
    SessionAdminResultDTO secondPage =
        this.sessionAdminService.findSessions(2, 100, new SessionFilter());

    assertThat(firstPage.getEmbedded(), hasSize(100));
    assertThat(secondPage.getEmbedded(), hasSize(57));
  }

  @Test
  public void findSessions_Should_returnSessionsFilteredByAgency_When_filterHasAgencySet() {
    SessionFilter sessionFilter = new SessionFilter().agency(1);

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, sessionFilter);

    sessions
        .getEmbedded()
        .forEach(sessionAdminDTO -> assertThat(sessionAdminDTO.getAgencyId(), is(1)));
  }

  @Test
  public void findSessions_Should_returnSessionsFilteredByConsultant_When_filterHasConsultantSet() {
    SessionFilter sessionFilter =
        new SessionFilter().consultant("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc");

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, sessionFilter);

    sessions
        .getEmbedded()
        .forEach(
            sessionAdminDTO ->
                assertThat(
                    sessionAdminDTO.getConsultantId(), is("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc")));
  }

  @Test
  public void findSessions_Should_returnSessionsFilteredByAsker_When_filterHasAskerSet() {
    SessionFilter sessionFilter = new SessionFilter().asker("a38983e3-43f7-49ac-ab61-18161fd45b69");

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, sessionFilter);

    sessions
        .getEmbedded()
        .forEach(
            sessionAdminDTO ->
                assertThat(
                    sessionAdminDTO.getUserId(), is("a38983e3-43f7-49ac-ab61-18161fd45b69")));
  }

  @Test
  public void
      findSessions_Should_returnSessionsFilteredByConsultingType_When_filterHasConsultingTypeSet() {
    SessionFilter sessionFilter = new SessionFilter().consultingType(1);

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, sessionFilter);

    sessions
        .getEmbedded()
        .forEach(sessionAdminDTO -> assertThat(sessionAdminDTO.getConsultingType(), is(1)));
  }

  @Test
  public void findSessions_Should_returnEmptyResult_When_filterHasInvalidAgencySet() {
    SessionFilter sessionFilter = new SessionFilter().agency(-20);

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, sessionFilter);

    assertThat(sessions.getEmbedded(), hasSize(0));
  }

  @Test
  public void findSessions_Should_returnEmptyResult_When_filterHasInvalidConsultantSet() {
    SessionFilter sessionFilter = new SessionFilter().consultant("invalid");

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, sessionFilter);

    assertThat(sessions.getEmbedded(), hasSize(0));
  }

  @Test
  public void findSessions_Should_returnEmptyResult_When_filterHasInvalidAskerSet() {
    SessionFilter sessionFilter = new SessionFilter().asker("invalid");

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, sessionFilter);

    assertThat(sessions.getEmbedded(), hasSize(0));
  }

  @Test
  public void findSessions_Should_returnEmptyResult_When_filterHasInvalidConsultingTypeSet() {
    SessionFilter sessionFilter = new SessionFilter().consultingType(-20);

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, sessionFilter);

    assertThat(sessions.getEmbedded(), hasSize(0));
  }
}

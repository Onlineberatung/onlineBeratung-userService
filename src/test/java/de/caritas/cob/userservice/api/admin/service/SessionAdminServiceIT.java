package de.caritas.cob.userservice.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.model.SessionAdminDTO;
import de.caritas.cob.userservice.api.model.SessionAdminResultDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class SessionAdminServiceIT {

  @Autowired
  private SessionAdminService sessionAdminService;

  @Test
  public void findSessions_Should_returnAllSessions_When_noFilterIsGiven() {
    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 100, new Filter());

    assertThat(sessions.getEmbedded(), hasSize(100));
  }

  @Test
  public void findSessions_Should_returnOneSession_When_perPageIsNegativeValue() {
    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, -100, new Filter());

    assertThat(sessions.getEmbedded(), hasSize(1));
  }

  @Test
  public void findSessions_Should_returnFullMappedSessionAdminDTO() {
    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 1, new Filter());

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
    SessionAdminResultDTO firstPage = this.sessionAdminService.findSessions(1, 100, new Filter());
    SessionAdminResultDTO secondPage = this.sessionAdminService.findSessions(2, 100, new Filter());

    assertThat(firstPage.getEmbedded(), hasSize(100));
    assertThat(secondPage.getEmbedded(), hasSize(41));
  }

  @Test
  public void findSessions_Should_returnSessionsFilteredByAgency_When_filterHasAgencySet() {
    Filter filter = new Filter().agency(1);

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, filter);

    sessions.getEmbedded().forEach(sessionAdminDTO ->
        assertThat(sessionAdminDTO.getAgencyId(), is(1))
    );
  }

  @Test
  public void findSessions_Should_returnSessionsFilteredByConsultant_When_filterHasConsultantSet() {
    Filter filter = new Filter().consultant("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc");

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, filter);

    sessions.getEmbedded().forEach(sessionAdminDTO ->
        assertThat(sessionAdminDTO.getConsultantId(), is("bad14912-cf9f-4c16-9d0e-fe8ede9b60dc"))
    );
  }

  @Test
  public void findSessions_Should_returnSessionsFilteredByAsker_When_filterHasAskerSet() {
    Filter filter = new Filter().asker("a38983e3-43f7-49ac-ab61-18161fd45b69");

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, filter);

    sessions.getEmbedded().forEach(sessionAdminDTO ->
        assertThat(sessionAdminDTO.getUserId(), is("a38983e3-43f7-49ac-ab61-18161fd45b69"))
    );
  }

  @Test
  public void findSessions_Should_returnSessionsFilteredByConsultingType_When_filterHasConsultingTypeSet() {
    Filter filter = new Filter().consultingType(1);

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, filter);

    sessions.getEmbedded().forEach(sessionAdminDTO ->
        assertThat(sessionAdminDTO.getConsultingType(), is(1))
    );
  }

  @Test
  public void findSessions_Should_returnEmptyResult_When_filterHasInvalidAgencySet() {
    Filter filter = new Filter().agency(-20);

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, filter);

    assertThat(sessions.getEmbedded(), hasSize(0));
  }

  @Test
  public void findSessions_Should_returnEmptyResult_When_filterHasInvalidConsultantSet() {
    Filter filter = new Filter().consultant("invalid");

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, filter);

    assertThat(sessions.getEmbedded(), hasSize(0));
  }

  @Test
  public void findSessions_Should_returnEmptyResult_When_filterHasInvalidAskerSet() {
    Filter filter = new Filter().asker("invalid");

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, filter);

    assertThat(sessions.getEmbedded(), hasSize(0));
  }

  @Test
  public void findSessions_Should_returnEmptyResult_When_filterHasInvalidConsultingTypeSet() {
    Filter filter = new Filter().consultingType(-20);

    SessionAdminResultDTO sessions = this.sessionAdminService.findSessions(1, 200, filter);

    assertThat(sessions.getEmbedded(), hasSize(0));
  }

}

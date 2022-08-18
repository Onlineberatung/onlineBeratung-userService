package de.caritas.cob.userservice.api.admin.service.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.model.Session;
import java.util.stream.Stream;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class SessionAdminResultDTOBuilderTest {

  private static final int MOCKED_SESSIONS_SIZE = 20;

  @Mock private Page<Session> resultPage;

  void givenRandomSesisons() {
    EasyRandom easyRandom = new EasyRandom();
    Stream<Session> randomSessions = easyRandom.objects(Session.class, MOCKED_SESSIONS_SIZE);
    when(resultPage.get()).thenReturn(randomSessions);
  }

  @Test
  void build_Should_returnEmptySessionAdminResultDTOWithDefaultSelfLink_When_noParametersAreSet() {
    SessionAdminResultDTO resultDTO = SessionAdminResultDTOBuilder.getInstance().build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO.getEmbedded(), hasSize(0));
    assertThat(resultDTO.getTotal(), is(0));
    assertThat(resultDTO.getLinks(), notNullValue());
    assertThat(resultDTO.getLinks().getNext(), nullValue());
    assertThat(resultDTO.getLinks().getPrevious(), nullValue());
    assertThat(resultDTO.getLinks().getSelf(), notNullValue());
    assertThat(
        resultDTO.getLinks().getSelf().getHref(), is("/useradmin/sessions?page=1&perPage=20"));
  }

  @Test
  void build_Should_returnSessionAdminResultDTOWithAllLinks_When_parametersAreSet() {
    givenRandomSesisons();
    when(this.resultPage.getTotalPages()).thenReturn(MOCKED_SESSIONS_SIZE / 2);
    when(this.resultPage.getTotalElements()).thenReturn(Long.valueOf(MOCKED_SESSIONS_SIZE));

    SessionAdminResultDTO resultDTO =
        SessionAdminResultDTOBuilder.getInstance()
            .withFilter(new SessionFilter().agency(1))
            .withPage(2)
            .withPerPage(2)
            .withResultPage(this.resultPage)
            .build();

    assertThat(resultDTO, notNullValue());
    assertThat(resultDTO.getEmbedded(), hasSize(20));
    assertThat(resultDTO.getTotal(), is(MOCKED_SESSIONS_SIZE));
    assertThat(
        resultDTO.getLinks().getNext().getHref(), is("/useradmin/sessions?page=3&perPage=2"));
    assertThat(
        resultDTO.getLinks().getPrevious().getHref(),
        is("/useradmin/sessions?page=1" + "&perPage=2"));
    assertThat(
        resultDTO.getLinks().getSelf().getHref(), is("/useradmin/sessions?page=2&perPage=2"));
  }

  @Test
  void build_Should_returnSessionAdminResultDTOWithoutPreviousLink_When_parametersPageIsTheFirst() {
    givenRandomSesisons();
    when(this.resultPage.getTotalPages()).thenReturn(MOCKED_SESSIONS_SIZE / 2);

    SessionAdminResultDTO resultDTO =
        SessionAdminResultDTOBuilder.getInstance()
            .withPage(1)
            .withPerPage(2)
            .withResultPage(this.resultPage)
            .build();

    assertThat(resultDTO.getLinks().getPrevious(), nullValue());
  }

  @Test
  void build_Should_returnSessionAdminResultDTOWithoutNextLink_When_parametersPageIsTheLast() {
    givenRandomSesisons();
    when(this.resultPage.getTotalPages()).thenReturn(MOCKED_SESSIONS_SIZE / 2);

    SessionAdminResultDTO resultDTO =
        SessionAdminResultDTOBuilder.getInstance()
            .withPage(10)
            .withPerPage(2)
            .withResultPage(this.resultPage)
            .build();

    assertThat(resultDTO.getLinks().getNext(), nullValue());
  }
}

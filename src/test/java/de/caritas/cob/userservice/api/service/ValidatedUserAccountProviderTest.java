package de.caritas.cob.userservice.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.AbsenceDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.user.User;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidatedUserAccountProviderTest {

  @InjectMocks
  private ValidatedUserAccountProvider accountProvider;

  @Mock
  private UserService userService;

  @Mock
  private ConsultantService consultantService;

  @Mock
  private AuthenticatedUser authenticatedUser;

  @Test
  public void retrieveValidatedUser_Should_ReturnUser_When_UserIsPresent() {
    User userMock = mock(User.class);
    when(userService.getUser(any())).thenReturn(Optional.of(userMock));

    User resultUser = this.accountProvider.retrieveValidatedUser();

    assertThat(resultUser, is(userMock));
  }

  @Test(expected = InternalServerErrorException.class)
  public void retrieveValidatedUser_Should_Throw_InternalServerErrorException_When_UserIsNotPresent() {
    when(userService.getUser(any())).thenReturn(Optional.empty());

    this.accountProvider.retrieveValidatedUser();
  }

  @Test
  public void retrieveValidatedConsultant_Should_ReturnConsultant_When_ConsultantIsPresent() {
    Consultant consultantMock = mock(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultantMock));

    Consultant resultUser = this.accountProvider.retrieveValidatedConsultant();

    assertThat(resultUser, is(consultantMock));
  }

  @Test(expected = InternalServerErrorException.class)
  public void retrieveValidatedConsultant_Should_Throw_InternalServerErrorException_When_ConsultantIsNotPresent() {
    when(consultantService.getConsultant(any())).thenReturn(Optional.empty());

    this.accountProvider.retrieveValidatedConsultant();
  }

  @Test
  public void retrieveValidatedTeamConsultant_Should_ReturnTeamConsultant_When_TeamConsultantIsPresent() {
    Consultant teamConsultantMock = mock(Consultant.class);
    when(teamConsultantMock.isTeamConsultant()).thenReturn(true);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(teamConsultantMock));

    Consultant resultUser = this.accountProvider.retrieveValidatedTeamConsultant();

    assertThat(resultUser, is(teamConsultantMock));
  }

  @Test(expected = ForbiddenException.class)
  public void retrieveValidatedTeamConsultant_Should_Throw_ForbiddenException_When_ConsultantIsNotATeamConsultant() {
    Consultant consultantMock = mock(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultantMock));

    this.accountProvider.retrieveValidatedTeamConsultant();
  }

  @Test
  public void updateConsultantAbsent_Should_CallUpdateConsultantAbsent_When_ConsultantIsPresent() {
    Consultant consultantMock = mock(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(consultantMock));

    AbsenceDTO absenceDTO = mock(AbsenceDTO.class);
    this.accountProvider.updateConsultantAbsent(absenceDTO);

    verify(consultantService, times(1)).updateConsultantAbsent(eq(consultantMock), eq(absenceDTO));
  }

}

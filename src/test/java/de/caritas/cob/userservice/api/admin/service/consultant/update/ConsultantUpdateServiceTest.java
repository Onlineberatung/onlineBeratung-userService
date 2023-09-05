package de.caritas.cob.userservice.api.admin.service.consultant.update;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantUpdateServiceTest {

  @InjectMocks private ConsultantUpdateService consultantUpdateService;

  @Mock private KeycloakService keycloakService;

  @Mock private ConsultantService consultantService;

  @Mock private UserAccountInputValidator userAccountInputValidator;

  @Mock private RocketChatService rocketChatService;

  @Mock private AppointmentService appointmentService;

  @Test(expected = BadRequestException.class)
  public void
      updateConsultant_Should_throwBadRequestException_When_givenConsultantIdDoesNotExist() {
    when(this.consultantService.getConsultant(any())).thenReturn(Optional.empty());

    this.consultantUpdateService.updateConsultant("", mock(UpdateAdminConsultantDTO.class));
  }

  @Test
  public void updateConsultant_Should_callServicesCorrectly_When_givenConsultantDataIsValid() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.consultantService.getConsultant(any())).thenReturn(Optional.of(consultant));
    UpdateAdminConsultantDTO updateConsultant =
        new EasyRandom().nextObject(UpdateAdminConsultantDTO.class);
    updateConsultant.setIsGroupchatConsultant(null);

    this.consultantUpdateService.updateConsultant("", updateConsultant);

    verify(this.keycloakService, Mockito.never())
        .updateRole(consultant.getId(), UserRole.GROUP_CHAT_CONSULTANT.getValue());

    verify(this.keycloakService, times(1))
        .updateUserData(
            eq(consultant.getId()),
            any(UserDTO.class),
            eq(updateConsultant.getFirstname()),
            eq(updateConsultant.getLastname()));
    verify(this.consultantService, times(1)).saveConsultant(any());
    verify(this.appointmentService, times(1)).syncConsultantData(any());
  }

  @Test
  public void
      updateConsultant_Should_callServicesCorrectly_And_AddGroupChatConsultantRole_When_givenConsultantDataIsValidAndGroupChatFlagIsGiven() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.consultantService.getConsultant(any())).thenReturn(Optional.of(consultant));
    UpdateAdminConsultantDTO updateConsultant =
        new EasyRandom().nextObject(UpdateAdminConsultantDTO.class);
    updateConsultant.setIsGroupchatConsultant(true);

    this.consultantUpdateService.updateConsultant("", updateConsultant);

    verify(this.keycloakService)
        .updateRole(consultant.getId(), UserRole.GROUP_CHAT_CONSULTANT.getValue());

    verify(this.keycloakService, times(1))
        .updateUserData(
            eq(consultant.getId()),
            any(UserDTO.class),
            eq(updateConsultant.getFirstname()),
            eq(updateConsultant.getLastname()));
    verify(this.consultantService, times(1)).saveConsultant(any());
    verify(this.appointmentService, times(1)).syncConsultantData(any());
  }

  @Test
  public void
      updateConsultant_Should_callServicesCorrectly_And_RemoveGroupChatConsultantRole_When_givenConsultantDataIsValidAndGroupChatFlagIsGiven() {
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.consultantService.getConsultant(any())).thenReturn(Optional.of(consultant));
    UpdateAdminConsultantDTO updateConsultant =
        new EasyRandom().nextObject(UpdateAdminConsultantDTO.class);
    updateConsultant.setIsGroupchatConsultant(false);

    this.consultantUpdateService.updateConsultant("", updateConsultant);

    verify(this.keycloakService)
        .removeRoleIfPresent(consultant.getId(), UserRole.GROUP_CHAT_CONSULTANT.getValue());

    verify(this.keycloakService, times(1))
        .updateUserData(
            eq(consultant.getId()),
            any(UserDTO.class),
            eq(updateConsultant.getFirstname()),
            eq(updateConsultant.getLastname()));
    verify(this.consultantService, times(1)).saveConsultant(any());
    verify(this.appointmentService, times(1)).syncConsultantData(any());
  }
}

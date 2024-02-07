package de.caritas.cob.userservice.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.admin.service.consultant.TransactionalStep;
import de.caritas.cob.userservice.api.exception.httpresponses.DistributedTransactionException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatchConsultantSagaTest {

  private static final String CHANGED_DISPLAY_NAME = "new displayName";
  private static final String CONSULTANT_ID = "consultantId";
  private static final String ROCKETCHAT_ID = "rocketChatId";
  @InjectMocks PatchConsultantSaga patchConsultantSaga;

  @Mock ConsultantRepository consultantRepository;

  @Mock UserServiceMapper userServiceMapper;

  @Mock MessageClient messageClient;

  @Mock AppointmentService appointmentService;

  @Mock PatchConsultantSagaRollbackHandler patchConsultantSagaRollbackHandler;

  @Test
  void
      executeTransactionalOrRollback_Should_SaveConsultantInMariaDB_And_UpdateRocketChat_And_AppointmentService() {
    // given
    Map<String, Object> patchMap = givenPatchMapWithDisplayName();
    givenUserServiceMapper();
    when(messageClient.updateUser(Mockito.eq(ROCKETCHAT_ID), Mockito.anyString())).thenReturn(true);
    Consultant patchedConsultant =
        Consultant.builder()
            .rocketChatId(ROCKETCHAT_ID)
            .id(CONSULTANT_ID)
            .username("username")
            .firstName("firstname")
            .lastName("lastname")
            .email("email")
            .languageCode(LanguageCode.de)
            .build();
    when(consultantRepository.save(patchedConsultant)).thenReturn(patchedConsultant);

    // when
    patchConsultantSaga.executeTransactional(patchedConsultant, patchMap);

    // then
    verify(consultantRepository).save(patchedConsultant);
    verify(messageClient).updateUser(Mockito.eq(ROCKETCHAT_ID), Mockito.anyString());
    verify(appointmentService).patchConsultant(CONSULTANT_ID, CHANGED_DISPLAY_NAME);
  }

  @Test
  void
      executeTransactionalOrRollback_Should_RollbackUpdateRocketChat_When_AppointmentService_ThrowsException() {
    // given
    Map<String, Object> patchMap = givenPatchMapWithDisplayName();
    givenUserServiceMapper();
    when(userServiceMapper.displayNameOf(patchMap))
        .thenReturn(java.util.Optional.of(CHANGED_DISPLAY_NAME));
    when(messageClient.updateUser(Mockito.eq(ROCKETCHAT_ID), Mockito.anyString())).thenReturn(true);
    Consultant patchedConsultant =
        Consultant.builder()
            .rocketChatId(ROCKETCHAT_ID)
            .id(CONSULTANT_ID)
            .username("username")
            .firstName("firstname")
            .lastName("lastname")
            .email("email")
            .languageCode(LanguageCode.de)
            .build();
    when(consultantRepository.save(patchedConsultant)).thenReturn(patchedConsultant);
    doThrow(new RuntimeException())
        .when(appointmentService)
        .patchConsultant(Mockito.anyString(), Mockito.anyString());

    try {
      // when
      patchConsultantSaga.executeTransactional(patchedConsultant, patchMap);
      fail("Expected DistributedTransactionException");
    } catch (DistributedTransactionException ex) {
      // then
      verify(consultantRepository).save(patchedConsultant);
      verify(messageClient).updateUser(Mockito.eq(ROCKETCHAT_ID), Mockito.anyString());
      verify(appointmentService).patchConsultant(CONSULTANT_ID, CHANGED_DISPLAY_NAME);
      verify(patchConsultantSagaRollbackHandler).rollbackUpdateUserInRocketchat(patchedConsultant);
      assertThat(ex.getMessage())
          .contains(TransactionalStep.PATCH_APPOINTMENT_SERVICE_CONSULTANT.name());
    }
  }

  @Test
  void
      executeTransactionalOrRollback_Should_Not_CallAppointmentService_When_RocketchatService_ThrowsException() {
    // given
    Map<String, Object> patchMap = givenPatchMapWithDisplayName();
    when(userServiceMapper.encodedDisplayNameOf(Mockito.anyMap()))
        .thenReturn(java.util.Optional.of(CHANGED_DISPLAY_NAME));

    Consultant patchedConsultant =
        Consultant.builder()
            .rocketChatId(ROCKETCHAT_ID)
            .id(CONSULTANT_ID)
            .username("username")
            .firstName("firstname")
            .lastName("lastname")
            .email("email")
            .languageCode(LanguageCode.de)
            .build();
    when(consultantRepository.save(patchedConsultant)).thenReturn(patchedConsultant);
    doThrow(new RuntimeException())
        .when(messageClient)
        .updateUser(Mockito.anyString(), Mockito.anyString());

    try {
      // when
      patchConsultantSaga.executeTransactional(patchedConsultant, patchMap);
      fail("Expected DistributedTransactionException");
    } catch (DistributedTransactionException ex) {
      // then
      verify(consultantRepository).save(patchedConsultant);
      verify(messageClient).updateUser(Mockito.eq(ROCKETCHAT_ID), Mockito.anyString());
      verify(appointmentService, Mockito.never())
          .patchConsultant(CONSULTANT_ID, CHANGED_DISPLAY_NAME);
      verify(patchConsultantSagaRollbackHandler, Mockito.never())
          .rollbackUpdateUserInRocketchat(patchedConsultant);
      assertThat(ex.getMessage())
          .contains(TransactionalStep.UPDATE_ROCKET_CHAT_USER_DISPLAY_NAME.name());
    }
  }

  @NotNull
  private Map<String, Object> givenPatchMapWithDisplayName() {
    Map<String, Object> patchMap = Maps.newHashMap();
    patchMap.put("displayName", CHANGED_DISPLAY_NAME);
    return patchMap;
  }

  private void givenUserServiceMapper() {
    when(userServiceMapper.displayNameOf(Mockito.anyMap()))
        .thenReturn(java.util.Optional.of(CHANGED_DISPLAY_NAME));
    when(userServiceMapper.encodedDisplayNameOf(Mockito.anyMap()))
        .thenReturn(java.util.Optional.of(CHANGED_DISPLAY_NAME));
  }
}

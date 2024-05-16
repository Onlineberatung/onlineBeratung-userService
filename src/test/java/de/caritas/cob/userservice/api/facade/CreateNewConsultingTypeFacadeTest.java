package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_KREUZBUND;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_KREUZBUND;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.INVALID_CONSULTING_TYPE_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.UNKNOWN_CONSULTING_TYPE_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.web.dto.NewRegistrationResponseDto;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateNewConsultingTypeFacadeTest {

  @InjectMocks private CreateNewConsultingTypeFacade createNewConsultingTypeFacade;
  @Mock private ConsultingTypeManager consultingTypeManager;
  @Mock private CreateUserChatRelationFacade createUserChatRelationFacade;
  @Mock private CreateSessionFacade createSessionFacade;
  @Mock private StatisticsService statisticsService;

  @Test
  public void
      initializeNewConsultingType_Should_RegisterNewSession_When_ProvidedWithSessionConsultingType_For_NewAccountRegistrations() {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setConsultingType(String.valueOf(0));
    userDTO.setConsultantId(null);
    User user = easyRandom.nextObject(User.class);
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        easyRandom.nextObject(ExtendedConsultingTypeResponseDTO.class);
    extendedConsultingTypeResponseDTO.setId(0);

    createNewConsultingTypeFacade.initializeNewConsultingType(
        userDTO, user, extendedConsultingTypeResponseDTO);
    if (!extendedConsultingTypeResponseDTO.getGroupChat().getIsGroupChat()) {
      verify(createSessionFacade, times(1)).createUserSession(any(), any(), any());
    } else {
      verify(createUserChatRelationFacade, times(1))
          .initializeUserChatAgencyRelation(any(), any(), any());
    }
  }

  @Test
  public void
      initializeNewConsultingType_Should_RegisterNewSessionAndReturnSessionId_When_ProvidedWithSessionConsultingType_For_NewConsultingTypeRegistrations() {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setConsultingType(String.valueOf(CONSULTING_TYPE_ID_SUCHT));
    userDTO.setConsultantId(null);
    User user = easyRandom.nextObject(User.class);
    RocketChatCredentials rocketChatCredentials =
        easyRandom.nextObject(RocketChatCredentials.class);

    when(createSessionFacade.createUserSession(any(), any(), any())).thenReturn(1L);
    when(consultingTypeManager.getConsultingTypeSettings("0"))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    var responseDto =
        createNewConsultingTypeFacade.initializeNewConsultingType(
            userDTO, user, rocketChatCredentials);

    assertEquals(responseDto.getSessionId().longValue(), 1L);
    verify(createSessionFacade, times(1)).createUserSession(any(), any(), any());
  }

  @Test
  public void
      initializeNewConsultingType_Should_RegisterNewUserChatRelationAndReturnNull_When_ProvidedWithGroupChatConsultingType_For_NewConsultingTypeRegistrations() {
    EasyRandom easyRandom = new EasyRandom();
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setConsultingType(String.valueOf(CONSULTING_TYPE_ID_KREUZBUND));
    userDTO.setConsultantId(null);
    User user = easyRandom.nextObject(User.class);
    RocketChatCredentials rocketChatCredentials =
        easyRandom.nextObject(RocketChatCredentials.class);

    when(consultingTypeManager.getConsultingTypeSettings("15"))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);

    var responseDto =
        createNewConsultingTypeFacade.initializeNewConsultingType(
            userDTO, user, rocketChatCredentials);

    assertNull(responseDto.getSessionId());
    assertThat(responseDto.getStatus(), is(CREATED));
    verify(createUserChatRelationFacade, times(1))
        .initializeUserChatAgencyRelation(any(), any(), any());
  }

  @Test
  public void
      initializeNewConsultingType_Should_ThrowBadRequestException_When_ProvidedWithInvalidConsultingType_For_NewConsultingTypeRegistrations() {
    assertThrows(
        BadRequestException.class,
        () -> {
          EasyRandom easyRandom = new EasyRandom();
          UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
          userDTO.setConsultingType(INVALID_CONSULTING_TYPE_ID);
          User user = easyRandom.nextObject(User.class);
          RocketChatCredentials rocketChatCredentials =
              easyRandom.nextObject(RocketChatCredentials.class);
          when(consultingTypeManager.getConsultingTypeSettings(INVALID_CONSULTING_TYPE_ID))
              .thenThrow(new NumberFormatException(""));
          createNewConsultingTypeFacade.initializeNewConsultingType(
              userDTO, user, rocketChatCredentials);

          verify(createUserChatRelationFacade, times(0))
              .initializeUserChatAgencyRelation(any(), any(), any());
        });
  }

  @Test
  public void
      initializeNewConsultingType_Should_ThrowBadRequestException_When_ProvidedWithUnknownConsultingType_For_NewConsultingTypeRegistrations() {
    assertThrows(
        BadRequestException.class,
        () -> {
          EasyRandom easyRandom = new EasyRandom();
          UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
          userDTO.setConsultingType(UNKNOWN_CONSULTING_TYPE_ID);
          User user = easyRandom.nextObject(User.class);
          RocketChatCredentials rocketChatCredentials =
              easyRandom.nextObject(RocketChatCredentials.class);
          when(consultingTypeManager.getConsultingTypeSettings(UNKNOWN_CONSULTING_TYPE_ID))
              .thenThrow(new MissingConsultingTypeException(""));
          createNewConsultingTypeFacade.initializeNewConsultingType(
              userDTO, user, rocketChatCredentials);

          verify(createUserChatRelationFacade, times(0))
              .initializeUserChatAgencyRelation(any(), any(), any());
        });
  }

  @Test
  public void
      initializeNewConsultingType_Should_callCreateDirectSession_When_ProvidedWithExitsingConsultantId() {
    EasyRandom easyRandom = new EasyRandom();
    var userDTO = new UserDTO();
    userDTO.setConsultantId("consultantId");
    userDTO.setConsultingType(String.valueOf(CONSULTING_TYPE_ID_KREUZBUND));
    User user = easyRandom.nextObject(User.class);
    var rocketChatCredentials = easyRandom.nextObject(RocketChatCredentials.class);

    when(consultingTypeManager.getConsultingTypeSettings("15"))
        .thenReturn(CONSULTING_TYPE_SETTINGS_KREUZBUND);

    NewRegistrationResponseDto mockResult = mock(NewRegistrationResponseDto.class);
    when(mockResult.getSessionId()).thenReturn(1L);
    when(createSessionFacade.createDirectUserSession(any(), any(), any(), any()))
        .thenReturn(mockResult);

    createNewConsultingTypeFacade.initializeNewConsultingType(userDTO, user, rocketChatCredentials);

    verify(createSessionFacade)
        .createDirectUserSession("consultantId", userDTO, user, CONSULTING_TYPE_SETTINGS_KREUZBUND);
    verify(this.statisticsService, times(1)).fireEvent(any());
  }
}

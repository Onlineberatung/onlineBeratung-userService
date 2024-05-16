package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ABSENCE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ABSENCE_DTO_WITH_EMPTY_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ABSENCE_DTO_WITH_HTML_AND_JS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ABSENCE_DTO_WITH_NULL_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_SESSION_RESPONSE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.AccountManager;
import de.caritas.cob.userservice.api.adapters.web.dto.GroupSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.GroupSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.UserDtoMapper;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConsultantDataFacadeTest {

  @InjectMocks private ConsultantDataFacade consultantDataFacade;
  @Mock private ConsultantService consultantService;
  @Mock private AccountManager accountManager;
  @Mock private UserDtoMapper userDtoMapper;

  @Test
  public void updateConsultantAbsent_Should_UpdateAbsenceMessageAndIsAbsence() {
    when(consultantService.saveConsultant(Mockito.any(Consultant.class))).thenReturn(CONSULTANT);

    Consultant consultant = consultantDataFacade.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO);

    assertEquals(consultant.getAbsenceMessage(), ABSENCE_DTO.getMessage());
    assertEquals(consultant.isAbsent(), ABSENCE_DTO.getAbsent());
  }

  @Test
  public void
      saveEnquiryMessageAndRocketChatGroupId_Should_RemoveHtmlCodeAndJsFromMessageForXssProtection() {
    when(consultantService.saveConsultant(Mockito.any(Consultant.class))).thenReturn(CONSULTANT);

    Consultant consultant =
        consultantDataFacade.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO_WITH_HTML_AND_JS);

    assertEquals(consultant.isAbsent(), ABSENCE_DTO_WITH_HTML_AND_JS.getAbsent());
    assertNotEquals(consultant.getAbsenceMessage(), ABSENCE_DTO_WITH_HTML_AND_JS.getMessage());
    assertEquals(MESSAGE, consultant.getAbsenceMessage());
  }

  @Test
  public void
      updateConsultantAbsent_Should_SetAbsenceMessageToNull_WhenAbsenceMessageFromDtoIsEmpty() {
    consultantDataFacade.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO_WITH_EMPTY_MESSAGE);

    ArgumentCaptor<Consultant> captor = ArgumentCaptor.forClass(Consultant.class);
    verify(consultantService).saveConsultant(captor.capture());
    assertNull(captor.getValue().getAbsenceMessage());
  }

  @Test
  public void
      updateConsultantAbsent_Should_SetAbsenceMessageToNull_WhenAbsenceMessageFromDtoIsNull() {
    consultantDataFacade.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO_WITH_NULL_MESSAGE);

    ArgumentCaptor<Consultant> captor = ArgumentCaptor.forClass(Consultant.class);
    verify(consultantService).saveConsultant(captor.capture());
    assertNull(captor.getValue().getAbsenceMessage());
  }

  @Test
  public void addConsultantDisplayNameToSessionList_GroupSession_Should_AddConsultantDisplayName() {

    List<GroupSessionResponseDTO> sessions = new ArrayList<>();
    sessions.add(GROUP_SESSION_RESPONSE_DTO);

    GroupSessionListResponseDTO response = new GroupSessionListResponseDTO().sessions(sessions);

    var userName = RandomStringUtils.randomAlphanumeric(16);
    sessions.get(0).getConsultant().setUsername(userName);
    var displayName = RandomStringUtils.randomAlphanumeric(16);

    Map<String, Object> map = Map.of("displayName", displayName);
    when(userDtoMapper.displayNameOf(map)).thenReturn(displayName);
    when(accountManager.findConsultantByUsername(userName)).thenReturn(Optional.of(map));

    consultantDataFacade.addConsultantDisplayNameToSessionList(response);

    assertEquals(displayName, response.getSessions().get(0).getConsultant().getDisplayName());
  }

  @Test
  public void addConsultantDisplayNameToSessionList_UserSession_Should_AddConsultantDisplayName() {

    List<UserSessionResponseDTO> sessions = new ArrayList<>();
    sessions.add(USER_SESSION_RESPONSE_DTO);

    UserSessionListResponseDTO response = new UserSessionListResponseDTO().sessions(sessions);

    var userName = RandomStringUtils.randomAlphanumeric(16);
    sessions.get(0).getConsultant().setUsername(userName);
    var displayName = RandomStringUtils.randomAlphanumeric(16);

    Map<String, Object> map = Map.of("displayName", displayName);
    when(userDtoMapper.displayNameOf(map)).thenReturn(displayName);
    when(accountManager.findConsultantByUsername(userName)).thenReturn(Optional.of(map));

    consultantDataFacade.addConsultantDisplayNameToSessionList(response);

    assertEquals(displayName, response.getSessions().get(0).getConsultant().getDisplayName());
  }
}

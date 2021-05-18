package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoveConsultantFromRocketChatServiceTest {

  @InjectMocks
  private RemoveConsultantFromRocketChatService removeConsultantFromRocketChatService;

  @Mock
  private RocketChatFacade rocketChatFacade;

  @Mock
  private ConsultantRepository consultantRepository;

  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;

  @Mock
  private ConsultingTypeManager consultingTypeManager;

  @Test
  public void removeConsultantFromSessions_Should_removeConsultant_When_consultantIsNotUserAndNotDireclyAssigned() {
    Session session = new EasyRandom().nextObject(Session.class);
    session.getConsultant().setRocketChatId("consultant");
    session.getUser().setRcUserId("user");
    GroupMemberDTO groupMemberDTO = new EasyRandom().nextObject(GroupMemberDTO.class);
    groupMemberDTO.set_id("another");
    Consultant consultant = new EasyRandom().nextObject(Consultant.class);
    when(this.consultantRepository.findByRocketChatIdAndDeleteDateIsNull(any()))
        .thenReturn(Optional.of(consultant));
    GroupMemberDTO otherConsultant = new GroupMemberDTO();
    otherConsultant.set_id(consultant.getRocketChatId());
    when(this.rocketChatFacade.getStandardMembersOfGroup(any()))
        .thenReturn(singletonList(groupMemberDTO));
    when(this.rocketChatFacade.retrieveRocketChatMembers(any()))
        .thenReturn(singletonList(otherConsultant));
    when(this.keycloakAdminClientService.userHasAuthority(any(), any())).thenReturn(false);

    this.removeConsultantFromRocketChatService.removeConsultantFromSessions(singletonList(session));

    verify(this.rocketChatFacade, times(1))
        .removeUserFromGroup(eq(consultant.getRocketChatId()), eq(session.getGroupId()));
    verify(this.rocketChatFacade, times(1))
        .removeUserFromGroup(eq(consultant.getRocketChatId()), eq(session.getFeedbackGroupId()));
  }

}

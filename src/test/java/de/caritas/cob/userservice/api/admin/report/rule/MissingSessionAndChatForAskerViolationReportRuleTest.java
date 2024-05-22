package de.caritas.cob.userservice.api.admin.report.rule;

import static de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO.ViolationTypeEnum.ASKER;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MissingSessionAndChatForAskerViolationReportRuleTest {

  @InjectMocks private MissingSessionAndChatForAskerViolationReportRule reportRule;

  @Mock private UserRepository userRepository;

  @Mock private UserAgencyRepository userAgencyRepository;

  @Test
  public void generateViolations_Should_returnEmptyList_When_noViolationExists() {
    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(0));
  }

  @Test
  public void generateViolations_Should_returnExpectedViolation_When_oneViolatedAskerExists() {
    User violatedUser = new EasyRandom().nextObject(User.class);
    violatedUser.setSessions(null);
    when(this.userRepository.findAll()).thenReturn(singletonList(violatedUser));

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(1));
    ViolationDTO resultViolation = violations.iterator().next();
    assertThat(resultViolation.getIdentifier(), is(violatedUser.getUserId()));
    assertThat(resultViolation.getViolationType(), is(ASKER));
    assertThat(resultViolation.getReason(), is("Use has neither chat nor session relation"));
  }

  @Test
  public void generateViolations_Should_returnViolationsOnlyForAskersWithoutSessions() {
    List<User> users = new EasyRandom().objects(User.class, 10).collect(Collectors.toList());
    users.get(0).setSessions(null);
    users.get(2).setSessions(null);
    users.get(4).setSessions(null);
    users.get(6).setSessions(null);
    users.get(9).setSessions(null);
    when(this.userRepository.findAll()).thenReturn(users);

    List<ViolationDTO> violations = this.reportRule.generateViolations();

    assertThat(violations, hasSize(5));
  }
}

package de.caritas.cob.userservice.api.admin.report.rule;

import static de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO.ViolationTypeEnum.ASKER;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Violation rule to find askers without session or chat relation. */
@Component
@RequiredArgsConstructor
public class MissingSessionAndChatForAskerViolationReportRule implements ViolationReportRule {

  private final @NonNull UserRepository userRepository;
  private final @NonNull UserAgencyRepository userAgencyRepository;

  /**
   * Generates all violations for {@link User} without session or chat assignment.
   *
   * @return the generated violations
   */
  @Override
  public List<ViolationDTO> generateViolations() {
    return StreamSupport.stream(this.userRepository.findAll().spliterator(), false)
        .filter(this::withoutSessionAndChat)
        .map(this::fromUser)
        .collect(Collectors.toList());
  }

  private boolean withoutSessionAndChat(User user) {
    return isEmpty(user.getSessions()) && isEmpty(this.userAgencyRepository.findByUser(user));
  }

  private ViolationDTO fromUser(User user) {
    return new ViolationDTO()
        .identifier(user.getUserId())
        .violationType(ASKER)
        .reason("Use has neither chat nor session relation");
  }
}

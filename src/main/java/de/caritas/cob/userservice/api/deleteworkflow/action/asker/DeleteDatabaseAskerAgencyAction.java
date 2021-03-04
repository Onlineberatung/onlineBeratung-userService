package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import static de.caritas.cob.userservice.api.deleteworkflow.action.ActionOrder.THIRD;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgency;
import de.caritas.cob.userservice.api.repository.useragency.UserAgencyRepository;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deletes a {@link UserAgency} in database.
 */
@Component
@RequiredArgsConstructor
public class DeleteDatabaseAskerAgencyAction implements DeleteAskerAction {

  private final @NonNull UserAgencyRepository userAgencyRepository;

  /**
   * Deletes all {@link User} regarding {@link UserAgency} relations.
   *
   * @param userAccount the {@link User}
   * @return a possible generated {@link DeletionWorkflowError}
   */
  @Override
  public List<DeletionWorkflowError> execute(User userAccount) {
    try {
      List<UserAgency> userAgencies = this.userAgencyRepository.findByUser(userAccount);
      this.userAgencyRepository.deleteAll(userAgencies);
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      return singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DATABASE)
              .identifier(userAccount.getUserId())
              .reason("Could not delete user agency relations")
              .timestamp(nowInUtc())
              .build()
      );
    }
    return emptyList();
  }

  /**
   * Provides the execution order.
   *
   * @return the value for the execution order
   */
  @Override
  public int getOrder() {
    return THIRD.getOrder();
  }
}

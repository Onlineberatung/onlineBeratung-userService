package de.caritas.cob.userservice.api.facade.rollback;

import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import lombok.Builder;
import lombok.Data;

/**
 * Representation of needed information to perform a roll back of an user account and its
 * corresponding sessions and/or user-chat/agency relations.
 */
@Data
@Builder
public class RollbackUserAccountInformation {

  private String userId;
  private User user;
  private Session session;
  private UserAgency userAgency;
  private boolean rollBackUserAccount;
}

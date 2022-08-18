package de.caritas.cob.userservice.api.config.migration;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.config.BeanAwareSpringLiquibase;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemporaryPublicPrivateKeysTask implements CustomTaskChange {

  private RocketChatService rocketChatService;

  @Override
  public void execute(Database database) {
    log.info("{}", rocketChatService.findUser(""));
  }

  @Override
  public String getConfirmationMessage() {
    return null;
  }

  @Override
  public void setUp() throws SetupException {
    try {
      rocketChatService = BeanAwareSpringLiquibase.getBean(RocketChatService.class);
    } catch (InstantiationException e) {
      throw new SetupException(e);
    }
  }

  @Override
  public void setFileOpener(ResourceAccessor resourceAccessor) {
    log.info("setFileOpener called");
  }

  @Override
  public ValidationErrors validate(Database database) {
    return null;
  }
}

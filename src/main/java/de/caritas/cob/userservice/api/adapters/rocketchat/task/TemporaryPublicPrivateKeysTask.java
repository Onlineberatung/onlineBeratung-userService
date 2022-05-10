package de.caritas.cob.userservice.api.adapters.rocketchat.task;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.config.BeanAwareSpringLiquibase;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

public class TemporaryPublicPrivateKeysTask implements CustomTaskChange {

  private RocketChatService rocketChatService;

  @Override
  public void execute(Database database) {
    System.out.println(rocketChatService.findUser(""));
  }

  @Override
  public String getConfirmationMessage() {
    return null;
  }

  @Override
  public void setUp() throws SetupException {
    try {
      rocketChatService = BeanAwareSpringLiquibase.getBean(RocketChatService.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setFileOpener(ResourceAccessor resourceAccessor) {
  }

  @Override
  public ValidationErrors validate(Database database) {
    return null;
  }
}


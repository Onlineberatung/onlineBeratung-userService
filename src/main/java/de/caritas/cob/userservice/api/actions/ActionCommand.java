package de.caritas.cob.userservice.api.actions;

public interface ActionCommand<T> {

  void execute(T actionTarget);
}

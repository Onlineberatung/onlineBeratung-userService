package de.caritas.cob.userservice.api.exception;

public class ImportException extends RuntimeException {

  /** Exception for import errors */
  private static final long serialVersionUID = -4222451820639535874L;

  public ImportException(String message) {
    super(message);
  }
}

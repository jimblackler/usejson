package net.jimblackler.usejson;

public class InternalParserError extends RuntimeException {
  public InternalParserError() {}

  public InternalParserError(String message) {
    super(message);
  }
}

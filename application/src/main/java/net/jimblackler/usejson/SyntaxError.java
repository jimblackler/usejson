package net.jimblackler.usejson;

public class SyntaxError extends RuntimeException {
  public SyntaxError(Throwable cause) {
    super(cause);
  }

  public SyntaxError(String message) {
    super(message);
  }
}

package net.jimblacker.usejson;

public class Util {
  static boolean isSpaceSeparator(char c) {
    return c == ' ';
  }

  public static boolean isIdStartChar(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '$') || (c == '_');
  }

  public static boolean isIdContinueChar(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '$')
        || (c == '_') || (c == '\u200C') || (c == '\u200D');
  }

  public static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  public static boolean isHexDigit(char c) {
    return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }
}

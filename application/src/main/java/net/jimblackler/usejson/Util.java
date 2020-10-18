package net.jimblackler.usejson;

/**
 * Based on https://github.com/json5/json5/blob/master/lib/util.js
 * Ported to Java by Jim Blackler (jimblackler@gmail.com).
 */
public class Util {
  static boolean isSpaceSeparator(char c) {
    return Unicode.SPACE_SEPARATOR.matcher(String.valueOf(c)).matches();
  }

  public static boolean isIdStartChar(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '$') || (c == '_')
        || Unicode.ID_START.matcher(String.valueOf(c)).matches();
  }

  public static boolean isIdContinueChar(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '$')
        || (c == '_') || (c == '\u200C') || (c == '\u200D')
        || Unicode.ID_CONTINUE.matcher(String.valueOf(c)).matches();
  }

  public static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  public static boolean isHexDigit(char c) {
    return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }
}
